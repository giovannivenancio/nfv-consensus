/*
 * Copyright (c) 2013-2015, University of Lugano
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the copyright holders nor the names of it
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


#include <evpaxos.h>
#include <errno.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <signal.h>
#include <event2/event.h>
#include <netinet/tcp.h>
#include <event2/listener.h>
#include <event2/buffer.h>

#define MAX_VALUE_SIZE 8192
#define PORT 8900

struct client_value
{
  struct timeval t;
  size_t size;
  char value[MAX_VALUE_SIZE];
};

struct stats
{
	int delivered;
	long min_latency;
	long max_latency;
	long avg_latency;
};

struct client
{
	int value_size;
	int outstanding;
	int recv_conn;
	struct stats stats;
	struct event_base* base;
	struct bufferevent* bev;
	struct event* stats_ev;
	struct timeval stats_interval;
	struct event* sig;
    struct evconnlistener* listener;
    struct evlearner* learner;
};

static void
handle_sigint(int sig, short ev, void* arg)
{
	struct event_base* base = arg;
	printf("Caught signal %d\n", sig);
	event_base_loopexit(base, NULL);
}

static void
random_string(char *s, const int len)
{
	int i;
	static const char alphanum[] =
		"0123456789abcdefghijklmnopqrstuvwxyz";
	for (i = 0; i < len-1; ++i)
		s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
	s[len-1] = 0;
}

static void
client_submit_internal_value(struct client* c)
{
	struct client_value v;
	gettimeofday(&v.t, NULL);
	v.size = c->value_size;
	random_string(v.value, v.size);
	size_t size = sizeof(struct timeval) + sizeof(size_t) + v.size;
	paxos_submit(c->bev, (char*)&v, size);
}

static void
client_submit_value(struct client* c, char *string, int len)
{
    struct client_value v;
    /*FILE *f = fopen("client_debug.txt", "a");
    if (f == NULL)
    {
        printf("Error opening file!\n");
        exit(1);
    }*/

    gettimeofday(&v.t, NULL);
    strcpy(v.value, string);
    v.size = len;
   size_t size = sizeof(struct timeval) + sizeof(size_t) + v.size;
    /*fprintf(f, "%d\n", size);
    fprintf(f, "%s\n", v.value);*/
    paxos_submit(c->bev, (char*)&v, size);
    //fclose(f);
}

// Returns t2 - t1 in microseconds.
static long
timeval_diff(struct timeval* t1, struct timeval* t2)
{
	long us;
	us = (t2->tv_sec - t1->tv_sec) * 1e6;
	if (us < 0) return 0;
	us += (t2->tv_usec - t1->tv_usec);
	return us;
}

static void
update_stats(struct stats* stats, struct client_value* delivered)
{
	struct timeval tv;
	gettimeofday(&tv, NULL);
	long lat = timeval_diff(&delivered->t, &tv);
	stats->delivered++;
	stats->avg_latency = stats->avg_latency +
		((lat - stats->avg_latency) / stats->delivered);
	if (stats->min_latency == 0 || lat < stats->min_latency)
		stats->min_latency = lat;
	if (lat > stats->max_latency)
		stats->max_latency = lat;
}

static void
on_deliver(unsigned iid, char* value, size_t size, void* arg)
{
	struct client* c = arg;
	struct client_value* v = (struct client_value*)value;
	update_stats(&c->stats, v);
}

static void
on_stats(evutil_socket_t fd, short event, void *arg)
{
	struct client* c = arg;
	double mbps = (double)(c->stats.delivered*c->value_size*8) / (1024*1024);
	printf("%d value/sec, %.2f Mbps, latency min %ld us max %ld us avg %ld us\n",
		c->stats.delivered, mbps, c->stats.min_latency, c->stats.max_latency,
		c->stats.avg_latency);
	memset(&c->stats, 0, sizeof(struct stats));
	event_add(c->stats_ev, &c->stats_interval);
}

