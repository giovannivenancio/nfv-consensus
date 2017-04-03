package ch.usi.inf.paxos.test;

import ch.usi.inf.paxos.roles.Acceptor;
import ch.usi.inf.paxos.roles.Client;
import ch.usi.inf.paxos.roles.Proposer;
import ch.usi.inf.paxos.roles.Role;
import ch.usi.inf.paxos.support.ConfigReader;
import ch.usi.inf.paxos.support.ConnectionManager;
import ch.usi.inf.paxos.support.Message;
import ch.usi.inf.paxos.support.MessageType;

public class TestConnection {
	private static final String configFile = "paxos.properties";
	
	public static void main(String[] args) throws Exception{
		Role myRole = Role.valueOf(args[0].toUpperCase());
		int id = 0;
		if(args.length > 1l)
			id = Integer.valueOf(args[1]);
		
		Message m;
		
		if(myRole == Role.ACCEPTOR)
		{
			Acceptor ac1 = new Acceptor(id, configFile);
			
		}
		else if(myRole == Role.PROPOSER){
			Proposer proposer = new Proposer(0, configFile);
			
		}else if(myRole == Role.CLIENT){
			Client client = new Client(0, configFile);
		}
		else{
			ConfigReader.loadConfiguration(configFile);
			ConnectionManager.initConnection(myRole);
			
			m = new Message(MessageType.PREPARE, 1, 1, "oi".getBytes());
			ConnectionManager.sendToAcceptor(m);
			System.out.println("sent " + m);
		}
	}

}
