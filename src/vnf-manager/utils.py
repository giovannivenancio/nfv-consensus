#!/usr/bin/env python

import subprocess
from yaml import load

class Utils():

    def read_yaml(self):
        """
        Read YAML config file
        """

        stream = file('config.yaml', 'r')
        return load(stream)

    def prepare_java_cmd(self, role, role_id, config, consensus_config):
        """
        Build a java command to instantiate a role
        """

        cmd = config[role].replace("bin", config['system_path'] + "bin")
        cmd = ' '.join([cmd, role_id, consensus_config])
        cmd = cmd.split(' ')

        return cmd

    def instantiate_role(self, *args):
        """
        Insantiate a role process
        """

        java_cmd = self.prepare_java_cmd(*args)

        # Only create pipes that communicate with parent process
        if args[0] in ['client', 'learner']:
            return subprocess.Popen(java_cmd,
                                    stdin=subprocess.PIPE,
                                    stdout=subprocess.PIPE)
        else: return subprocess.Popen(java_cmd)
        #return subprocess.Popen(java_cmd)
