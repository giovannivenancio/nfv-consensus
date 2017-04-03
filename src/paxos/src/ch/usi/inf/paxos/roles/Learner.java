package ch.usi.inf.paxos.roles;

import java.util.HashMap;

import ch.usi.inf.paxos.exception.PaxosException;
import ch.usi.inf.paxos.support.ConfigReader;
import ch.usi.inf.paxos.support.ConnectionManager;
import ch.usi.inf.paxos.support.Message;
import ch.usi.inf.paxos.support.MessageListener;
import ch.usi.inf.paxos.support.MessageType;
import ch.usi.inf.paxos.support.ReceivingThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.logging.Logger;

import java.lang.System;

import java.io.*;
import java.net.*;

import java.util.*;
import java.text.*;

public class Learner implements MessageListener {
    private static Logger LOGGER = Logger.getLogger("InfoLogging");
    private final Role myRole = Role.LEARNER;
    private int id, count = 0;
    private long lastIid, maxIid;
    private HashMap<Long, byte[]> deliveryHistory;
    private ReceivingThread rt;
    public PrintWriter outToServer;
    public byte[] send_data;

    public Learner(int id, String configFile) {
        try {
            this.setId(id);
            this.lastIid = this.maxIid = 0;
            deliveryHistory = new HashMap<Long, byte[]>();
            ConfigReader.loadConfiguration(configFile);
            ConnectionManager.initConnection(myRole);
            ConnectionManager.registerListener(MessageType.DECISION, this);
            rt = new ReceivingThread();
            new Thread(rt).start();
        } catch (Exception e) {
            System.out.println("Unable to initialize connection: " + e.toString());
        }
    }

    @Override
    public void messageRecieved(Message msg) throws PaxosException {
        if (msg.getType() == MessageType.DECISION) {
            tryToDeliver(msg);
            //LOGGER.info(new String(msg.getPropValue()));
            //System.out.println(new String(msg.getPropValue()));
        } else
            throw new PaxosException("Unexpected type.");
    }

    private void tryToDeliver(Message msg) {
        deliveryHistory.put(msg.getIid(), msg.getPropValue());
        if (msg.getIid() > this.maxIid) {
            this.maxIid = msg.getIid();
        }
        for (long i = this.lastIid + 1; i <= this.maxIid; i++) {
            byte[] value;
            if ((value = deliveryHistory.get(i)) != null) {
                deliver(value);
                this.lastIid = i;
            } else {
                break;
            }
        }
    }

    private void deliver(byte[] value) {
        System.out.println(new String(value));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
