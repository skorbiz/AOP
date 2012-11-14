package environment;
import vehicle.*;

import general.Settings;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System;


public class Lane extends Agent {

	private int laneId;						// Lane identifiers
	private String typeOfAgent = "lane";	// The type of the agent
	private QueueLane queue;				// Queue for vehicle

	protected void setup() {
		//System.out.println("Lane-agent "+getAID().getLocalName()+" is ready.");
		
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			
			laneId = (Integer) args[0];
			//System.out.println("Lane identifiers are: " + laneId);
		
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
			try 
			{
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) { fe.printStackTrace(); }
	    
			// Add the behavior serving lane id queries from cross agents
			addBehaviour(new RequestLaneIdServer());
	
			// Add the behavior serving offer queries from cross agents
			addBehaviour(new RequestOfferServer());
			
			// Add the behavior serving offer queries from gui agents
			addBehaviour(new RequestNumberVehicleServer());
			
			// Add the behavior serving space in queue from cross agents
			addBehaviour(new RequestNumberEmptySpacesServer());
			
			// Add the behavior serving sending vehicle to cross agents
			addBehaviour(new RequestRetrieveVehicleServer());
			
			// Add the behavior serving reveiving vehicle from cross agents
			addBehaviour(new InsertVehicleServer());
		}
		else {
			// Make the agent terminate
			System.out.println("Not all parameters are specified in Lane agent");
			doDelete();
		}
	}

	protected void takeDown() {
		// Deregister from the yellow pages
		try 
		{
			DFService.deregister(this);
		}
		catch (FIPAException fe) { fe.printStackTrace(); }
		// Printout a dismissal message
		System.out.println("Lane-agent "+getAID().getLocalName()+" terminating.");
	}
  
	
	private class RequestLaneIdServer extends CyclicBehaviour {
		public void action() 
		{
			MessageTemplate mt = MessageTemplate.and(  
					MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
					MessageTemplate.MatchContent(Settings.CrossToLaneRequestLocalID));

			ACLMessage msg = myAgent.receive(mt);
			
			if( msg != null ) 
			{
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(Integer.toString(laneId));
				myAgent.send(reply);
			}
			else
				block();
		}
	}
	

	private class RequestOfferServer extends CyclicBehaviour {
		public void action() 
		{	
			MessageTemplate mt = MessageTemplate.and(  
					MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
					MessageTemplate.MatchContent(Settings.CrossToLaneRequesOffers));

			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				ACLMessage reply = msg.createReply();
				
				int price = queue.getPrice();
				
				if ( price>=0 ) 
				{
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(price));
				}
				else 
				{
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Lane out of order");
				}
				myAgent.send(reply);
			}
			else
				block();			
		}
	}
	
	
	private class RequestNumberVehicleServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(  
													MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
													MessageTemplate.MatchContent(Settings.GuiToLaneRequestCars));

			ACLMessage msg = myAgent.receive(mt);
			if ( msg != null )											//Create reply messages
			{
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				int numberOfVehicles = queue.getNumberOfVehicles();
				//reply.setContent(Integer.toString((int) (Math.random()*10)));
				reply.setContent(Integer.toString(numberOfVehicles));
				myAgent.send(reply);
			}
			else 
				block();
		}
	}
	
	
	private class RequestNumberEmptySpacesServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(  
													MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
													MessageTemplate.MatchContent(Settings.CrossToLaneRequesSpaces));

			ACLMessage msg = myAgent.receive(mt);
			if (msg != null  )
			{
				// make reply
				ACLMessage reply = msg.createReply();
				int numberOfFreeSpace = queue.getNumberOfFreeSpacesForVehicles();
				
				if ( numberOfFreeSpace>=0 ) 
				{
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(numberOfFreeSpace));
				}
				else 
				{
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
	
	
	private class RequestRetrieveVehicleServer extends CyclicBehaviour {
		private int step = 0;
		MessageTemplate mt;
		
		public void action() {
			MessageTemplate mt = MessageTemplate.and(  
													MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
													MessageTemplate.MatchContent(Settings.CrossToLaneRequestRetrieveVehicle));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				ACLMessage reply = msg.createReply();
				Vehicle vehicle = queue.retrieveVehicle();
				if( vehicle != null ) 
				{
					System.out.println("RequestRetrieveVehicleServer: " + myAgent.getLocalName() + ": " + vehicle.getWaitTime());

					reply.setPerformative(ACLMessage.INFORM);
					try 
					{
						reply.setContentObject(vehicle);
					} 
					catch (IOException e) { e.printStackTrace(); }
				}
				else 
				{
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Can not insert vehicle");
				}
				myAgent.send(reply);
			}
			else 
				block();
		}
	}
		
	private class InsertVehicleServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative( ACLMessage.PROPAGATE );
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) 
			{
				boolean test = false;
				try 
				{
					Vehicle vehicle = (Vehicle) msg.getContentObject();
					long temp = vehicle.getWaitTime();
					test = queue.insertVehicle( (Vehicle) vehicle );
					if( test ) 
					{
						System.out.println(myAgent.getLocalName() + " inserted " + vehicle + " (" + temp + "=>" + vehicle.getWaitTime() + ").");
					}
					else 
						System.out.println("Error");
				}
				catch (Exception ex) { ex.printStackTrace(); }
							
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
			}
			else 
				block();
		}
	}
	
	
	private class QueueLane 
	{
		ArrayList<Vehicle> vehicleInLane = new ArrayList<Vehicle>();
		private int maxVehicleInLane = 10;
		
		public QueueLane() 
		{
		}
		
		public int getNumberOfVehicles() 
		{
			return vehicleInLane.size();
		}
		
		public int getNumberOfFreeSpacesForVehicles() 
		{
			return maxVehicleInLane-vehicleInLane.size();
		}
		
		public int getPrice() 
		{
			int price = -1;
			for(int i = 0; i < vehicleInLane.size(); i++)				
				price += vehicleInLane.get(i).getWaitTime();

			return price/1000;
		}
		
		public boolean insertVehicle(Vehicle vehicle) 
		{
			if( vehicleInLane.size() < maxVehicleInLane )
				return vehicleInLane.add(vehicle);
			else 
				return false;
		}
		
		public Vehicle retrieveVehicle() 
		{	
			if(vehicleInLane.size() == 0)										//No vehicles in que
				return null;
			
			if(vehicleInLane.size() >= 2)										//If the vehicle behind the vehicle to be transfered has reached the traffic light,
				vehicleInLane.get(1).putVehicleInQue();							//it will be put in the state 'inQue' and have a speed of zero
			
			boolean vehicleReadyToTransfer;										//Update the front vehicle to see if its ready to transfer
			vehicleReadyToTransfer = vehicleInLane.get(0).transferVehicle();	//The vehicle is updated such that the parameters fit the next que
			
			if(vehicleReadyToTransfer == false)									//Return null if the vehicle is not ready to transfer
				return null;											
			
			return vehicleInLane.remove(0);
		}		
	}
}
