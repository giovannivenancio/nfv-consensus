package ch.usi.inf.paxos.roles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.Writer;

import java.util.logging.Logger;

import java.io.PrintWriter;
import java.io.FileWriter;

import java.lang.System;

import java.util.concurrent.TimeUnit;


import java.net.*;

import ch.usi.inf.paxos.support.ConfigReader;
import ch.usi.inf.paxos.support.ConnectionManager;
import ch.usi.inf.paxos.support.Message;

import java.util.*;
import java.text.*;

public class Client implements Runnable {
    private static Logger LOGGER = Logger.getLogger("InfoLogging");
    private final Role myRole = Role.CLIENT;
    private int id;
    //public ServerSocket Server;
    public DatagramSocket Server;
    public String fromclient;
    public String toclient;

    public Client(int id, String configFile) {
        //System.out.println("TCPServer Waiting for client on port 6000");
        try {
            this.id = id;

            if (this.id == 1) {
                //this.Server = new ServerSocket (6500);
                this.Server = new DatagramSocket(6500);
            }
/*            else if (this.id == 2) {
                //this.Server = new ServerSocket (6501);
                this.Server = new DatagramSocket(6501);
            }
            else if (this.id == 3) {
                //this.Server = new ServerSocket (6502);
                this.Server = new DatagramSocket(6502);
            }*/

            ConfigReader.loadConfiguration(configFile);
            ConnectionManager.initConnection(myRole);
            new Thread(this).start();
        } catch (Exception e) {
            System.out.println("Unable to initialize connection: " + e.toString());
        }
    }

    @Override
    public void run() {
        try {
            Integer num = 0;
            String input;
            //Socket connected = Server.accept();
            //BufferedReader inFromClient =
            //new BufferedReader(new InputStreamReader (connected.getInputStream()));

            byte[] receive_data = new byte[1024];

            while (true) {
                DatagramPacket receive_packet = new DatagramPacket(
                    receive_data, receive_data.length);

                this.Server.receive(receive_packet);

                String data = new String(
                    receive_packet.getData(), 0, 0, receive_packet.getLength());

                broadcast(data);
            }

        } catch (IOException e) {
            System.out.println("Could not read from standard input: " + e.getMessage());
        }
    }

    private void broadcast(String value) {
        try {
            //LOGGER.info("Client " + this.id + ");
            //LOGGER.info("enviando " + value);
            ConnectionManager.sendToProposer(Message.createClientMessage(value.getBytes()));
            TimeUnit.MILLISECONDS.sleep(15);
        } catch (Exception e) {
            System.out.println("Could not send message to leader: " + e.getMessage());
        }
    }

}
