import socket

server_address = ('127.0.1.1', 8901)
conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
conn.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
conn.bind(server_address)
conn.listen(1)
connection, client_address = conn.accept()

while True:
    message = connection.recv(192)
    print message
