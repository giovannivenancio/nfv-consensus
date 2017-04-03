from __future__ import division

import sys

input = sys.argv[1]

f = open(input, 'r')
data = f.readlines()
f.close()

values = []

for d in data:
    values.append(float(d.split(' ')[1][:-1]))

print sum(values)/len(values)
