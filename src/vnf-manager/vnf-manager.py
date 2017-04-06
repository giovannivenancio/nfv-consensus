#!/usr/bin/env python

import datetime
import os
import signal
import socket
import sys
import time
import yaml
from time import gmtime, strftime

class Manager():

    def __init__(self, role_id, roles):
        signal.signal(signal.SIGINT, self.terminate)

        self.ip = self.get_my_ip()

        with open("config.yml", 'r') as ymlfile:
            self.cfg = yaml.load(ymlfile)

        self.domain = self.get_domain()

        # Start TCP Server
        self.conn = self.start_server()

    def start_server(self):
        server_address = ('127.0.0.1', 8900)
        conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        conn.bind(server_address)
        return conn

    def get_my_ip(self):
        f = os.popen('ifconfig eth0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1')
        ip = f.read()
        return ip[:-1]

    def get_domain(self):
        """
        From domain file, get which controllers
        this VNF should manage. This is represented
        by a range of consecutive numbers (controllers id).
        Example:
            domain = [2, 5] =>
            domain = [172.17.0.2, 172.17.0.3, 172.17.0.4, 172.17.0.5]
        """

        vnf_domain = []

        with open("../network/domain", 'r') as f:
            domain = f.readlines()
            for d in domain:
                vnf, controller = d[:-1].split(' ')

                if vnf == self.ip:
                    host = int(controller.split('.')[-1])
                    vnf_domain.append(host)

        return range(min(vnf_domain), max(vnf_domain) + 1)

    def terminate(self, signal, frame):
        """
        On Keyboard Interrupt, kill remaining processes
        """

        os.system("pkill -f vnf-manager")
        os.system("pkill -f java")
        exit(0)

    def handle_request(self, message):
        """
        If sender is on domain's range, install rule.
        Otherwise, just store the rule.
        """

        rule, host = message.split('#')
        print rule
        print sender

        if int(host) in self.domain:
            print "instalo regra"
            #request = "curl -X POST -d %s http://172.17.0.2:8080/stats/flowentry/add" % rule[:-1]
            #os.system(request) # substituir por subprocess (popen)

        print rule

    def mainloop(self):
        """
        Keep waiting for consensus requests
        """

        i = 0
        while True:
            print "ok... %d" % i
            time.sleep(1)
            i+= 1

        while True:
            connection, client_address = self.conn.accept()
            try:
                # Receive the data in small chunks and retransmit it
                # http://stupidpythonideas.blogspot.com.br/2013/05/sockets-are-byte-streams-not-message.html
                size = int(connection.recv(3))
                message = connection.recv(size)

                print 'received "%s"' % message
                self.handle_request(message)

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print 'usage: %s <start_id> [LEARNER] [ACCEPTOR] [PROPOSER] [CLIENT]' % sys.argv[0]
        exit(1)

    os.system("pkill -f java")

    manager = Manager(int(sys.argv[1]), sys.argv[2:])

    print "*** Paxos is running"
    print "- IP: %s"  % manager.ip
    print "- Domain: %s" % manager.domain
    print "- Roles: %s\n" % ' '.join(sys.argv[2:])

    manager.mainloop()
