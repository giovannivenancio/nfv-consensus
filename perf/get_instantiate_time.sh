#!/bin/bash

for i in {1..10}
do
   time docker run -v ~/projects:/projects -it gvsouza/nfv-consenso /bin/bash -c "cd /projects/nfv-consensus-controller/src/controller; ryu-manager controller.py cbench.py ofctl_rest.py"
done

echo "--------------------------------------"

for i in {1..10}
do
   time docker run -v ~/projects:/projects -it gvsouza/nfv-consenso /bin/bash -c "cd /projects/nfv-consensus/src/filter; pkill -f java; ./filter.py 1 5000 1 CLIENT LEARNER"
done
