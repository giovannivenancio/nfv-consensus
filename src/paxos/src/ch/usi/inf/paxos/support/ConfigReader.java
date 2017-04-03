package ch.usi.inf.paxos.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import ch.usi.inf.paxos.exception.PaxosException;

public class ConfigReader {
	
	private static Properties p = null;
	
	private static String configFile = null;
	
	public static void loadConfiguration(String configFile) throws PaxosException
	{
		if(p == null){
			p = new Properties();
			ConfigReader.configFile = configFile;
			try {
				FileInputStream in = new FileInputStream(configFile);
				p.load(in);
				in.close();
			} catch (IOException e) {
				throw new PaxosException("Error reading configuration file: " + e.getMessage());
			}
		
			try{
				Logger.setLevel(getProperty("LOG_LEVEL"));
			}
			catch(PaxosException e)
			{
				Logger.setLevel("INFO");
			}
		}
	}
	
	private static String getProperty(String prop) throws PaxosException
	{
		loadConfiguration(configFile);
		if(p.getProperty(prop) == null)
		{
			throw new PaxosException("Property " + prop + " not set in configuration file");
		}
		return p.getProperty(prop);
	
	}

	public static int getQuorum() throws PaxosException
	{
		return (Integer.parseInt(getProperty("NUM_ACCEPTORS"))/2 + 1);
	}
	
	public static InetAddress getClientIP() throws PaxosException, IOException
	{
		return InetAddress.getByName(getProperty("CLIENTS").split(":")[0]);
	}
	
	public static int getClientPort() throws PaxosException
	{
		return Integer.parseInt(p.getProperty("CLIENTS").split(":")[1]);
	}
	
	public static InetAddress getProposerIP() throws IOException, PaxosException
	{
		return InetAddress.getByName(getProperty("PROPOSERS").split(":")[0]);
	}
	
	public static int getProposerPort() throws PaxosException
	{
		return Integer.parseInt(getProperty("PROPOSERS").split(":")[1]);
	}
	
	public static InetAddress getAcceptorIP() throws IOException, PaxosException
	{
		return InetAddress.getByName(getProperty("ACCEPTORS").split(":")[0]);
	}
	
	public static int getAcceptorPort() throws PaxosException
	{
		return Integer.parseInt(getProperty("ACCEPTORS").split(":")[1]);
	}
	
	public static InetAddress getLearnerIP() throws IOException, PaxosException
	{
		return InetAddress.getByName(getProperty("LEARNERS").split(":")[0]);
	}
	
	public static int getLearnerPort() throws PaxosException
	{
		return Integer.parseInt(getProperty("LEARNERS").split(":")[1]);
	}

	public static int getMaxMessageSize() throws PaxosException 
	{	
		return Integer.parseInt(getProperty("MAX_MSG_SIZE"));
	}
	
	public static int getTimeout() throws PaxosException
	{
		return Integer.parseInt(getProperty("TIMEOUT"));
	}
}
