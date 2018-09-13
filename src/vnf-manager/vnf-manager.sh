#!/usr/bin/env bash

export LD_LIBRARY_PATH=/usr/lib:/usr/local/lib:/projects/nfv-consensus/src/paxos/libpaxos/build/local/lib:$LD_LIBRARY_PATH
ryu-manager /projects/nfv-consensus/src/controller/controller.py /projects/nfv-consensus/src/controller/ofctl_rest.py &
python /projects/nfv-consensus/src/vnf-manager/vnf-manager.py
