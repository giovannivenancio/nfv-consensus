#!/usr/bin/env bash

INCLUDE_DIR=/home/gvsouza/projects/nfv-consensus/src/paxos/libpaxos/build/local/include
LIB_DIR=/home/gvsouza/projects/nfv-consensus/src/paxos/libpaxos/build/local/lib

cc -Wall -g -I$INCLUDE_DIR -c client-vnf-test.c -o client-vnf-test.o
cc -Wall -g -I$INCLUDE_DIR -o client-vnf-test client-vnf-test.o -L$LIB_DIR -levent

cc -Wall -g -I$INCLUDE_DIR -c learner-paxos-vnf.c -o learner-paxos-vnf.o
cc -Wall -g -I$INCLUDE_DIR -o learner-paxos-vnf learner-paxos-vnf.o -L$LIB_DIR -levpaxos -levent

cc -Wall -g -I$INCLUDE_DIR -c client-paxos-vnf.c -o client-paxos-vnf.o
cc -Wall -g -I$INCLUDE_DIR -o client-paxos-vnf client-paxos-vnf.o -L$LIB_DIR -levpaxos -levent

cc -Wall -g -I$INCLUDE_DIR -c proposer.c -o proposer.o
cc -Wall -g -I$INCLUDE_DIR -o proposer proposer.o -L$LIB_DIR -levpaxos -levent

cc -Wall -g -I$INCLUDE_DIR -c acceptor.c -o acceptor.o
cc -Wall -g -I$INCLUDE_DIR -o acceptor acceptor.o -L$LIB_DIR -levpaxos -levent

cc -Wall -g -I$INCLUDE_DIR -c learner.c -o learner.o
cc -Wall -g -I$INCLUDE_DIR -o learner learner.o -L$LIB_DIR -levpaxos -levent

cc -Wall -g -I$INCLUDE_DIR -c replica.c -o replica.o
cc -Wall -g -I$INCLUDE_DIR -o replica replica.o -L$LIB_DIR -levpaxos -levent
