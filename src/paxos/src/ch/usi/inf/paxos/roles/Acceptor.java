package ch.usi.inf.paxos.roles;

import java.io.IOException;
import java.util.HashMap;

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

public class Acceptor implements MessageListener {
	private final Role myRole = Role.ACCEPTOR;
	private int id;
	private HashMap<Long, Instance> instances;

	private ReceivingThread rt;

	public Acceptor(int id, String configFile) {
		try {
			this.id = id;
			instances = new HashMap<Long, Instance>();
			ConfigReader.loadConfiguration(configFile);
			ConnectionManager.initConnection(myRole);
			ConnectionManager.registerListener(MessageType.PREPARE, this);
			ConnectionManager.registerListener(MessageType.ACCEPT, this);
			rt = new ReceivingThread();
			new Thread(rt).start();
		} catch (Exception e) {
			Logger.logError("Unable to initialize connection: " + e.toString());
		}
	}

	@Override
	public void messageRecieved(Message msg) throws PaxosException {
		switch (msg.getType()) {
		case PREPARE:
			this.prepare(msg);
			break;
		case ACCEPT:
			this.accept(msg);
			break;
		default:
			throw new PaxosException("Unexpected type.");
		}
	}

	/**
	 * Receives prepare requests from the leader Should build a corresponding
	 * promise message and send it back to the proposer
	 * 
	 * @param msg
	 *            the received message of type PREPARE
	 */
	private void prepare(Message msg) {
		Message resp = null;
		Instance inst;
		long iid;
		try {
			iid = msg.getIid();
			inst = instances.get(iid);
			if (inst == null) {
				Logger.logDebug("PREPARE(" + iid + ") - new instance started");
				inst = new Instance(iid, msg.getBallot());
				inst.setCurrentPhase(Phase._1B);
				instances.put(iid, inst);
				resp = Message.createPromiseMessage(this.id, iid, msg.getBallot(), -1, null);
			} else {
				if (msg.getBallot() > inst.getCurrentBallot()) {
					Logger.logDebug("PREPARE(" + iid + ") - higher ballot: restarting from phase 1");
					resp = Message.createPromiseMessage(this.id, iid, msg.getBallot(), inst.getLastVotingBallot(), inst.getLastVotingValue());
					inst.setCurrentPhase(Phase._1B);
					inst.setCurrentBallot(msg.getBallot());
				} else if (msg.getBallot() < inst.getCurrentBallot()) {
					Logger.logDebug("PREPARE(" + iid + ") - smaller ballot: let the proposer know (instead of just ignoring)");
					resp = Message.createPromiseMessage(this.id, iid, inst.getCurrentBallot(), inst.getLastVotingBallot(), inst.getLastVotingValue());
				} else {
					Logger.logDebug("PREPARE(" + iid + ") - same ballot: possible timeout on proposer(?) or "
							+ "it is the same proposer trying to pass a previous decided instance (?)");
					resp = Message.createPromiseMessage(this.id, iid, msg.getBallot(), inst.getLastVotingBallot(), inst.getLastVotingValue());
					inst.setCurrentPhase(Phase._1B);
				}
			}
			ConnectionManager.sendToProposer(resp);
		} catch (IOException e) {
			Logger.logError("Could not send msg '" + resp + "': " + e.getMessage());
			e.printStackTrace();
		} catch (PaxosException e) {
			Logger.logError("Could not send msg '" + resp + "': " + e.getMessage());
		}
	}

	/**
	 * Receives accept requests from the leader Should build a corresponding
	 * accepted message and send it back to the proposer
	 * 
	 * @param msg
	 *            the received message of type ACCEPT
	 */
	private void accept(Message msg) {
		Message resp = null;
		Instance inst;
		long iid;
/*		if(id == 0 || id == 2){
			 try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/		try {
			iid = msg.getIid();
			inst = instances.get(iid);
			if (inst == null) {
				Logger.logDebug("ACCEPT(" + iid + ") - new instance started");
				inst = new Instance(iid, msg.getBallot(), msg.getBallot(), msg.getPropValue(), msg.getPropValue(), Phase._2B);
				instances.put(iid, inst);
				resp = Message.createAcceptedMessage(this.id, iid, msg.getBallot(), msg.getPropValue());
			} else {
				if (msg.getBallot() >= inst.getCurrentBallot()) {
					Logger.logDebug("ACCEPT(" + iid + ") - expected ballot: " + msg.getBallot());
					resp = Message.createAcceptedMessage(this.id, iid, msg.getBallot(), msg.getPropValue());
					inst.setCurrentPhase(Phase._2B);
					inst.setLastVotingBallot(msg.getBallot());
					inst.setLastVotingValue(msg.getPropValue());
				} else {
					Logger.logDebug("ACCEPT(" + iid + ") - smaller ballot. Received/Previous ballot = " + msg.getBallot() + "/" + inst.getCurrentBallot());
					resp = null;
				}
			}
			if (resp != null)
				ConnectionManager.sendToProposer(resp);
		} catch (IOException e) {
			Logger.logError("Could not send msg '" + resp + "': " + e.getMessage());
			e.printStackTrace();
		} catch (PaxosException e) {
			Logger.logError("Could not send msg '" + resp + "': " + e.getMessage());
		}
	}

}

/**
 * Synchronous system - Leader election: ✦ Let τ be a constant bigger than the
 * largest message delivery delay; for simplicity assume local execution takes
 * no time. ✦ Each process Pi waits until either: (a) Pi delivers a message, or
 * (b) τ * uid(i) time units elapse on Pi’s clock, at which time Pi broadcasts
 * (uid(i)). ✦ The first process to broadcast is elected.
 */
