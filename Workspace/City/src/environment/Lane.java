package environment;
import vehicle.*;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.lang.System;


public class Lane extends Agent {

	// Lane identifiers
	private String ori;
	private String pos;
	private String dirIn;
	private String dirOut;
	// The type of the agent
	private String typeOfAgent = "lane";
	// Queue for vehicle
	private QueueLane queue;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Lane-agent "+getAID().getName()+" is ready.");
		
		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			
			ori = (String) args[0];
			pos = (String) args[1];
			dirIn = (String) args[2];
			dirOut = (String) args[3];
			System.out.println("Lane identifiers are: " + ori + pos + dirIn + dirOut);
		
			// Create the vehicle queue
			queue = new QueueLane();
			
			// insert cars into lane
			for(int i=0; i<5; i++) {
				queue.insertVehicle(new Car());
			}
			
			// Register the lane-trading service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType(typeOfAgent);
			sd.setName("Lane-trading");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
	    
			// Add the behavior serving queries from cross agents
			addBehaviour(new RequestPriceServer());
	
			// Add the behavior serving purchase orders from buyer agents
			addBehaviour(new RequestVehicleServer());
		}
		else {
			// Make the agent terminate
			System.out.println("Not all parameters are specified");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Lane-agent "+getAID().getName()+" terminating.");
	}
  
	
	private class RequestPriceServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				// CFP Message received. Process it
				String x = msg.getContent().substring(0,1);
				String y = msg.getContent().substring(1,2);
//				System.out.println("Lane " + getName() + " got message from cross: " + x + y);
				
				ACLMessage reply = msg.createReply();
	
				Integer price = (Integer) queue.getPrice();
				
//				System.out.println( "(" + ori.compareTo("v") + " && " + x.compareTo(pos) + " && " + y.compareTo(dirIn) + ") || (" + ori.compareTo("h") + " && " + y.compareTo(pos) + " && " + x.compareTo(dirIn) + ")");
				if ( (0==ori.compareTo("v") && 0==x.compareTo(pos) && 0==y.compareTo(dirIn)) || 
					 (0==ori.compareTo("h") && 0==y.compareTo(pos) && 0==x.compareTo(dirIn)) ) {
//					System.out.println("Lane PROPOSE.");
					// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(ori + String.valueOf(price.intValue()));
				}
				else {
					// The requested book is NOT available for sale.
//					System.out.println("Lane REFUSE");
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Not the right lane");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	

	private class RequestVehicleServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				String o = msg.getContent().substring(0,1);
				String p = msg.getContent().substring(1,2);
				
				ACLMessage reply = msg.createReply();
				
				if ( 0==ori.compareTo(o) && 0==p.compareTo(pos) ) {
					reply.setPerformative(ACLMessage.INFORM);
					if ( 0==p.compareTo(dirIn) ) {
						Vehicle vehicle = queue.retrieveVehicle();
						reply.setContent("i" + dirOut + vehicle.toString());
						try {
							reply.setContentObject(vehicle);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else if ( 0==p.compareTo(dirOut) ) {
						reply.setContent("o" + dirOut);
					}
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Not the right lane");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	
	private class InsertVehicleServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				boolean test = false;
				try {
					Object veh = msg.getContentObject();
					test = queue.insertVehicle( (Vehicle) veh );
				}
				catch (Exception ex) {
						ex.printStackTrace();
				}
							
				ACLMessage reply = msg.createReply();
				
				if ( test ) {
					System.out.println("Lane INFORM");
					reply.setPerformative(ACLMessage.INFORM);
				}
				else {
					System.out.println("Lane FAILURE");
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Can not insert vehicle");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	
	
	private class QueueLane {
		ArrayList<Vehicle> queue = new ArrayList<Vehicle>();
		Random random = new Random();
		
		public QueueLane() {
		}
		
		public int getPrice() {
			int price = 0;
			long curtime = System.currentTimeMillis();
			for(int i=0; i<queue.size(); i++) {
				price += (curtime - queue.get(i).getWaitTime());
			}
			return price/1000;
//			return random.nextInt(10);
		}
		
		public boolean insertVehicle(Vehicle vehicle) {
			if( queue.size()<10 ) {
//				vehicle.setWaitTime(System.currentTimeMillis());
				vehicle.setWaitTime(System.currentTimeMillis()-random.nextInt(10000));
				return queue.add(vehicle);
			}
			else {
				return false;
			}
		}
		
		public Vehicle retrieveVehicle() {
			return queue.remove(0);
		}
		
	}
}
