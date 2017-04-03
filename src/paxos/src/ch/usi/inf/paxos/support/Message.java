package ch.usi.inf.paxos.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = -295989872137066267L;
	private MessageType type;
	private long iid;
	private long aid;  // acceptor id
	private long pid;  // proposer id
	private long ballot;
	private byte[] propValue;
	private long lastVotingBallot;
	private byte[] lastVotingValue;

	public Message() {
		this.type = null;
		this.iid = -1;
		this.ballot = -1;
		this.propValue = null;
		this.lastVotingBallot = -1;
		this.lastVotingValue = null;
	}

	public Message(MessageType type, long iid, long ballot, byte[] propValue) {
		this.type = type;
		this.iid = iid;
		this.aid = -1;
		this.ballot = ballot;
		this.propValue = propValue;
		this.lastVotingBallot = -1;
		this.lastVotingValue = null;
	}

	public Message(MessageType type, byte[] value) {
		this.type = type;
		this.iid = -1;
		this.aid = -1;
		this.ballot = -1;
		this.propValue = value;
		this.lastVotingBallot = -1;
		this.lastVotingValue = null;
	}

	public Message(MessageType type, long iid, long ballot) {
		this.type = type;
		this.iid = iid;
		this.aid = -1;
		this.ballot = ballot;
		this.propValue = null;
		this.lastVotingBallot = -1;
		this.lastVotingValue = null;
	}

	public Message(MessageType type, long iid, long aid, long ballot, byte[] propValue, long lastVotingBallot,
			byte[] lastVotingValue) {
		this.type = type;
		this.iid = iid;
		this.aid = aid;
		this.ballot = ballot;
		this.propValue = propValue;
		this.lastVotingBallot = lastVotingBallot;
		this.lastVotingValue = lastVotingValue;
	}

	public Message(MessageType type, long iid, long aid, long ballot, long lastVotingBallot, byte[] lastVotingValue) {
		this.type = type;
		this.iid = iid;
		this.aid = aid;
		this.ballot = ballot;
		this.propValue = null;
		this.lastVotingBallot = lastVotingBallot;
		this.lastVotingValue = lastVotingValue;
	}
	
	public Message(MessageType type, long id){
		this.type = type;
		this.pid = id;
	}

	public static Message createClientMessage(byte[] value) {
		return new Message(MessageType.CLIENT, value);
	}

	public static Message createPrepareMessage(long iid, long ballot) {
		return new Message(MessageType.PREPARE, iid, ballot);
	}

	public static Message createPromiseMessage(long aid, long iid, long ballot, long lastBallot, byte[] lastValue) {
		return new Message(MessageType.PROMISE, iid, aid, ballot, lastBallot, lastValue);
	}

	public static Message createAcceptMessage(long iid, long ballot, byte[] proposedValue) {
		return new Message(MessageType.ACCEPT, iid, ballot, proposedValue);
	}

	public static Message createAcceptedMessage(long aid, long iid, long ballot, byte[] value) {
		return new Message(MessageType.ACCEPTED, iid, aid, ballot, value, -1, null);
	}
	
	public static Message createDecisionMessage(long iid, byte[] proposedValue) {
		return new Message(MessageType.DECISION, iid, -1, proposedValue);
	}
	
	public static Message createOkMessage(long proposerId){
		return new Message(MessageType.OK, proposerId );
	}
	
	public static Message createElectionMessage(long proposerId){
		return new Message(MessageType.ELECTION, proposerId );
	}
	
	public static Message createAliveMessage(int leaderId) {
		return new Message(MessageType.ALIVE, leaderId );
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public long getIid() {
		return iid;
	}

	public void setIid(long iid) {
		this.iid = iid;
	}

	public long getAid() {
		return aid;
	}

	public void setAid(long aid) {
		this.aid = aid;
	}

	public long getBallot() {
		return ballot;
	}

	public void setBallot(long ballot) {
		this.ballot = ballot;
	}

	public byte[] getPropValue() {
		return propValue;
	}

	public void setPropValue(byte[] propValue) {
		this.propValue = propValue;
	}

	public long getLastVotingBallot() {
		return lastVotingBallot;
	}

	public void setLastVotingBallot(long lastVotingBallot) {
		this.lastVotingBallot = lastVotingBallot;
	}

	public byte[] getLastVotingValue() {
		return lastVotingValue;
	}

	public void setLastVotingValue(byte[] lastVotingValue) {
		this.lastVotingValue = lastVotingValue;
	}
	
	public long getProposerId() {
		return pid;
	}

	public void setProposerId(long pid) {
		this.pid = pid;
	}

	public byte[] getBytes() throws IOException {
		return convertToBytes(this);
	}

	public static Message getMessage(byte[] b) throws ClassNotFoundException, IOException {
		return (Message) convertFromBytes(b);
	}

	public String toString() {
		return "Message(iid, pid, aid, ballot, type, lastVotingBallot) = (" + this.getIid() + ", " + this.getProposerId()+ ", " + this.getAid() + ", "
				+ this.getBallot() + ", " + this.getType() + ", " + this.getLastVotingBallot() + ")";
	}

	public final boolean equals(Object other) {
		Message m = (Message) other;
		return m.type == this.type && m.iid == this.iid && m.aid == this.aid && m.ballot == this.ballot;
	}

	private byte[] convertToBytes(Object object) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return bos.toByteArray();
		}
	}

	private static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
			return in.readObject();
		}
	}
}
