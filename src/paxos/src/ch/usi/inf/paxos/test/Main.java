package ch.usi.inf.paxos.test;

import ch.usi.inf.paxos.roles.Acceptor;
import ch.usi.inf.paxos.roles.Client;
import ch.usi.inf.paxos.roles.Learner;
import ch.usi.inf.paxos.roles.Proposer;
import ch.usi.inf.paxos.roles.Role;

public class Main {

	public static void main(String[] args) {
		Role r;
		int id;
		String conf;
		try {
			r = Role.valueOf(args[0].toUpperCase());
			id = Integer.parseInt(args[1]);
			conf = args[2];

			switch (r) {
			case ACCEPTOR:
				new Acceptor(id, conf);
				break;
			case PROPOSER:
				new Proposer(id, conf);
				break;
			case LEARNER:
				new Learner(id, conf);
				break;
			case CLIENT:
				new Client(id, conf);
				break;
			default:
				throw new Exception("Invalid Role");
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			usage();
			System.exit(1);
		}
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("\tjava test.paxos.ds.inf.usi.ch.Main <role> <id> <config-file>");

	}

}
