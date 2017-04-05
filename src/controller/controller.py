#!/usr/bin/env python

import os
import socket
import time

from time import strftime
from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_0
from ryu.lib.mac import haddr_to_bin
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.lib.packet import ether_types

class ConsensusSwitch(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_0.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(ConsensusSwitch, self).__init__(*args, **kwargs)
        self.mac_to_port = {}
        self.pkts = 0

        self.ip = self.get_my_ip()

        server_address = ('localhost', 10000)
        self.conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # while True:
        #     try:
        #         #print "Trying to connect with Paxos Client on %s:%d" % server_address
        #         self.conn.connect(server_address)
        #         break
        #     except:
        #         time.sleep(1)

    def get_my_ip(self):
        f = os.popen('ifconfig enp2s0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1')
        ip = f.read()
        return ip[:-1]

    def build_rule(self, datapath, in_port, out_port, dst):
        ofproto = datapath.ofproto

        rule = [
            '\'{"dpid": %d,' % datapath.id,
            '"cookie": 1,',
            '"idle_timeout": 0,',
            '"hard_timeout": 0,',
            '"priority": %d,' % ofproto.OFP_DEFAULT_PRIORITY,
            '"match": {"in_port": %d, "dl_dst": \"%s\"},' % (in_port, dst),
            '"actions": [{"type": "OUTPUT", "port": %s}]' % str(out_port),
            '}\''
        ]

        return ''.join(rule)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def _packet_in_handler(self, ev):
        msg = ev.msg
        datapath = msg.datapath
        ofproto = datapath.ofproto

        pkt = packet.Packet(msg.data)
        eth = pkt.get_protocol(ethernet.ethernet)

        dst = eth.dst
        src = eth.src
        dpid = datapath.id

        self.mac_to_port.setdefault(dpid, {})

        # learn a mac address to avoid FLOOD next time.
        self.mac_to_port[dpid][src] = msg.in_port

        if dst in self.mac_to_port[dpid]:
            out_port = self.mac_to_port[dpid][dst]
        else:
            out_port = ofproto.OFPP_FLOOD

        actions = [datapath.ofproto_parser.OFPActionOutput(out_port)]

        if out_port != ofproto.OFPP_FLOOD:
            message = self.build_rule(datapath, msg.in_port, out_port, dst)
            message += ' # ' + self.ip
            print message
            print len(message)
            # Send request for consensus
            #self.conn.sendall(message)

            # Debug only
            #self.pkts += 1
            #print "recebi ", self.pkts

        data = None
        if msg.buffer_id == ofproto.OFP_NO_BUFFER:
            data = msg.data

        out = datapath.ofproto_parser.OFPPacketOut(
            datapath=datapath, buffer_id=msg.buffer_id, in_port=msg.in_port,
            actions=actions, data=data)

        datapath.send_msg(out)

        return

    @set_ev_cls(ofp_event.EventOFPPortStatus, MAIN_DISPATCHER)
    def _port_status_handler(self, ev):
        msg = ev.msg
        reason = msg.reason
        port_no = msg.desc.port_no

        ofproto = msg.datapath.ofproto
        if reason == ofproto.OFPPR_ADD:
            self.logger.info("port added %s", port_no)
        elif reason == ofproto.OFPPR_DELETE:
            self.logger.info("port deleted %s", port_no)
        elif reason == ofproto.OFPPR_MODIFY:
            self.logger.info("port modified %s", port_no)
        else:
            self.logger.info("Illeagal port state %s %s", port_no, reason)
