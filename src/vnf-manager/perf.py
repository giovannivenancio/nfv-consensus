import time
import random

class Performance():

    old_now = time.time()
    num_cons = 0
    total_time = 0
    avg = 0

    def get_time(self):
        """
        Return time in seconds
        """

        return time.time()

    def update_avg(self, cons_time):
        """
        Update time average of Paxos executions
        """

        self.total_time += cons_time
        self.num_cons += 1
        self.avg = self.total_time / self.num_cons
