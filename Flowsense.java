package net.floodlightcontroller.flowsense;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.LLDP;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;

import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;

import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Flowsense implements IOFMessageListener, IFloodlightModule {
protected IFloodlightProviderService floodlightProvider;
protected static Logger logger;
/*protected class Flow{
    public Iterable<MatchField<?>> flow;
    public long time;
   // public int port;
    public long swid;
    public void setTimeStamp(long tm)
    {
      time=tm;
    }
    public Flow(Iterable<MatchField<?>> f,long s)
    {
    	flow=f;
    	//port=p;
    	swid=s;
    }
    public String toString()
    {
    	return flow.toString()+" SWID is "+swid+" ";
    }

}*/

protected class CheckPoint{
	public int active;
	public long time;
	public double util;
	//public boolean ifActive;
	public CheckPoint()
	{
		time=System.currentTimeMillis();
		//ifActive=true;
		active=0;
		util=0;
	}
}


protected LinkedList<CheckPoint> UT;

//protected ArrayList<Flow> Active_List;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return Flowsense.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> I=
				new ArrayList<Class <? extends IFloodlightService>>();
		I.add(IFloodlightProviderService.class);
		return I;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider=context.getServiceImpl
				(IFloodlightProviderService.class);
		
		UT=new LinkedList<CheckPoint>();
		
       
   //    Active_List = new ArrayList<Flow>();
        logger=LoggerFactory.getLogger(Flowsense.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
	    
		UtilizationMonitor(UT,msg,sw,cntx);
		return Command.CONTINUE;
	}

	

	public void UtilizationMonitor(LinkedList<CheckPoint> UT,OFMessage p,IOFSwitch sw,FloodlightContext cntx)
	{			
		
			
	
		
		switch(p.getType()){
		case PACKET_IN:
		{		
			System.out.print("\npacketIN");
			/*Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			 Various getters and setters are exposed in Ethernet 
	  //     MacAddress srcMac = eth.getSourceMACAddress();
	   //    VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());  
			if(eth.getEtherType()==0x0800){
			System.out.print("Ipv4:\n");
			System.out.printf("%H",eth.getEtherType());
			
		  
			OFPacketIn a= (OFPacketIn)p;
			 IPv4 ipv4 = (IPv4) eth.getPayload();	                   
		       
			   IPv4Address dstIp = ipv4.getDestinationAddress();
		       int dest_ip=dstIp.getInt();
		       IPv4Address socIp = ipv4.getSourceAddress();
		       int source_ip=socIp.getInt();
		       
		       short dl_type =eth.getEtherType();
		       short net_proto=ipv4.getProtocol().getIpProtocolNumber();
		//  System.out.print(a.getReason());
			Match match = a.getMatch();
			System.out.print("Tostring:\n");
		    System.out.print(match.getMatchFields().toString());
		    System.out.print("hashcode:\n");
		    System.out.print(match.getMatchFields().hashCode());
		    System.out.print("Version:\n");
		    System.out.print(match.getVersion());
		    System.out.print("tos:\n");
		    System.out.print(match.toString());
		    sw.getId().getLong();
			//Flow active_flow=new Flow(a.getMatch().getMatchFields(),sw.getId().getLong());
			FlowEntry entry = new FlowEntry(sw.getId().getLong(), in_port, source_ip, dest_ip, net_proto, dl_type);
			synchronized(activeFlowTable)
			{
			if (activeFlowTable.contains(entry) == false && entry.getDlType() == Ethernet.TYPE_IPv4
			&& ipv4.getProtocol().equals(IpProtocol.UDP)) {
			entry.setTimestamp(System.currentTimeMillis());
			activeFlowTable.add(entry);
			logger.debug("Flow " + entry.toString() + " added to the Active Flow Table");
			System.out.print("Flow " + entry.toString() + " added to the Active Flow Table");
			}
			}
			/*boolean flag=false;
			Match match = a.getMatch();
			
			System.out.print(match.getMatchFields().toString());
			synchronized(Active_List)
			{
				for(int i=0;i<Active_List.size();i++)
				{
				  if(Active_List.get(i).flow.equals(a.getMatch().getMatchFields())&&Active_List.get(i).swid==sw.getId().getLong())
					  flag=true;
				}
				System.out.print(flag);
			if(!flag)
			{			    	
			    active_flow.setTimeStamp(System.currentTimeMillis());
			    Active_List.add(active_flow);
			    System.out.print("Flow " + active_flow.toString() + " added to the Active Flow Table\n");
			}
			}
			
			}
			else System.out.print("not IPv4");*/
		}
		break;
		case FLOW_REMOVED:
		{   
		
			
			OFFlowRemoved flowremove=(OFFlowRemoved)p;	
			
			CheckPoint acp=new CheckPoint();	
			if(sw.getId().getLong()==1){	
			  System.out.print("\nflowremove.getDurationSec:");
			  System.out.print(flowremove.getDurationSec()+ flowremove.getDurationNsec() / 1e9);
			  System.out.print("\nflowremove.getByteCount:");
			  System.out.print(flowremove.getByteCount().getValue());
		
			//bf.append("\nflowremove.getDurationSec:"+flowremove.getDurationSec()+ flowremove.getDurationNsec() / 1e9);			
			//logger.info("\nflowremove.getByteCount:"+flowremove.getByteCount().getValue());
			
			if(flowremove.getReason()==0){
			  double duration=flowremove.getDurationSec()-flowremove.getIdleTimeout()+ flowremove.getDurationNsec() / 1e9;
			 acp.util=(double) flowremove.getByteCount().getValue() /duration;	
			 Timestamp start,end;
			 start=new Timestamp(acp.time-flowremove.getDurationSec()*1000);
			 end=new Timestamp(acp.time-flowremove.getIdleTimeout()*1000);
             
			 System.out.print("\nFrom "+start+" to "+end+" reason="+flowremove.getReason()+",this flow's utilization is "+acp.util);
			 //logger.info("\nFrom "+start+" to "+end+",this flow's utilization is "+acp.util);
			}
			else{
				acp.util=(double) flowremove.getByteCount().getValue() /
						( flowremove.getDurationSec()+ flowremove.getDurationNsec() / 1e9);
				Timestamp start,end;
				 start=new Timestamp(acp.time-flowremove.getDurationSec());
				 end=new Timestamp(acp.time);
				 System.out.print("From "+start+" to "+end+" reason="+flowremove.getReason()+",this flow's utilization is "+acp.util);
				// logger.info("From "+start+" to "+end+",this flow's utilization is "+acp.util);
					}
			}
			/*logger.debug("Removed flow: " + removedEntry.toString());
			System.out.print("Removed flow: " + removedEntry.toString());
			
			long checkPointTimeStamp = System.currentTimeMillis();
			long timeOffset = 0;
			double duration = 0.0;
			long byteCount = 0;
		           Match match = b.getMatch();
			
		/*	System.out.print(match.getMatchFields().toString());
			 synchronized (Active_List)
			 {
			 for (Iterator<FlowEntry> it = activeFlowTable.iterator(); it.hasNext();)
			 {
			 FlowEntry flow = it.next();
			 if (flow.equals(removedEntry))
			 {
			 tmpMatchFlow = flow;
			 break;
			 }
			 }
		         /*   for(int i=0;i<Active_List.size();i++)
					{
					  if(Active_List.get(i).flow.equals(b.getMatch().getMatchFields())&&Active_List.get(i).swid==sw.getId().getLong())
					  { tmpMatchFlow = Active_List.get(i);
					  break;
					  }
					}
			 }*/
			/* checkPointTimeStamp -= timeOffset;
			// logger.debug("[FLOW_MOD] Checkpoint = " + checkPointTimeStamp);
			 System.out.print("[FLOW_MOD] Checkpoint = " + checkPointTimeStamp+"\n");
			 if (tmpMatchFlow != null)
			 {
			 //logger.debug("Found a Matching Flow: " + tmpMatchFlow.toString());
		     System.out.print("Found a Matching Flow: " + tmpMatchFlow.toString()+"\n");			
			 
			 duration = b.getDurationSec() + (b.getDurationNsec() / 1e9);
			 if (b.getReason()==0)
			 {
			 timeOffset = (long) b.getIdleTimeout() * 1000;
			 duration -= b.getIdleTimeout();
			 }
			 byteCount = b.getByteCount().getValue();
			
			 Active_List.remove(tmpMatchFlow);
			// printActiveFlows();
			 double utilization = (double) byteCount / duration;
			//  logger.debug("Instant utilization = " + utilization);
			 System.out.print("Instant utilization = " + utilization+"\n");
			 }
		/*	for(int i=0;i<Active_List.size();i++){
				if(Active_List.get(i).flow.equals(b.getMatch())){
					Active_List.remove(i);
					break;
				}		
			}
			CheckPoint chkpt=new CheckPoint();
			
			if(b.getIdleTimeout()!=0)
			{
				chkpt.time=minutes(chkpt.time,b.getIdleTimeout());
				
				
			}
			chkpt.active=Active_List.size();
			
			chkpt.util=b.getByteCount().getValue()/b.getDurationSec();//u64/u64 no
			for(int i=0;i<UT.size()&&UT.get(i).ifActive;i++){
				if(UT.get(i).time.before(chkpt.time)){
					UT.get(i).active--;
					UT.get(i).util+=chkpt.util;
				}
				if(UT.get(i).active==0)
				{
					UT.get(i).ifActive=false;
				}
			}
			/*System.out.print(chkpt.active);
			System.out.print("\n");
			System.out.print(chkpt.ifActive);
			System.out.print("\n");
			System.out.print(chkpt.util);
			System.out.print("\n");
			System.out.print(chkpt.time);
			System.out.print("\n");
			UT.add(chkpt);*/
			
		}
		break;
		default:break;
		}
		
		
	}
	
	/*@SuppressWarnings("deprecation")
	public Timestamp minutes(Timestamp ts,int s)
	{
		Timestamp results=ts;
		while(ts.getSeconds()-s<0)
		  {
			  if(ts.getMinutes()-1<0)
			  {
				  ts.setMinutes(59);
				  s = s - 60;
				  if(ts.getHours() - 1 < 0)
				  {
					  ts.setHours(23);
					  if(ts.getDay() - 1 < 0)
					  {
						  
					  }
				  }
				  else
				  {
					  ts.setHours(ts.getHours()-1);
				  }
			  }
			  else
			  {
				  ts.setMinutes(ts.getMinutes() - 1);
			   }
		  }
		 
			  ts.setSeconds(ts.getSeconds ()- s);

    	return results;
}*/
	/*public void printActiveFlows()
	{
	String text = "Active Flow Set:";
	Object[] allEntries = null;
	synchronized (activeFlowTable)
	{
	allEntries = activeFlowTable.toArray(new Flow[0]);
	}
	if (allEntries == null || allEntries.length <= 0)
	{
	text += " [empty]";
	}
	else
	{
	for (int i = 0; i < allEntries.length; i++)
	{
	text += "\n\t" + allEntries[i].toString();
	}
	}
	logger.debug(text);
	}*/

}