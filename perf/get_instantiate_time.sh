#!/bin/bash

for i in {1..10}
do
   time docker run -v ~/projects:/projects -it gvsouza/nfv-consenso /bin/bash -c "/projects/nfv-consensus/src/controller/controller.sh"
done

echo "--------------------------------------"

for i in {1..10}
do
   time docker run -v ~/projects:/projects -it gvsouza/nfv-consenso /bin/bash -c "/projects/nfv-consensus/src/vnf-manager/vnf-manager.sh"
done
