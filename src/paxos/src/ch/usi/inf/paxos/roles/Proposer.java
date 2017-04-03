package ch.usi.inf.paxos.roles;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import ch.usi.inf.paxos.exception.PaxosException;
import ch.usi.inf.paxos.support.ConfigReader;
import ch.usi.inf.paxos.support.ConnectionManager;
import ch.usi.inf.paxos.support.Instance;
import ch.usi.inf.paxos.support.Logger;
import ch.usi.inf.paxos.support.Message;
import ch.usi.inf.paxos.support.MessageListener;
import ch.usi.inf.paxos.support.MessageType;
import ch.usi.inf.paxos.support.Phase;
import ch.usi.inf.paxos.support.ReceivingThread;

public class Proposer implements MessageListener {

	private long iid = 0;
	private int id = 0;
	private Role myRole = Role.PROPOSER;
	private long leaderId = 0;
	private HashMap<Long, Instance> instances;
	private Timer instanceTask;
	private Timer leaderTask;
	private boolean leader = false;
	private boolean election = false;
	private boolean aguardaOkMsg = true;
	private int timeout;
	private long lastAlive;
	private long receivedValues = 0;


	/**
	 * Proposer begins with a identification and a configuration file
	 *
	 * He receives promise and accepted from acceptors and client messages
	 * from clients.
	 *
	 * @param id
	 * @param configFile
	 */
	public Proposer(int id, String configFile) {
		this.id = id;
		this.leaderId = -1; // proposer 0 will be leader at beginning
		instances = new HashMap<Long, Instance>();
		try {
			ConfigReader.loadConfiguration(configFile);
			ConnectionManager.initConnection(myRole);
			ConnectionManager.registerListener(MessageType.PROMISE, this);
			ConnectionManager.registerListener(MessageType.ACCEPTED, this);
			ConnectionManager.registerListener(MessageType.CLIENT, this);


			this.timeout = ConfigReader.getTimeout();
			ReceivingThread rt = new ReceivingThread();
			new Thread(rt).start();
			this.checkUnfinishedInstances(); // checking unfinished instances
			this.leaderElection();
		} catch (PaxosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Phase 1a - Starting the Paxos consensus protocol.
	 * There will be many instances. Each instance has a decided value.
	 * Each prepare request has an unique ballot. The ballot increments at each request.
	 *
	 * @param m
	 */
	private void prepare(Instance instance) {


		instance.setCurrentBallot(instance.getCurrentBallot() + 100);
		Message msgPrepare = Message.createPrepareMessage(instance.getIid(), instance.getCurrentBallot());
		instance.setCurrentPhase(Phase._1A);

		Logger.logDebug("*** Starting phase 1a - PREPARE ***" + " iid: " + instance.getIid()+ " ballot: " + instance.getCurrentBallot());

		try {
			ConnectionManager.sendToAcceptor(msgPrepare);
			instance.startTimeout();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PaxosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Collecting results from phase 1b, that is, the acceptors' answers. If
	 * there is a quorum then it starts phase 2a. Otherwise, go to phase 1a
	 * again
	 *
	 * @param msg
	 */

	private void promises(Instance instance, Message msg) {

		Logger.logDebug("PROMISE from acceptor (" + msg.getAid() + ") iid: " + instance.getIid() + " proposer ballot: "
				+ instance.getCurrentBallot() + " accept ballot: " + msg.getLastVotingBallot() + " accept value: "
				+ (msg.getLastVotingValue() == null ? "null" : new String(msg.getLastVotingValue() )));


		if ( (instance.getCurrentBallot() == msg.getBallot()) ){
			Logger.logDebug("receive a promise from acceptor "+msg.getAid());

			instance.setCountPromisses(instance.getCountPromisses() + 1);

			// If there is a previous ballot for that instance in any acceptor than get the highest and use the voted value
			if(msg.getLastVotingBallot() > instance.getLastVotingBallot()){

				instance.setLastVotingBallot(msg.getLastVotingBallot());
				instance.setLastVotingValue(msg.getLastVotingValue());
			}

			try {
				if (instance.getCountPromisses() == ConfigReader.getQuorum()){
					Logger.logDebug("Have a quorum in the phase 1");
					instance.getTimer().cancel(); // cancel the timer because we have a quorum now.

					instance.setCurrentPhase(Phase._2A); // Lets do the phase 2A
					if (instance.getLastVotingBallot() == 0){ // There is no previous ballot in any acceptor.
						// It will be used the proposed value by the client
						instance.setCurrentValue(instance.getClientMessage().getPropValue());

					}else{
						// It will be used one of values from the acceptors
						instance.setCurrentValue(instance.getLastVotingValue());
					}

					Message msgAccept = Message.createAcceptMessage(instance.getIid(), instance.getCurrentBallot(), instance.getCurrentValue());

					Logger.logDebug("*** Starting phase 2a - ACCEPT *** iid: " + instance.getIid() + " ballot: " + instance.getCurrentBallot()
							+ " value: " + new String(instance.getCurrentValue()));

					ConnectionManager.sendToAcceptor(msgAccept);
					instance.startTimeout(); // It gonna start to count the time for the  request of the phase 2A
				}
			} catch (PaxosException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (msg.getBallot() > instance.getCurrentBallot()) {
			// It should return to previous phase
			Logger.logDebug("**** Restarting  phase 1a (leader has received a greater ballot) **** ");
			instance.setCountPromisses(0);
			instance.setTimeout(false);
			this.prepare(instance);
		}
	}

	/**
	 * Collecting results from phase 2b, that is, the acceptors' answers. If
	 * there is a quorum then send to the learner the decision
	 *
	 * @param msg
	 */

	private void accepted(Instance instance, Message msg) {

		Logger.logDebug("ACCEPTED from acceptor (" + msg.getAid() + ") Instance "+ instance.getIid() );

		instance.setCountAccepts(instance.getCountAccepts() + 1);

		if ( (instance.getCurrentBallot() != msg.getBallot()) ) {
			instance.setSendDecision(false);
		}
		try {
			if ((instance.getCountAccepts() == ConfigReader.getQuorum()) ) {
				instance.getTimer().cancel();


				if (instance.isSendDecision()) {
					instance.setCurrentPhase(Phase._3);
					Message msgAccepted = Message.createDecisionMessage(instance.getIid(), msg.getPropValue());

					Logger.logDebug("*** Starting phase 3 - DECISION *** iid: " + instance.getIid()+ " ballot: "
							+ instance.getCurrentBallot() + " value: " + new String(msg.getPropValue()));

					ConnectionManager.sendToLearner(msgAccepted);
				}else{
					Logger.logDebug("***  Restarting  phase 1a (leader isn't able to send the decision message) ***");
					instance.setCountAccepts(0);
					instance.setCountPromisses(0);
					instance.setTimeout(false);
					this.prepare(instance);

				}
			}
		} catch (PaxosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void messageRecieved(Message msg) throws PaxosException {
		if (this.id == this.leaderId) {
			switch (msg.getType()) {
			case CLIENT:
				this.receivedValues++;
				this.prepare(this.startInstance(msg));
				break;

			case PROMISE:
				// a message can be received with no started instance if it was
				if (this.instances.get(msg.getIid()) == null){
					// TODO think what to do
					Logger.logDebug("instance id "+ msg.getIid()+" is null");
					//this.prepare(startInstance(msg));
				}else{
					this.promises(this.instances.get(msg.getIid()), msg);
				}
				break;

			case ACCEPTED:
				this.accepted(this.instances.get(msg.getIid()), msg);
				break;
			default:
				Logger.logDebug("Message isn't recognize");
				break;
			}
		}
	}

	private Instance startInstance(Message msg) {
		this.iid++;
		this.instances.put(this.iid, new Instance(this.iid, this.id, msg, 0));
		return this.instances.get(iid);
	}


	private void leaderElection() {
		this.leaderTask = new Timer();
		this.leaderTask.schedule(new BullyLeaderElection(), this.timeout * 1000);
	}

	/*
	 * After a time this method resubmits the unfinished instance (because a timeout)
	 */
	private void checkUnfinishedInstances() {
		this.instanceTask = new Timer();
		this.instanceTask.schedule(new CheckInstances(), this.timeout * 1000);
	}

	class BullyLeaderElection extends TimerTask implements MessageListener{

		private boolean leaderIsAlive;
		private int leaderTimeout;

		public BullyLeaderElection() {
			ConnectionManager.registerListener(MessageType.ALIVE, this);
			ConnectionManager.registerListener(MessageType.ELECTION, this);
			ConnectionManager.registerListener(MessageType.OK, this);

			this.leaderTimeout = timeout * 1000;
		}

		@Override
		public void messageRecieved(Message msg) throws PaxosException {

			switch (msg.getType()) {
			case ALIVE:
				this.alive(msg);
				break;
			case ELECTION:
				this.election(msg);
				break;
			case OK:
				this.ok(msg);
			default:
				break;
			}
		}
		private void ok(Message msg) {
			if( msg.getProposerId() < id ){
				Logger.logDebug("Message Type: "+ msg.getType()+" from proposer id: "+msg.getProposerId());
				aguardaOkMsg = true;
				lastAlive = System.currentTimeMillis();
			}
		}

		private void election(Message msg) {
			Logger.logDebug("Message Type: "+ msg.getType()+" from proposer id: "+msg.getProposerId());
			if (id < msg.getProposerId()){ // I can be the leader
				// send a ok message
				Message msgOk = Message.createOkMessage(id);
				try {
					ConnectionManager.sendToProposer(msgOk);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PaxosException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		private void alive(Message msg) {
			Logger.logDebug("Message Type: "+ msg.getType()+" from proposer id: "+msg.getProposerId());
			lastAlive = System.currentTimeMillis();
			this.leaderIsAlive = true;
			leaderId = msg.getProposerId();
		}


		@Override
		public void run() {
			Logger.logDebug("Running BullyLeaderElection");
			if(id == leaderId){ // I am the leader
				Logger.logDebug("I am the leader ");
				Message msgAlive = Message.createAliveMessage(id);
				try {
					ConnectionManager.sendToProposer(msgAlive);
				} catch (IOException | PaxosException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{ // if not receiving the "I am alive" message then start a election
				// send election message to all proposer with my proposer id

				if ( (System.currentTimeMillis() - lastAlive) > leaderTimeout )  {
					this.leaderIsAlive = false;
					Message msg = null;

					if(!aguardaOkMsg){
						// I am the leader, send the I am alive
						msg = Message.createAliveMessage(id);

					}else{

						msg = Message.createElectionMessage(id);
						lastAlive = System.currentTimeMillis();
						aguardaOkMsg = false;
					}
					try {
						ConnectionManager.sendToProposer(msg);
					} catch (IOException | PaxosException e) {
						e.printStackTrace();
					}
				}
			}
			leaderTask.schedule(new BullyLeaderElection(), timeout * 1000);
		}

	}

	class CheckInstances extends TimerTask {

		public void run() {
			boolean flag = false;
			Logger.logDebug("Checking unfinished instance");
			//task.cancel(); //Not necessary because we call System.exit
			Iterator<Long> keySetIterator = instances.keySet().iterator();

			while(keySetIterator.hasNext()){
				Long key = keySetIterator.next();
				Instance instance = instances.get(key);

				if( instance.isTimeout() && instance.getCurrentPhase() != Phase._3){
					Logger.logDebug("Instance "+ instances.get(key).getIid() +" resubimmited to phase 1 (promisses: "+ instance.getCountPromisses()+", accepts: "+instance.getCountAccepts()+")");
					instances.get(key).setCountAccepts(0);
					instances.get(key).setCountPromisses(0);
					prepare(instances.get(key));
					flag = true;
				}
			}
			if(!flag){
				Logger.logDebug("There is no unfinished instance (received "+receivedValues+" values from clients)");
			}
			instanceTask.schedule(new CheckInstances(), timeout * 1000);
		}
	}
}
