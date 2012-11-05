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
import java.io.Serializable;
import java.lang.System;


public class Lane extends Agent {

	// Lane identifiers
	private int laneId;
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
			
//			laneId = Integer.parseInt((String)args[0]);
			laneId = (Integer) args[0];
			System.out.println("Lane identifiers are: " + laneId);
		
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
	    
			// Add the behavior serving lane id queries from cross agents
			addBehaviour(new RequestLaneIdServer());
	
			// Add the behavior serving offer queries from cross agents
			addBehaviour(new RequestOfferServer());
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
  
	
	private class RequestLaneIdServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				// CFP Message received. Process it
//				int crossId = Integer.parseInt(msg.getContent());
				
				// make reply
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(Integer.toString(laneId));
				
//				if ( inLane(laneId, crossId)!=-1 || outLane(laneId, crossId)!=-1 ) {
//					reply.setPerformative(ACLMessage.PROPOSE);
//					reply.setContent(Integer.toString(laneId));
//				}
//				else {
//					reply.setPerformative(ACLMessage.REFUSE);
//					reply.setContent("Not the right lane");
//				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
	

	private class RequestOfferServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				// make reply
				ACLMessage reply = msg.createReply();
				
				int price = queue.getPrice();
				
				if ( price>=0 ) {
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(price));
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Lane out of order");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
	
	
//	if ( 0==p.compareTo(dirIn) ) {
//		Vehicle vehicle = queue.retrieveVehicle();
//		System.out.println(vehicle);
//		if( vehicle!=null ) {
//			try {
//				reply.setContentObject(vehicle);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		reply.setContent("i" + dirOut);
//	}
//	else if ( 0==p.compareTo(dirOut) ) {
//		reply.setContent("o" + dirIn);
//	}
	
	
//	private class InsertVehicleServer extends CyclicBehaviour {
//		public void action() {
//			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
//			ACLMessage msg = myAgent.receive(mt);
//			
//			if (msg != null) {
//				System.out.println("InsertVehicleServer");
//				boolean test = false;
//				try {
//					Object veh = msg.getContentObject();
//					test = queue.insertVehicle( (Vehicle) veh );
//				}
//				catch (Exception ex) {
//						ex.printStackTrace();
//				}
//							
//				ACLMessage reply = msg.createReply();
//				
//				if ( test ) {
//					System.out.println("Lane INFORM");
//					reply.setPerformative(ACLMessage.INFORM);
//				}
//				else {
//					System.out.println("Lane FAILURE");
//					reply.setPerformative(ACLMessage.FAILURE);
//					reply.setContent("Can not insert vehicle");
//				}
//				myAgent.send(reply);
//			}
//			else {
//				block();
//			}
//		}
//	}
	
	
	private class QueueLane {
		ArrayList<Vehicle> queue = new ArrayList<Vehicle>();
		boolean running = true;
		Random random = new Random();
		
		public QueueLane() {
		}
		
		public int numberOfVehicles() {
			return queue.size();
		}
		
		public int getPrice() {
			int price = -1;
			if( running==true ) {
				long curtime = System.currentTimeMillis();
				for(int i=0; i<queue.size(); i++) {
					price += (curtime - queue.get(i).getWaitTime());
				}
			}
			return price/1000;
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
		
		public void startQueue() {
			running = true;
		}
		public void stopQueue() {
			running = false;
		}
		public boolean statusQueue() {
			return running;
		}
	}
}