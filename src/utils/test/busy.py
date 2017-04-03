# import requests

# url = 'http://172.17.0.2:8080/stats/flowentry/add'
# data = '{"dpid": 1,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}'
# response = requests.post(url, data=data)

# #print response

import os

rule = '\'{"dpid": 1,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}\''
request = "curl -X POST -d %s http://172.17.0.2:8080/stats/flowentry/add" % rule

while True:
    os.system(request)