static void
on_connect(struct bufferevent* bev, short events, void* arg)
{
	int i;
	struct client* c = arg;
	if (events & BEV_EVENT_CONNECTED) {
		printf("Connected to proposer\n");
		//for (i = 0; i < c->outstanding; ++i)
		//	client_submit_internal_value(c);
	} else {
		printf("%s\n", evutil_socket_error_to_string(EVUTIL_SOCKET_ERROR()));
	}
}

static struct bufferevent*
connect_to_proposer(struct client* c, const char* config, int proposer_id)
{
	struct bufferevent* bev;
	struct evpaxos_config* conf = evpaxos_config_read(config);
	if (conf == NULL) {
		printf("Failed to read config file %s\n", config);
		return NULL;
	}
	struct sockaddr_in addr = evpaxos_proposer_address(conf, proposer_id);
	bev = bufferevent_socket_new(c->base, -1, BEV_OPT_CLOSE_ON_FREE);
	bufferevent_setcb(bev, NULL, NULL, on_connect, c);
	bufferevent_enable(bev, EV_READ|EV_WRITE);
	bufferevent_socket_connect(bev, (struct sockaddr*)&addr, sizeof(addr));
	int flag = 1;
	setsockopt(bufferevent_getfd(bev), IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(int));
	return bev;
}

static void
extract_data(struct evbuffer *input, size_t len, struct client *c, struct bufferevent *bev){
    FILE *f = fopen("client_debug.txt", "a");
    char v[MAX_VALUE_SIZE];
    char dados[3];
    int dados_size;
    // size_t header_size = sizeof(size_t);
    size_t header_size = sizeof(char) * 3;
    //printf("header size: %d\n", header_size);
    while (len >=  header_size){
        evbuffer_copyout(input,&dados, header_size);
	    dados_size = atoi(dados);
	    //printf("dadossize: %d\n", dados_size);
        fprintf(f, "dadossize: %d\n", dados_size);
        len = len -  header_size;
	    //printf("len: %d\n", len);
        if ( len >= dados_size){
    	    evbuffer_drain(input, header_size);
            evbuffer_remove(input, &v, dados_size);
    	    //v[dados_size] = '\0';
            len = len - dados_size;
            client_submit_value(c, v, dados_size);
            //printf("Received message %s \n", v);
            fprintf(f, "Received message %s \n", v);
        }
    }
    fclose(f);
}

static void
on_recv_value(struct bufferevent *bev, void *arg)
{
    //FILE *f = fopen("client_debug.txt", "a");
    struct client *c = arg;
    struct evbuffer *input = bufferevent_get_input(bev);
    size_t len = evbuffer_get_length(input);
    //printf("new data to read: %d\n", len);
    //fprintf(f, "new data to read: %d\n", len);
    //fclose(f);
    extract_data(input, len, c, bev);

}

static void
on_accept_ev(struct bufferevent *bev, short events, void *arg)
{
    if (events & BEV_EVENT_ERROR)
        perror("Error from bufferevent");
    if (events & (BEV_EVENT_EOF | BEV_EVENT_ERROR)) {
        bufferevent_free(bev);
    }
}

static void
on_accept_conn(struct evconnlistener *listener, evutil_socket_t fd, struct sockaddr *address, int socklen, void *arg)
{
    struct client *c = arg;
    struct event_base *base = evconnlistener_get_base(listener);
    struct bufferevent *bev = bufferevent_socket_new(base, fd, BEV_OPT_CLOSE_ON_FREE);

    printf("We got a new connection %d\n", c->recv_conn++);

    bufferevent_setcb(bev, on_recv_value, NULL, on_accept_ev, c);
    bufferevent_enable(bev, EV_READ | EV_WRITE);

    int flag = 1;
    setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(int));
}

