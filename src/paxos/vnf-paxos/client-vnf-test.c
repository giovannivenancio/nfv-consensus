
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

#include <errno.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <signal.h>
#include <event2/event.h>
#include <event2/listener.h>
#include <event2/bufferevent.h>
#include <event2/buffer.h>
#include <netinet/tcp.h>
#include <unistd.h>
#include <limits.h>
#include <arpa/inet.h>

#include <stdio.h>

#define MAX_VALUE_SIZE 8192

#define CLIENT_PAXOS_ADDR "127.0.1.1"
#define PORT 8900

int count = 0;

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
	int id;
	int outstanding;
	struct stats stats;
	struct event_base* base;
	struct bufferevent* bev;
	struct event* stats_ev;
	struct timeval stats_interval;
	struct event* sig;
	char msg[MAX_VALUE_SIZE];
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
client_submit_value(struct client* c, struct bufferevent *bev)
{
	struct client_value v;

	memset(&v, 0, sizeof(struct client_value));
    gettimeofday(&v.t, NULL);

    v.size = c->value_size;
	random_string(v.value, v.size);
    size_t size = sizeof(struct timeval) + sizeof(size_t) + v.size;

    bufferevent_write(bev, &size, sizeof(size_t));
    bufferevent_write(bev, &v, size);
}

static void
on_connect(struct bufferevent* bev, short events, void* arg)
{
	int i;
	struct client* c = arg;
	if (events & BEV_EVENT_CONNECTED) {
		printf("Connected to client-paxos\n");
		for (i = 0; i < c->outstanding; ++i)
			client_submit_value(c, bev);
	} else {
		printf("%s\n", evutil_socket_error_to_string(EVUTIL_SOCKET_ERROR()));
	}
}

static void
client_free(struct client* c)
{
        bufferevent_free(c->bev);
        event_free(c->stats_ev);
        event_free(c->sig);
        event_base_free(c->base);
        free(c);
}

struct bufferevent*
connect_to_client_paxos(struct client* c)
{
    struct sockaddr_in addr;
    struct bufferevent* bev;

	memset(&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(CLIENT_PAXOS_ADDR);
    addr.sin_port = htons(PORT);

	bev = bufferevent_socket_new(c->base, -1, BEV_OPT_CLOSE_ON_FREE);
	bufferevent_setcb(bev, NULL, NULL, on_connect, c);
	bufferevent_enable(bev, EV_READ|EV_WRITE);

    if (bufferevent_socket_connect(bev,
            (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        printf("Error starting connection");
        bufferevent_free(bev);
        exit(0);
   	 }
    int flag = 1;
	setsockopt(bufferevent_getfd(bev), IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(int));

	return bev;
}


static void
make_client(int id, int outstanding, int value_size)
{
	struct client* c;

	c = malloc(sizeof(struct client));
	c->base = event_base_new();

    c->value_size = value_size;
	c->outstanding = outstanding;
	c->id = id;

    c->bev = connect_to_client_paxos(c);

	c->sig = evsignal_new(c->base, SIGINT, handle_sigint, c->base); // signal event
    evsignal_add(c->sig, NULL);

    event_base_dispatch(c->base);
    client_free(c);

}

int main(int argc, char *argv[]){

    int outstanding = 10;
    int value_size = 32;
    int id = 1;

    make_client(id, outstanding, value_size);

    return 0;
}
