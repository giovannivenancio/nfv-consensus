#!/usr/bin/env bash

CONFIG="paxos.conf"
OPT="--verbose"

tmux_test ()  {
	tmux new-session -d -s paxos
	tmux new-window -t paxos

	for (( i = 0; i < 7; i++ )); do
		tmux split
		tmux select-layout even-vertical
	done

    for (( i = 0; i < 3; i++ )); do
        tmux send-keys -t $i "$VG ./acceptor $i $CONFIG" C-m
    done

    tmux send-keys -t 3 "./proposer 0 $CONFIG" C-m
    tmux send-keys -t 4 "python server.py" C-m
    sleep 1
	tmux send-keys -t 5 "./client-paxos-vnf $CONFIG" C-m
    tmux send-keys -t 6 "./learner-paxos-vnf $CONFIG" C-m

	tmux selectp -t 7

	tmux attach-session -t paxos
	tmux kill-session -t paxos
}

usage () {
	echo "$0 [--help] [--build-dir dir] [--config-file] [--valgrind]
	[--silence-replica]"
	exit 1
}

while [[ $# > 0 ]]; do
	key="$1"
	case $key in
		-b|--build-dir)
		DIR=$2
		shift
		;;
		-c|--config)
		CONFIG=$2
		shift
		;;
		-h|--help)
		usage
		;;
		-s|--silence-replica)
		OPT=""
		;;
		-v|--valgrind)
		VG="valgrind "
		;;
		*)
		usage
		;;
	esac
	shift
done

tmux_test

