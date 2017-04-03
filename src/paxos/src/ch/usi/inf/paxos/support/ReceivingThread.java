package ch.usi.inf.paxos.support;

import ch.usi.inf.paxos.exception.PaxosException;

public class ReceivingThread implements Runnable {

	public ReceivingThread() {
	}

	@Override
	public void run() {

		try {
			Message m;
			while (true) {
				//System.out.println(" ready to receive");
				m = ConnectionManager.receiveMessage();
				//System.out.println("received " + m.getType());
				onReceipt(m);
			}
		} catch (Exception e) {
			System.out.println("Error while receiving messages: " + e.toString());
		}
	}

	private void onReceipt(Message msg) throws PaxosException {
		MessageListener listener = ConnectionManager.getListener(msg.getType());
		if (listener != null)
			listener.messageRecieved(msg);
		else
			throw new PaxosException("No listener for message of type " + msg.getType());
	}

}
