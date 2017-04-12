# NFV-Consensus

...

## Prerequisites

On-The-Fly NFV tool is built upon Ryu Controller, ...

* [Ryu](https://osrg.github.io/ryu/) - OpenFlow Controller
* [Mininet](http://mininet.org/) - Virtual Network
* [MongoDB](https://www.mongodb.com/) - Database

## Installing

After installing the prerequisites, follow these steps to install ...

```
# Install Ryu Controller
git clone git://github.com/osrg/ryu.git
cd ryu; sudo python setup.py install; cd ..

# Clone On-The-Fly NFV source code.
git clone https://github.com/giovannivenancio/otfnfv.git

# Change to installation directory.
cd ./otfnfv/install/

# Execute 'setup.py' and 'install_packages.sh' script.
# These scripts will install necessary packages and libraries.
sudo ./setup.py install
sudo ./install_packages.sh

# Edit 'otfnfv.conf.example' file and replace "remote_host" and
# both "path" information.
vim otfnfv.conf.example

# Execute 'config.sh' script. This script will get information
# about some paths and will create on system some required files.
sudo ./config.sh

# Execute 'mongo_install.sh' script. This script will install
# MongoDB database.
sudo ./mongo_install.sh

# Execute 'create_db.py' script. This script will create
# and populate the database.
./create_db.py

# Finally, edit '/etc/mongod.conf' file and replace "bindIp: 127.0.0.1"
# with "bindIp: 0.0.0.0". This is required because there is a
# communication between host (where MongoDB Server is running)
# and Mininet host (where MongoDB Client is running).
sudo vim /etc/mongod.conf
sudo service mongod restart
```

After that, the tool should be running properly.
To run:

```
sudo otfnfv
```

## Built With

* [Python](https://www.python.org/)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
