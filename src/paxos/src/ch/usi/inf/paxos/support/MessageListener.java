package ch.usi.inf.paxos.support;

import ch.usi.inf.paxos.exception.PaxosException;

public interface MessageListener {
	public void messageRecieved(Message msg) throws PaxosException;
}
