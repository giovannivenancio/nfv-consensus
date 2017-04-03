#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Usage:"
    echo "$0 <id> <config-file>"
    exit 1
fi

ID=$1
CONF=$2

java -Djava.net.preferIPv4Stack=true -cp bin ch.usi.inf.paxos.test.Main acceptor $ID $CONF
