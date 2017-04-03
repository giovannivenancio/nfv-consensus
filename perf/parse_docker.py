#!/usr/bin/env python

from __future__ import division

import sys
import re

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print 'usage: %s <input_file>' % sys.argv[0]
        exit(1)

    input_file = sys.argv[1]

    p = []; stats = []

    with open(input_file) as f:
        data = f.readlines()

        for d in data:
            if d.startswith('CONTAINER'):
                continue
            d = re.sub(' +',' ', d)
            if d.endswith('\n'):
                d = d[:-1]

            d = d.split(' ')
            d = d[:-1]
            for c in ['/', 'GiB', 'KiB', 'MiB', 'kB', 'B', 'MB', '7.779']:
                if c in d:
                    d = filter(lambda a: a != c, d)

            stats.append([d[0], d[1], d[3]])

    perf = {}

    for item in stats:
        if item[0] not in perf:
            perf[item[0]] = {
                'cpu': [],
                'mem': []
            }
        cpu = str(float(item[1].replace('%', ''))/4)
        mem = item[2].replace('%', '')
        perf[item[0]]['cpu'].append(cpu)
        perf[item[0]]['mem'].append(mem)

    for container_id in perf:
        f_mem = open('docker_stats/%s_mem.dat' % container_id, 'w')
        f_cpu = open('docker_stats/%s_cpu.dat' % container_id, 'w')

        i = 0
        for mem in perf[container_id]['mem']:
            line = "%s %s\n" % (str(i), mem)
            f_mem.write(line)
            i += 1

            if i > 60:
                break

        i = 0
        for cpu in perf[container_id]['cpu']:
            line = "%s %s\n" % (str(i), cpu)
            f_cpu.write(line)
            i += 1

            if i > 60:
                break

        f_mem.close()
        f_cpu.close()
