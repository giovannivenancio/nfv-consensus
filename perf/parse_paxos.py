#!/usr/bin/env python

import sys

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print 'usage: %s <input_file>' % sys.argv[0]
        exit(1)

    input_file = sys.argv[1]

    parse1 = []
    parse2 = []

    result = {}

    with open(input_file) as f:
        data = f.readlines()

        for d in data:
            if d.endswith('\n'):
                parse1.append(d[:-1])
            else:
                parse1.append(d)

        for p in parse1:
            time, n_cons = p.split(' ')


            n_cons = int(n_cons)

            if time not in result:
                result[time] = [n_cons]
            else:
                result[time].append(n_cons)

f = open('file.dat', 'w')
i = 0

for r in sorted(result.keys()):
    diff = max(result[r]) - min(result[r])
    print "%s : %d" % (r, diff)
    f.write(str(i) + ' ' + str(diff) + '\n')
    i += 1

    # if i > 120:
    #     break

f.close()
