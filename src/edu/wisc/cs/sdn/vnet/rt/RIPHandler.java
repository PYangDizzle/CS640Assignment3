package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;

import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;

import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.LinkedList;

class RIPHandler {

	Timer timer = new Timer();

	void initialize( Router router ) {
		for( Iface iface : router.getInterfaces().values() ) {
			sendRequest( router, iface );
		}
		timer.scheduleAtFixedRate( new TimerTask() {
			@override
			void run() {
				sendResponsesAll( router );	
			}
		}, 0, 10000); // 10 sec
	}

	void sendRequest( Router router, Iface iface ) {
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

		ip.setTtl( (byte)64 ).setProtocol( IPv4.PROTOCOL_UDP );
		ip.setDestinationAddress( IPv4.toIPv4Address( "224.0.0.9" ) ); 
		ip.setSourceAddress( iface.getIpAddress() );	

		udp.setSourcePort( UDP.RIP_PORT );
		udp.setDestinationPort( UDP.RIP_PORT );

		rip.setCommand( RIPv2.COMMAND_REQUEST );

		router.sendPacket( ether, iface );
	}

	void handleRequest( Router router, Ethernet original, Iface iface ) {
		sendResponse( router, original, iface, null );	
	}

	void sendResponse( Router router, Ethernet original, Iface iface, List<RIPv2Entry> entries ) {
		Ethernet ether = new Ethernet();
		IPv4 ip = new IPv4();
		UDP udp = new UDP();	
		RIPv2 rip = new RIPv2();
		ether.setPayload( ip );
		ip.setPayload( udp );
		udp.setPayload( rip );

		ether.setEtherType( Ethernet.TYPE_IPv4 );
		if( original != null ) {
			ether.setDestinationMACAddress( original.getSourceMACAddress() );
		}
		else {
			ether.setDestinationMACAddress( "FF:FF:FF:FF:FF:FF" );
		}

		ip.setTtl( (byte)64 ).setProtocol( IPv4.PROTOCOL_UDP );
		if( original != null ) {
			ip.setDestinationAddress( (IPv4)original.getPayload().getSourceAddress() ); 
		}
		else {
			ip.setDestinationAddress( IPv4.toIPv4Address( "224.0.0.9" ) ); 
		}
		ip.setSourceAddress( iface.getIpAddress() );	

		udp.setSourcePort( UDP.RIP_PORT );
		udp.setDestinationPort( UDP.RIP_PORT );

		rip.setCommand( RIPv2.COMMAND_RESPONSE );
		if( entries != null ) {
			rip.setEntries( entries );
		}
		else {
			for( RouteEntry route : router.getRouteTable().getEntries() ) {
				RIPv2Entry entry = new RIPv2Entry( route.getDestinationAddress(), route.getMaskAddress(), route.getNumHops() ); 
				entry.setNextHopAddress( route.getGatewayAddress() );
				rip.addEntry( entry ); 			
			}
		}
		router.sendPacket( ether, iface );
	}

	void handleResponse( Router router, RIPv2 rip, Iface inIface ) {
		RouteTable routeTable = router.getRouteTable();
		for( RIPv2Entry entry : rip.getEntries() ) {
			routeTable.updateFromResponse( entry.getAddress(), entry.getSubnetMask(), entry.getNextHoopAddress(), inIface, entry.getNumHops() ); 
		}
	}

	void sendResponsesAll( Router router ) {
		List<RIPv2Entry> entries = new LinkedList<>();
		for( RouteEntry route : router.getRouteTable().getEntries() ) {
			RIPv2Entry entry = new RIPv2Entry( route.getDestinationAddress(), route.getMaskAddress(), route.getNumHops() ); 
			entry.setNextHopAddress( route.getGatewayAddress() );
			entries.add( entry ); 			
		}
		
		for( Iface iface : router.getInterfaces().values() ) {
			sendResponse( router, null, iface, entries );	
		}
	}
}
