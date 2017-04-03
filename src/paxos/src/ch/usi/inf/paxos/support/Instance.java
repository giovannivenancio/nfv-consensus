package ch.usi.inf.paxos.support;

import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import ch.usi.inf.paxos.exception.PaxosException;

public class Instance {
	private long iid, currentBallot, lastVotingBallot;
	private byte[] currentValue, lastVotingValue;
	private Phase currentPhase;
	private Message clientMessage;
	private int countPromisses;
	private int countAccepts;
	private boolean sendDecision;
	private boolean timeout;
	private Toolkit toolkit;
	private Timer timer;

	public Instance(long iid, long currentBallot, Message msg, long lastVotingBallot) {
		super();
		this.iid = iid;
		this.currentBallot = currentBallot;
		this.lastVotingBallot = lastVotingBallot;
		this.currentValue = null;
		this.lastVotingValue = null;
		this.currentPhase = Phase._1A;
		this.countPromisses = 0;
		this.countAccepts = 0;
		this.sendDecision = true;
		this.timeout = false;
		this.setClientMessage(msg);
	}

	public Instance(long iid, long currentBallot) {
		super();
		this.iid = iid;
		this.currentBallot = currentBallot;
		this.lastVotingBallot = -1;
		this.currentValue = null;
		this.lastVotingValue = null;
		this.currentPhase = Phase._1A;
	}

	public Instance(long iid, long currentBallot, byte[] currentValue, Phase currentPhase) {
		super();
		this.iid = iid;
		this.currentBallot = currentBallot;
		this.currentValue = currentValue;
		this.currentPhase = currentPhase;
		this.lastVotingValue = null;
		this.lastVotingBallot = -1;
	}

	public Instance(long iid, long currentBallot, long lastVotingBallot, byte[] currentValue, byte[] lastVotingValue,
			Phase currentPhase) {
		super();
		this.iid = iid;
		this.currentBallot = currentBallot;
		this.lastVotingBallot = lastVotingBallot;
		this.currentValue = currentValue;
		this.lastVotingValue = lastVotingValue;
		this.currentPhase = currentPhase;
	}

	public long getIid() {
		return iid;
	}

	public void setIid(long iid) {
		this.iid = iid;
	}

	public long getCurrentBallot() {
		return currentBallot;
	}

	public void setCurrentBallot(long currentBallot) {
		this.currentBallot = currentBallot;
	}

	public long getLastVotingBallot() {
		return lastVotingBallot;
	}

	public void setLastVotingBallot(long lastVotingBallot) {
		this.lastVotingBallot = lastVotingBallot;
	}

	public byte[] getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(byte[] currentValue) {
		this.currentValue = currentValue;
	}

	public byte[] getLastVotingValue() {
		return lastVotingValue;
	}

	public void setLastVotingValue(byte[] lastVotingValue) {
		this.lastVotingValue = lastVotingValue;
	}

	public Phase getCurrentPhase() {
		return currentPhase;
	}

	public void setCurrentPhase(Phase currentPhase) {
		this.currentPhase = currentPhase;
	}

	public Message getClientMessage() {
		return clientMessage;
	}

	public void setClientMessage(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	public int getCountPromisses() {
		return countPromisses;
	}

	public void setCountPromisses(int countPromisses) {
		this.countPromisses = countPromisses;
	}

	public int getCountAccepts() {
		return countAccepts;
	}

	public void setCountAccepts(int countAccepts) {
		this.countAccepts = countAccepts;
	}

	public boolean isSendDecision() {
		return sendDecision;
	}

	public void setSendDecision(boolean sendDecision) {
		this.sendDecision = sendDecision;
	}

	public Timer getTimer() {
		return this.timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void startTimeout() {
		toolkit = Toolkit.getDefaultToolkit();
		this.timer = new Timer();
		int time = 0;
		try {
			time = ConfigReader.getTimeout();
		} catch (PaxosException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.timer.schedule(new RemindTask(), time * 1000);
	}

	public boolean isTimeout() {
		return timeout;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	class RemindTask extends TimerTask {
		public void run() {
			//System.out.println("Time's up!");
			toolkit.beep();
			timer.cancel(); //Not necessary because we call System.exit
		    timeout = true;

			//System.exit(0); //Stops the AWT thread (and everything else)
		}
	}
}
