package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;

import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;

class RIPTable {
	void initialize( Router router ) {
		for( Iface iface : router.getInterfaces().values() ) {
			sendRIPRequest( router, iface );
		}
	}

	void sendRIPRequest( Router router, Iface iface ) {
		Ethernet ether = new Ethernet();
		IPv4 ip = new IPv4();
		UDP udp = new UDP();	
		RIPv2 rip = new RIPv2();
		ether.setPayload( ip );
		ip.setPayload( udp );
		udp.setPayload( rip );

		ether.setEtherType( Ethernet.TYPE_IPv4 );
		ether.setDestinationMACAddress( "FF:FF:FF:FF:FF:FF" );
		ether.setSourceMACAddress( iface.getMacAddress().toBytes() );
		//ether.serialize();

		ip.setTtl( (byte)64 ).setProtocol( IPv4.PROTOCOL_UDP );
		ip.setDestinationAddress( IPv4.toIPv4Address( "224.0.0.9" ) ); 
		ip.setSourceAddress( iface.getIpAddress() );	
		//ip.serialize();

		udp.setSourcePort( UDP.RIP_PORT );
		udp.setDestinationPort( UDP.RIP_PORT );
		//udp.serialize();

		rip.setCommand( RIPv2.COMMAND_REQUEST );
		//rip.serialize();
	}

	void sendRIPResponse(){}
}
