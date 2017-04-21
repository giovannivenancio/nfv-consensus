f = open('file.dat', 'r')
g = open('vnf_no_overhead.dat', 'w')
values = f.readlines()
i = 1
for v in values:
    print v.split(' ')[1][:-1]
    g.write(str(i) + ' ' + v.split(' ')[1][:-1] + '\n')
    i += 1
f.close()
g.close()
