package edu.wisc.cs.sdn.vnet.rt;

import net.floodlightcontroller.packet.IPv4;
import edu.wisc.cs.sdn.vnet.Iface;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An entry in a route table.
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class RouteEntry 
{
	/** Destination IP address */
	private int destinationAddress;
	
	/** Gateway IP address */
	private int gatewayAddress;
	
	/** Subnet mask */
	private int maskAddress;
	
	/** Router interface out which packets should be sent to reach
	 * the destination or gateway */
	private Iface iface;

	/** Number of hops, used as a metric	*/
	private int numHops;

	private Timer timer;
	private RouteTable table;
	
	/**
	 * Create a new route table entry.
	 * @param destinationAddress destination IP address
	 * @param gatewayAddress gateway IP address
	 * @param maskAddress subnet mask
	 * @param iface the router interface out which packets should 
	 *        be sent to reach the destination or gateway
	 */
	public RouteEntry(int destinationAddress, int gatewayAddress, 
			int maskAddress, Iface iface, RouteTable table)
	{
		this.destinationAddress = destinationAddress;
		this.gatewayAddress = gatewayAddress;
		this.maskAddress = maskAddress;
		this.iface = iface;
		this.talbe = table;
		this.numHops = 0;
		if( gatewwayAddress == 0 ) {
			timer = null;
		}
		else {
			timer = new Timer();
			timer.scheduleAtFixedRate( new TimerTask() {
				void run() {
					synchronized( table ) {
						table.remove( this.destinationAddress, this.maskAddress );	
						timer.cancel();
						timer.purge();	
					}
				}
			}, 0, 30000 ); // 30 sec
		}
	}

	public void resetTimer() {
		timer.cancel();
		timer.purge();
		timer = new Timer();
		timer.scheduleAtFixedRate( new TimerTask() {
			void run() {
				synchronized( table ) {
					table.remove( this.destinationAddress, this.maskAddress );	
					timer.cancel();
					timer.purge();	
				}
			}
		}, 0, 30000 ); // 30 sec
	}

	public void incNumHops() {
		this.numHops++;
	}

	public void setNumHops( int numHops ) {
		this.numHops = numHops;
	}
	
	public int getNumHops() {
		return this.numHops;
	}

	/**
	 * @return destination IP address
	 */
	public int getDestinationAddress()
	{ return this.destinationAddress; }
	
	/**
	 * @return gateway IP address
	 */
	public int getGatewayAddress()
	{ return this.gatewayAddress; }

    public void setGatewayAddress(int gatewayAddress)
    { this.gatewayAddress = gatewayAddress; }
	
	/**
	 * @return subnet mask 
	 */
	public int getMaskAddress()
	{ return this.maskAddress; }
	
	/**
	 * @return the router interface out which packets should be sent to 
	 *         reach the destination or gateway
	 */
	public Iface getInterface()
	{ return this.iface; }

    public void setInterface(Iface iface)
    { this.iface = iface; }
	
	public String toString()
	{
		return String.format("%s \t%s \t%s \t%s",
				IPv4.fromIPv4Address(this.destinationAddress),
				IPv4.fromIPv4Address(this.gatewayAddress),
				IPv4.fromIPv4Address(this.maskAddress),
				this.iface.getName());
	}
}
