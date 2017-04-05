#!/usr/bin/env bash


CONFIG="paxos.conf"
OPT="--verbose"

tmux_test ()  {
	tmux new-session -d -s paxos
	tmux new-window -t paxos

	for (( i = 0; i < 4; i++ )); do
		tmux split
		tmux select-layout even-vertical
	done

	for (( i = 0; i < 3; i++ )); do
		tmux send-keys -t $i "$VG ./replica $i $CONFIG $OPT" C-m
	done

	tmux send-keys -t 3 "./client-paxos-vnf $CONFIG" C-m
	#tmux send-keys -t 4 "python client.py" C-m
	tmux selectp -t 4

	#tmux send-keys -t 4 "./client-vnf-test $CONFIG" C-m
	#tmux selectp -t 4

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
