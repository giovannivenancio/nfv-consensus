#!/usr/bin/env python

import os
import logging
import signal
import subprocess
import sys
import time
from subprocess import Popen
from prettytable import PrettyTable

class Network():

    def __init__(self, num_controllers, num_vnfs):
        signal.signal(signal.SIGINT, self.stop_network)
        logging.basicConfig(format='%(message)s', filename='network.log', filemode='w', level=logging.DEBUG)

        self.num_ctls = int(num_controllers)
        self.num_vnfs = int(num_vnfs)

        ip_base = "172.17.0."
        ctl_host_base = 2
        vnf_host_base = self.num_ctls + 2

        self.controllers = [ip_base + str(ctl_host_base + i) for i in range(self.num_ctls)]
        self.vnfs = [ip_base + str(vnf_host_base + i) for i in range(self.num_vnfs)]
        self.create_paxos_conf()
        self.domain = self.build_domain()

        self.cleanup()

    def create_paxos_conf(self):
        """
        Write on paxos.conf correct IP Address for proposers and acceptors
        """

        with open('../paxos/vnf-paxos/paxos.conf.example', 'r') as f:
            data = f.read()

        with open('../paxos/vnf-paxos/paxos.conf', 'w') as f:
            f.write(data)
            f.write(' '.join(['proposer', '0', self.vnfs[0], '5550\n']))

            i = 0
            for vnf in self.vnfs:
                f.write(' '.join(['acceptor', str(i), vnf, str(8800 + i) + '\n']))
                i += 1

    def build_domain(self):
        """
        Divide controllers into groups and assign these groups to VNFs.
        """

        domain = {}
        num_conn, remainder_conn = divmod(self.num_ctls, self.num_vnfs)

        sub_domains = [
            self.controllers[
                i * num_conn + min(i, remainder_conn) :
                (i + 1) * num_conn + min(i + 1, remainder_conn)
            ] for i in xrange(self.num_vnfs)
        ]

        i = 0
        for s in sub_domains:
            domain[self.vnfs[i]] = s
            i += 1

        return domain

    def print_domain(self):
        domain = open('domain', 'w')
        table = PrettyTable(['VNFs', 'Controllers'])

        for d in self.domain:
            for ctl in self.domain[d]:
                table.add_row([d, ctl])
                domain.write(' '.join([d, ctl]) + '\n')

        domain.close()
        logging.info(str(table))

    def run_controllers(self):
        """
        Create a docker container for each controller and execute them.
        """

        for ctl in self.controllers:
            logging.info("Running controller on %s" % ctl)

            cmd = ['docker', 'run',
                '-v', '/home/gvsouza/projects:/projects',
                '-it', 'gvsouza/nfv-consenso',
                '/bin/bash', '-c',
                '/projects/nfv-consensus/src/controller/controller.sh'
            ]

            self.run(cmd)
            time.sleep(2)

    def run_paxos(self):
        """
        Create a docker container for each VNF-Paxos and execute them.
        For debug purposes, each VNF is executing on a separate tmux pane.
        """

        self.run(['tmux', 'new-session', '-d', '-s', 'paxos'])
        self.run(['tmux', 'new-window', '-t', 'paxos'])

        if self.num_vnfs > 3:
            tmux_sessions = 2
        else:
            tmux_sessions = self.num_vnfs - 1

        for i in range(tmux_sessions):
            self.run(['tmux', 'split'])
            self.run(['tmux', 'select-layout', 'even-vertical'])

        i = 0
        tmux_active_sessions = 0
        for vnf in self.vnfs:
            cmd = ['docker', 'run',
                '-v', '/home/gvsouza/projects:/projects',
                '-it', 'gvsouza/nfv-consenso',
                '/bin/bash', '-c',
                '/projects/nfv-consensus/src/vnf-manager/vnf-manager.sh'
            ]

            logging.info("Running VNF-Paxos on %s" % vnf)

            if tmux_active_sessions < 3:
                cmd = ' '.join(cmd)
                self.run(['tmux', 'send-keys', '-t', str(i), cmd, 'C-m'])
                tmux_active_sessions += 1
            else:
                self.run(cmd)

            i += 1

        self.run(['tmux', 'selectp', '-t', '0'])
        self.run(['tmux', 'attach-session', '-t', 'paxos'])

    def run_cbench(self):
        """
        Execute a cbench client for each controller.
        """

        for ctl in self.controllers:
            logging.info("Running cbench on %s" % ctl)

            cmd = ['cbench',
                '-c', ctl,
                '-p', '6653',
                '-s', '1',
                '-l', '600',
                '-t'
            ]

            self.run(cmd)

    def sleep(self):
        time.sleep(1000000000)

    def stop_network(self, signal, frame):
        self.cleanup()
        logging.info("\n*** Done")
        exit(0)

    def cleanup(self):
        """
        Kill remaining processes from previous executions
        """

        logging.info("\n*** Stopping containers")
        os.system('docker stop $(docker ps -a -q) > /dev/null 2>&1')
        os.system('docker rm $(docker ps -a -q) > /dev/null 2>&1')

        logging.info("*** Stopping remaining processes")
        cmds = [
            ['pkill', '-f', 'tmux'],
            ['pkill', '-f', 'cbench']
        ]

        for cmd in cmds:
            subprocess.call(cmd)

    def run(self, cmd):
        """
        Execute bash commands
        """

        fh = open("/tmp/NUL", "w")
        Popen(cmd, stdin=None, stdout=fh, stderr=fh)
        fh.close()

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "Usage: %s <num_controllers> <num_vnfs>" % sys.argv[0]
        exit(1)

    num_controllers = sys.argv[1]
    num_vnfs = sys.argv[2]

    net = Network(num_controllers, num_vnfs)

    logging.info("\n*** Creating domain file\n")
    net.print_domain()

    logging.info("\n*** Starting [%d] controllers\n" % net.num_ctls)
    net.run_controllers()

    logging.info("\n*** Starting [%d] VNF-Paxos\n" % net.num_vnfs)
    net.run_paxos()

    logging.info("\n")
    for i in range(9, -1, -1):
        logging.info("Loading... %s" % str(i + 1))
        time.sleep(1)

    logging.info("\n*** Starting [%d] cbench clients\n" % net.num_ctls)
    net.run_cbench()

    logging.info("\n*** Network is Online")
    net.sleep()
