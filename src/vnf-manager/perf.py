import logging
import time
import random
from datetime import datetime

class Performance():

    old_now = time.time()
    num_cons = 0
    total_time = 0
    avg = 0

    def __init__(self, vnf_ip):
        logging.basicConfig(format='%(message)s', filename='/projects/nfv-consensus/src/vnf-manager/vnf_%s.log' % vnf_ip, filemode='w', level=logging.INFO)

    def get_time(self):
        """
        Return time in seconds.
        """

        return time.time()

    def update_avg(self, cons_time):
        """
        Update time average of Paxos executions.
        """

        self.total_time += cons_time
        self.avg = self.total_time / self.num_cons

    def update_stats(self):
        """
        Update quantity of executed consensus.
        """

        self.num_cons += 1
        logging.info(' '.join([datetime.now().strftime('%H:%M:%S'), str(self.num_cons)]))

