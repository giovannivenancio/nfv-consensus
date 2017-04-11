import socket

server_address = ('127.0.1.1', 8901)
conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
conn.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
conn.bind(server_address)
conn.listen(1)
connection, client_address = conn.accept()

# print "new conn:", connection, client_address
# lixo = connection.recv(16)
# print lixo
# lixo = connection.recv(16)
# print lixo
# lixo = connection.recv(16)
# print lixo

while True:
    try:
        # http://stupidpythonideas.blogspot.com.br/2013/05/sockets-are-byte-streams-not-message.html
        #size = int(connection.recv(3))
        #print 'size = ', size
        message = connection.recv(192)

        print 'received "%s" len = %d' % (message, len(message))
    except:
        #print "error"
        pass

