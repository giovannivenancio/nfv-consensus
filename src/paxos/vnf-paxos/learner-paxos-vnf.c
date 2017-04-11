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


#include <stdlib.h>
#include <stdio.h>
#include <evpaxos.h>
#include <signal.h>
#include <string.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <errno.h>

#define MAX_VALUE_SIZE 8192
#define MANAGER_ADDR "127.0.1.1"
#define PORT 8901

struct client_value
{
  struct timeval t;
  size_t size;
  char value[MAX_VALUE_SIZE];
};

struct bufferevent* bev_manager;


static void
handle_sigint(int sig, short ev, void* arg)
{
	struct event_base* base = arg;
	printf("Caught signal %d\n", sig);
	event_base_loopexit(base, NULL);
}

static void
submit_manager(struct client_value* v)
{
    //size_t size = strlen(v->value);
    int size = 192;
    //printf("size = %d\n", size);

    //bufferevent_write(bev_manager, &size, sizeof(size_t));
    bufferevent_write(bev_manager, &v->value, size);
}


static void
deliver(unsigned iid, char* value, size_t size, void* arg)
{
	FILE *f = fopen("learner_debug.txt", "a");
	struct client_value* v = (struct client_value*)value;
	printf("Delivered value: %s\n", v->value);
	submit_manager(v);
	fprintf(f, "Recebi: %s\n", v->value);
	fclose(f);
}

static void
on_connect(struct bufferevent* bev, short events, void* arg)
{
	if (events & BEV_EVENT_CONNECTED) {
		printf("Connected to vnf manager\n");
	} else {
		printf("%s\n", evutil_socket_error_to_string(EVUTIL_SOCKET_ERROR()));
	}
}

struct bufferevent*
connect_to_vnf_manager(struct event_base* base)
{
        struct sockaddr_in addr;
        struct bufferevent* bev;

        memset(&addr, 0, sizeof(addr));
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = inet_addr(MANAGER_ADDR);
        addr.sin_port = htons(PORT);

	    bev = bufferevent_socket_new(base, -1, BEV_OPT_CLOSE_ON_FREE);
	    bufferevent_setcb(bev, NULL, NULL, on_connect, NULL);
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
start_learner(const char* config)
{
	struct event* sig;
	struct evlearner* lea;
	struct event_base* base;

	base = event_base_new();
	lea = evlearner_init(config, deliver, NULL, base);
	if (lea == NULL) {
		printf("Could not start the learner!\n");
		exit(1);
	}

	bev_manager = connect_to_vnf_manager(base);

	sig = evsignal_new(base, SIGINT, handle_sigint, base);
	evsignal_add(sig, NULL);

	event_base_dispatch(base);

	event_free(sig);
	evlearner_free(lea);
    bufferevent_free(bev_manager);
	event_base_free(base);
}


int
main(int argc, char const *argv[])
{
	const char* config = "../paxos.conf";

	if (argc != 1 && argc != 2) {
		printf("Usage: %s [path/to/paxos.conf]\n", argv[0]);
		exit(1);
	}

	if (argc == 2)
		config = argv[1];

	start_learner(config);

	return 0;
}

