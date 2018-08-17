1) Baixar o código fonte

cd ~
mkdir projects; cd projects
git clone git@github.com:giovannivenancio/nfv-consensus.git

2) Instalar os componentes necessários

sudo apt-get install python-pip tmux python-webob python-routes python-tinyrpc python-oslo.config python-msgpack python-eventlet cmake
sudo pip install PTable ovs

Instalando o docker no ubuntu 16
https://www.digitalocean.com/community/tutorials/como-instalar-e-usar-o-docker-no-ubuntu-16-04-pt

Instalando o docker no ubuntu 18
https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-18-04

É importante executar o "step 2" para o docker não precisar executar com sudo

3) Baixar a imagem do container VNF-Consensus

docker pull gvsouza/nfv-consenso

4) Instalando a Mininet (não usamos a Mininet diretamente, mas sim o benchmarking de controladores e outras dependências como o OpenVSwitch)

git clone git://github.com/mininet/mininet
mininet/util/install.sh -a

Testa se a Mininet foi instalada com sucesso
sudo mn --test pingall

5) Instalar o controlador Ryu

git clone git://github.com/osrg/ryu.git

cd ryu
sudo python ./setup.py install

6) Instalar a libPaxos

https://bitbucket.org/sciascid/libpaxos

7) Configurar a HOME

Abra o arquivo nfv-consensus/src/network/network.py e altere a variável HOME_DIR para a sua home. Não remova o diretório 'projects', pois nele estará
todo o código fonte.

6) Executando a VNF-Consensus

cd nfv-consensus/src/network
./network <num_controllers> <num_vnfs>

Por exemplo:

./network 3 3