static void
on_accept_error(struct evconnlistener *listener, void *arg)
{
    struct event_base *base = evconnlistener_get_base(listener);
    int err = EVUTIL_SOCKET_ERROR();
    fprintf(stderr, "Got an error %d (%s) on the listener. "
            "Shutting down.\n", err, evutil_socket_error_to_string(err));

    event_base_loopexit(base, NULL);
}

static struct evconnlistener*
start_listener(struct client* c)
{
    struct evconnlistener* lev = NULL;
    struct sockaddr_in addr;

    memset(&addr, 0, sizeof(struct sockaddr_in));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = htonl(0);
	addr.sin_port = htons(PORT);

    lev = evconnlistener_new_bind(c->base, on_accept_conn, c,
                                       LEV_OPT_CLOSE_ON_FREE | LEV_OPT_REUSEABLE, -1,
                                       (struct sockaddr *) &addr, sizeof(addr));
    if (!lev){
        perror("Couldn't create listener");
        exit(1);
    }
    evconnlistener_set_error_cb(lev, on_accept_error);

    return lev;
}

static struct client*
make_client(const char* config, int proposer_id, int outstanding, int value_size)
{
	struct client* c;
	c = malloc(sizeof(struct client));
	c->base = event_base_new();

	c->value_size = value_size;
	c->outstanding = outstanding;
	c->recv_conn = 0;


	c->bev = connect_to_proposer(c, config, proposer_id);
	if (c->bev == NULL)
		exit(1);


    c->listener = start_listener(c);

	memset(&c->stats, 0, sizeof(struct stats));
	c->stats_interval = (struct timeval){1, 0};
	c->stats_ev = evtimer_new(c->base, on_stats, c);
	event_add(c->stats_ev, &c->stats_interval);

	//paxos_config.learner_catch_up = 0;
	c->learner = evlearner_init(config, on_deliver, c, c->base);

	c->sig = evsignal_new(c->base, SIGINT, handle_sigint, c->base);
	evsignal_add(c->sig, NULL);

	return c;
}

static void
client_free(struct client* c)
{
	bufferevent_free(c->bev);
	event_free(c->stats_ev);
	event_free(c->sig);
	event_base_free(c->base);
    evconnlistener_free(c->listener);
	free(c);
}

static void
start_client(const char* config, int proposer_id, int outstanding, int value_size)
{
	struct client* client;
	client = make_client(config, proposer_id, outstanding, value_size);
	event_base_dispatch(client->base);
	client_free(client);
}

static void
usage(const char* name)
{
	printf("Usage: %s [path/to/paxos.conf] [-h] [-o] [-v] [-p]\n", name);
	printf("  %-30s%s\n", "-h, --help", "Output this message and exit");
	printf("  %-30s%s\n", "-o, --outstanding #", "Number of outstanding client values");
	printf("  %-30s%s\n", "-v, --value-size #", "Size of client value (in bytes)");
	printf("  %-30s%s\n", "-p, --proposer-id #", "d of the proposer to connect to");
	exit(1);
}

int
main(int argc, char const *argv[])
{
	int i = 1;
	int proposer_id = 0;
	int outstanding = 1;
	int value_size = 32;
	const char* config = "../paxos.conf";

	if (argc > 1 && argv[1][0] != '-') {
		config = argv[1];
		i++;
	}

	while (i != argc) {
		if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "--help") == 0)
			usage(argv[0]);
		else if (strcmp(argv[i], "-o") == 0 || strcmp(argv[i], "--outstanding") == 0)
			outstanding = atoi(argv[++i]);
		else if (strcmp(argv[i], "-v") == 0 || strcmp(argv[i], "--value-size") == 0)
			value_size = atoi(argv[++i]);
		else if (strcmp(argv[i], "-p") == 0 || strcmp(argv[i], "--proposer-id") == 0)
			proposer_id = atoi(argv[++i]);
		else
			usage(argv[0]);
		i++;
	}

	srand(time(NULL));
	start_client(config, proposer_id, outstanding, value_size);

	return 0;
}

