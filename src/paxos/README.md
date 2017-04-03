# Description
Paxos Java implementation with IP multicast.

It implements the basic paxos algorithm, assuming a simple majority of non-faulty acceptors.

It provides safety and liveness with at least a majority of acceptors and one proposer.

We have implemented leader election by using the Bully Leader Election algorithm. If there are more than one proposer, the proposer with lower id will be the leader.

# Configuration
The configuration is provided by a text file where each property is expressed as:

	property value
You can find an example file (paxos.properties) where it is possible to see all the available properties:

	#addresses of each role (IP multicast address and port)
	PROPOSERS=239.0.0.1:5000
	CLIENTS=239.0.0.1:6000
	ACCEPTORS=239.0.0.1:7000
	LEARNERS=239.0.0.1:8000

	#number of acceptors (in order to define quorum)
	NUM_ACCEPTORS=3

	#maximum size of a message (bytes)
	MAX_MSG_SIZE=1024

	TIMEOUT=5

	#how much verbosity (from minimal to max): ERROR/WARN/INFO/DEBUG
	LOG_LEVEL=DEBUG 


# Compilation
Just run: 
	
	ant pack

The source code is in './src' folder, the generated class files should be in './bin' and the packaged java is named 'Paxos.jar'


# Usage
For each role, run the corresponding script with the id and config file as parameters:
## Client:
	client.sh <id> <conf-file>
## Proposer:
	proposer.sh <id> <conf-file>
## Acceptor:
	acceptor.sh <id> <conf-file>
## Learner:
	learner.sh <id> <conf-file>

##Other features
You can run manually with the command:
	
	java -jar Paxos.jar <role> <id> <conf-file>
	

# TODO
##Leader election:

Leader catchup is not implemented yet.

##Statistics

	
This you start a process that generate random String and is also a learner. As soon as it learns its own message it will send the next.

Each one second the process output some data, which has the following format:
	
	<TIME INTERVAL> <NUMBER OF MESSAGES> <SAMPLE LATENCY> <AVERAGE LATENCY> <RATE (Mbps)>
	
The rate is obtained by multiplying the number of messages received in a interval by the size of the generated message.


# Notes
On OSX, in order to work correctly with IP Multicast, please run with the following parameter (already in the scripts):

    java -Djava.net.preferIPv4Stack=true

