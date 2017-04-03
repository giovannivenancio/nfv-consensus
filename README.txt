----------------------------------------------------------------------------------------

Execução:

Terminator: 6 abas, cada um executa um container

Container:
sudo docker run -v ~/projects/nfv-consensus:/nfv-consensus -it nfv-consenso /bin/bash

sudo docker exec -it CONTAINER_ID /bin/bash

+---------------------+--------------------------+--------------------+
|                     |                          |                    |
|                     |                          |                    |
|        Ryu 1        |          Ryu 2           |       Ryu 3        |
|                     |                          |                    |
|                     |                          |                    |
+---------------------------------------------------------------------+
|                     |                          |                    |
|                     |                          |                    |
|      Filter 1       |         Filter 2         |      Filter 3      |
|                     |                          |                    |
|                     |                          |                    |
+---------------------+--------------------------+--------------------+

Ryu:
cd /nfv-consensus/src/controller
ryu-manager controller.py

Filter:
cd /nfv-consensus/src/filter
./filter.py 5000 1 PROPOSER CLIENT ACCEPTOR LEARNER
./filter.py 5000 5 CLIENT ACCEPTOR LEARNER
./filter.py 5000 8 CLIENT ACCEPTOR LEARNER

Para ver as regras:
ctrl-z
cat /tmp/rules

Terminator: 4 abas

+---------------------+--------------------------+--------------------+
|                     |                          |                    |
|                     |                          |                    |
|    Filter Log 1     |        Filter Log 2      |     Filter Log 3   |
|                     |                          |                    |
|                     |                          |                    |
+---------------------+--------------------------+--------------------+
|                                                                     |
|                                                                     |
|                                Mininet                              |
|                                                                     |
|                                                                     |
+---------------------------------------------------------------------+

Filter Log:
tail -f logs/filter_172.17.0.5.log
tail -f logs/filter_172.17.0.6.log
tail -f logs/filter_172.17.0.7.log

Mininet:
sudo ./network.py
