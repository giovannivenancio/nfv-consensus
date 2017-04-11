cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c client-vnf-test.c  -o client-vnf-test.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o client-vnf-test client-vnf-test.o -L/home/gvsouza/libpaxos/build/local/lib -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c learner-paxos-vnf.c  -o learner-paxos-vnf.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o learner-paxos-vnf learner-paxos-vnf.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c client-paxos-vnf.c  -o client-paxos-vnf.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o client-paxos-vnf client-paxos-vnf.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c proposer.c  -o proposer.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o proposer proposer.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c acceptor.c  -o acceptor.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o acceptor acceptor.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c learner.c  -o learner.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o learner learner.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent

cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -c replica.c  -o replica.o
cc   -Wall -g -I/projects/nfv-consensus/src/paxos/libpaxos/build/local/include   -o replica replica.o -L/home/gvsouza/libpaxos/build/local/lib -levpaxos -levent
