import socket
import sys
import time

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('127.0.1.1', 8900)

print >>sys.stderr, 'connecting to %s port %s' % server_address

try:
    sock.connect(server_address)

    message1 = """'{"dpid": 1,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}' # 192.168.0.12"""
    message2 = """'{"dpid": 2,"cookie": 1,"idle_timeout": 0,"hard_timeout": 0,"priority": 32768,"match": {"in_port": 1, "dl_dst": "80:00:00:00:00:01"},"actions": [{"type": "OUTPUT", "port": 2}]}' # 192.168.0.12"""
    l = len(message1) 
    print 'sending "%s"' % message1
    print "tamanho: ", l

    #exit()

    c = 0

    while True:
        sock.send(str(l))
	if c == 0:
	    print message1
	    sock.send(message1)
	    c = 1
	else:
   	    print message2
	    sock.send(message2)
	    c = 0
        time.sleep(0.5)

except:
    pass
finally:
    print 'closing socket'
    sock.close()
