package ch.usi.inf.paxos.support;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import ch.usi.inf.paxos.exception.PaxosException;
import ch.usi.inf.paxos.roles.Role;

public class ConnectionManager {

	private static DatagramSocket clientSocket = null;
	private static DatagramSocket proposerSocket = null;
	private static DatagramSocket acceptorSocket = null;
	private static DatagramSocket learnerSocket = null;

	private static MulticastSocket mySocket = null;
	private static InetAddress myAddress = null;
	private static int myPort = -1;

	private static Map<MessageType, MessageListener> listeners = null;

	public static void initConnection(Role r) throws SocketException, IOException, PaxosException {
		Logger.logInfo("Starting connection as " + r.toString());
		listeners = new HashMap<MessageType, MessageListener>();
		clientSocket = new DatagramSocket();
		clientSocket.setReuseAddress(true);
		clientSocket.connect(ConfigReader.getClientIP(), ConfigReader.getClientPort());
		proposerSocket = new DatagramSocket();
		proposerSocket.setReuseAddress(true);
		proposerSocket.connect(ConfigReader.getProposerIP(), ConfigReader.getProposerPort());
		acceptorSocket = new DatagramSocket();
		acceptorSocket.setReuseAddress(true);
		acceptorSocket.connect(ConfigReader.getAcceptorIP(), ConfigReader.getAcceptorPort());
		learnerSocket = new DatagramSocket();
		learnerSocket.setReuseAddress(true);
		learnerSocket.connect(ConfigReader.getLearnerIP(), ConfigReader.getLearnerPort());
		switch (r) {
		case CLIENT:
			myPort = ConfigReader.getClientPort();
			myAddress = ConfigReader.getClientIP();
			break;
		case PROPOSER:
			myPort = ConfigReader.getProposerPort();
			myAddress = ConfigReader.getProposerIP();
			break;
		case ACCEPTOR:
			myPort = ConfigReader.getAcceptorPort();
			myAddress = ConfigReader.getAcceptorIP();
			break;
		case LEARNER:
			myPort = ConfigReader.getLearnerPort();
			myAddress = ConfigReader.getLearnerIP();
			break;
		}
		mySocket = new MulticastSocket(myPort);
		mySocket.setReuseAddress(true);
		mySocket.joinGroup(myAddress);

	}

	static void endConnection() throws IOException {
		mySocket.leaveGroup(myAddress);
		mySocket.close();
		clientSocket.close();
		proposerSocket.close();
		acceptorSocket.close();
		learnerSocket.close();
	}

	public static void sendToClient(Message m) throws IOException, PaxosException {
		byte[] msg = m.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length);
		packet.setAddress(ConfigReader.getClientIP());
		packet.setPort(ConfigReader.getClientPort());
		clientSocket.send(packet);
	}

	public static void sendToProposer(Message m) throws IOException, PaxosException {
		byte[] msg = m.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length);
		packet.setAddress(ConfigReader.getProposerIP());
		packet.setPort(ConfigReader.getProposerPort());
		proposerSocket.send(packet);
	}

	public static void sendToAcceptor(Message m) throws IOException, PaxosException {
		byte[] msg = m.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length);
		packet.setAddress(ConfigReader.getAcceptorIP());
		packet.setPort(ConfigReader.getAcceptorPort());
		acceptorSocket.send(packet);

	}

	public static void sendToLearner(Message m) throws IOException, PaxosException {
		byte[] msg = m.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length);
		packet.setAddress(ConfigReader.getLearnerIP());
		packet.setPort(ConfigReader.getLearnerPort());
		learnerSocket.send(packet);

	}

	public static Message receiveMessage() throws PaxosException, IOException, ClassNotFoundException {
		byte[] msgBytes = new byte[ConfigReader.getMaxMessageSize()];
		Message msg;
		DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length);
		mySocket.receive(packet);
		msg = Message.getMessage(msgBytes);
		Logger.logDebug("Received new message: " + msg);
		return msg;
	}

	public static void registerListener(MessageType type, MessageListener ml) {
		listeners.put(type, ml);
	}

	public static MessageListener getListener(MessageType type) {
		return listeners.get(type);
	}
}
