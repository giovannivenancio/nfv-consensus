import os

rule = '\'{"dpid": 1,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}\''
r1 = "curl -X POST -d %s http://172.17.0.2:8080/stats/flowentry/add" % rule
r2 = "curl -X POST -d %s http://172.17.0.3:8080/stats/flowentry/add" % rule
r3 = "curl -X POST -d %s http://172.17.0.4:8080/stats/flowentry/add" % rule

while True:
    os.system(r1)
    os.system(r2)
    os.system(r3)
