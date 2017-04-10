#!/usr/bin/env bash

export LD_LIBRARY_PATH=/usr/lib:/usr/local/lib:/projects/nfv-consensus/src/paxos/libpaxos/build/local/lib:$LD_LIBRARY_PATH
/projects/nfv-consensus/src/paxos/vnf-paxos/client-paxos-vnf /projects/nfv-consensus/src/paxos/vnf-paxos/paxos.conf &
ryu-manager /projects/nfv-consensus/src/controller/controller.py /projects/nfv-consensus/src/controller/ofctl_rest.py
