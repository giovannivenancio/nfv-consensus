import socket
import sys
import time

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('127.0.1.1', 8900)
#server_address = ('172.17.0.5', 8900)

print >>sys.stderr, 'connecting to %s port %s' % server_address

sock.connect(server_address)
message = """'{"dpid": 1,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}' # 192.168.0.12"""
l = len(message)
print 'sending "%s"' % message
print "tamanho: ", l

#exit()

while True:
    sock.send(str(l))
    sock.send(message)
    time.sleep(0.1)