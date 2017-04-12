#!/usr/bin/env python

import datetime
import os
import requests
import signal
import socket
import time
import yaml
from subprocess import Popen
from time import gmtime, strftime
from perf import Performance

class Manager():

    def __init__(self):
        signal.signal(signal.SIGINT, self.terminate)

        self.roles = []
        self.ip = self.get_my_ip()

        with open("/projects/nfv-consensus/src/vnf-manager/config.yml", 'r') as ymlfile:
            self.cfg = yaml.load(ymlfile)
            self.paxos_path = self.cfg['config']
            self.paxos_conf = self.paxos_path + 'paxos.conf'

        self.domain = self.get_domain()

        # Start TCP Server
        self.conn = self.start_server()

        # Execute paxos roles
        self.execute_roles()

    def start_server(self):
        """
        Start TCP server to receive learned rules from libpaxos Learner.
        """

        server_address = ('127.0.1.1', 8901)
        conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        conn.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        conn.bind(server_address)
        conn.listen(1)
        return conn

    def get_my_ip(self):
        """
        Read VNF-Manager IP.
        """

        f = os.popen('ifconfig eth0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1')
        ip = f.read()
        return ip[:-1]

    def run(self, cmd):
        """
        Execute bash commands
        """

        fh = open("/tmp/NUL", "w")
        Popen(cmd, stdin=None, stdout=fh, stderr=fh)
        fh.close()

    def execute_roles(self):
        """
        Execute each Paxos role.
        """

        with open('/projects/nfv-consensus/src/paxos/vnf-paxos/paxos.conf', 'r') as f:
            lines = f.readlines()

            conf = []
            for line in lines:
                if '172.17.0' in line:
                    conf.append(line[:-1])

            proposer = conf[0].split(' ')[2]
            acceptors = conf[1:]

            for acceptor in acceptors:
                acceptor_id, acceptor_ip = acceptor.split(' ')[1:3]

                if acceptor_ip == self.ip:
                    acceptor_id = str(acceptor_id)
                    break

        if self.ip == proposer:
            self.roles.append('PROPOSER')
            self.run([self.paxos_path + 'proposer', '0', self.paxos_conf])

        self.roles.append('ACCEPTOR')
        self.run([self.paxos_path + 'acceptor', acceptor_id, self.paxos_conf])

        self.roles.append('LEARNER')
        self.run([self.paxos_path + 'learner-paxos-vnf', self.paxos_conf])

        # Wait until proposer, acceptor and learner are stabilized
        time.sleep(1)

        self.roles.append('CLIENT')
        self.run([self.paxos_path + 'client-paxos-vnf', self.paxos_conf])

    def get_domain(self):
        """
        From domain file, get which controllers
        this VNF should manage. This is represented
        by a range of consecutive numbers (controllers id).
        Example:
            domain = [172.17.0.2, 172.17.0.3, 172.17.0.4, 172.17.0.5] =>
            domain = [2, 5]
        """

        vnf_domain = []

        with open("/projects/nfv-consensus/src/network/domain", 'r') as f:
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

        os.system('pkill -f vnf-manager')
        os.system('pkill -f proposer')
        os.system('pkill -f acceptor')
        os.system('pkill -f client-paxos-vnf')
        os.system('pkill -f learner-paxos-vnf')
        exit(0)

    def handle_request(self, message):
        """
        If sender is on domains range, the rule should be installed.
        Otherwise, just store the rule.
        """

        rule, host = message.split('#')

        host = int(host)
        if host in self.domain:
            url = 'http://172.17.0.%s:8080/stats/flowentry/add' % str(host)
            requests.post(url, data=rule)

    def mainloop(self):
        """
        Keep waiting for rules learned by libpaxos Learner.
        """

        perf = Performance()

        connection, client_address = self.conn.accept()

        perf.old_now = perf.get_time()
        while True:
            message = connection.recv(179)
            self.handle_request(message)

            now = perf.get_time()
            cons_time = now - perf.old_now
            perf.old_now = now

            perf.update_avg(cons_time)

            print "time = %s s | avg = %s s" % (cons_time, perf.avg)

if __name__ == '__main__':

    manager = Manager()

    print "\n*** Paxos is running"
    print "- IP: %s"  % manager.ip
    print "- Domain: %s" % manager.domain
    print "- Roles: %s\n" % ' '.join(manager.roles)

    manager.mainloop()
