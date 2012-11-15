package environment;

import vehicle.Vehicle;
import general.Settings;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.lang.Object;

public class Cross extends Agent {
	
	// The the identifier of the cross
	private int crossId;
	// The type of this agent
	private String typeOfAgent = "cross";
	// Which agents the cross want to find
	private String agentToFind = "lane";
	// The list of input and output lane agents
	private AID[] inLaneAgents = new AID[4];
	private AID[] outLaneAgents = new AID[4];
	// traffic direction, "v" for vertical, "h" for horizontal
	private String traDir = "v";
	// Price for changing direction
	private int chaPri = 10;

	
	/**
	 * Initialization of the agent.
	 */
	protected void setup() {
		// Printout a welcome message
		if(Settings.print)
			System.out.println("Cross-agent " + getAID().getLocalName() + " is ready.");

		// Register the service of the cross to the directory facilitator (yellow pages)
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(typeOfAgent);
		sd.setName("Cross-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	    
		// Get the identifier of the cross and add behaviours
		Object[] args = getArguments();
		if ( args!=null && args.length>0 ) {
			crossId = (Integer) args[0];
			
			// Add CyclicBehaviour: gui can request the light
			addBehaviour(new RequestDirection());
			
			// Add WakerBehaviour:
			addBehaviour(new WakerBehaviour(this, 1000) {
				public void onStart() {
					// Add Behaviour: finding the right lanes
					addBehaviour(new FindingRightLanes());
				};
				
				protected void onWake() {
					addBehaviour(new RequestLaneOffers());
					reset(10000);
				}
			} );
						
			// Add WakerBehaviour: for complex controlling of cross behaviours
			addBehaviour(new WakerBehaviour(this, 2000) {
				// used on switch-case
				private int step = 0;
				private String oldTrafficDir = traDir;
				private String newTrafficDir = traDir;
				
				protected void onWake() {
					int resetTime = 0;
					switch (step) {
						case 0:
							newTrafficDir = traDir;
							if( oldTrafficDir.compareTo(newTrafficDir)==0 ) {
								resetTime = 2000;
							}
							else
								resetTime = 6000;
							oldTrafficDir = newTrafficDir;
							step = 1;
							if( Settings.print )
								System.out.println(myAgent.getLocalName() + " waiting " + resetTime + "ms.");
							break;
						case 1:
							addBehaviour(new MovingVehicle());
							resetTime = 0;
							step = 0;
							break;
					}
					reset(resetTime);
				}
			} );
			
		}
		else {
			// Make the agent terminate
			System.err.println("Not all parameters are specified");
			doDelete();
		}
	}

	
	/**
	 * Takedown the agent
	 */
	protected void takeDown() {
		// deregister the service of the cross from the directory facilitator (yellow pages)
		try 
		{
			DFService.deregister(this);
		}
		catch (FIPAException fe) { fe.printStackTrace(); }
		// Printout a dismissal message
		System.err.println("Cross-agent " + getAID().getLocalName() + " terminating.");
	}
	

	/**
	 * Reply the gui with the current traffic direction.
	 */
	private class RequestDirection extends CyclicBehaviour {
		public void action() 
		{	
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative( ACLMessage.REQUEST ),
													 MessageTemplate.MatchContent( Settings.GuiToCrossRequestLights ));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) 
			{
				// make reply
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(traDir);
				myAgent.send(reply);
			}
			else {
				block();
			}
			
		}
	}
  

	/**
	 * 
	 */
	private class FindingRightLanes extends Behaviour {
		// used on switch-case
		private int step = 0;
		// The template to receive replies
		private MessageTemplate mt;
		// The counter of replies from lane agents
		private int repliesCnt = 0;
		// conversations ID of this behaviour
		private String conIdFind = "lane-finding";
		
		// array of AID used to store all lane agents
		private AID[] laneAgents;
		// numbers of input and output lane IDs
		private int[] inPutLanesToFind = new int[4];
		private int[] outPutLanesToFind = new int[4];

	
		public void action() {
			switch (step) {
				case 0: // Update the list of lane agents
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType(agentToFind);
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						laneAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							laneAgents[i] = result[i].getName();
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					step = 1;
					break;
				case 1: // Send the request message to all lanes
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					for (int i = 0; i < laneAgents.length; ++i) {
						msg.addReceiver(laneAgents[i]);
					} 
					msg.setContent(Settings.CrossToLaneRequestLocalID);
					msg.setConversationId(conIdFind);
					msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(msg);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdFind),
											 MessageTemplate.MatchInReplyTo(msg.getReplyWith()));

					inPutLanesToFind = Settings.getInputLanes(crossId);
					outPutLanesToFind = Settings.getOutputLanes(crossId);
					
					step = 2;
					break;
				case 2: // Receive proposals/refusals from all lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							// test if the replying lane is a input or output lane
							int temp = Integer.parseInt(reply.getContent());
							for( int i=0; i<4; i++) {
								if( temp==inPutLanesToFind[i] ) {
									inLaneAgents[i] = reply.getSender();
								}
								else if( temp==outPutLanesToFind[i] ) {
									outLaneAgents[i] = reply.getSender();
								}
							}
						}
						repliesCnt++;
						if (repliesCnt >= laneAgents.length) {
							// All replies received
							step = 3;
						}
					}
					else {
						block();
					}
					break;
			}        
		}
		
		public boolean done() {
			return (step == 3);
		}
	}
	
	private class RequestLaneOffers extends Behaviour {
		// used on switch-case
		private int step = 0;
		// The template to receive replies
		private MessageTemplate mt;
		// The counter of replies from lane agents
		private int repliesCnt = 0;
		// conversations ID of this behaviour
		private String conIdOffer = "lane-offers";
		
		// used for offers from the four lanes
		private int[] offers = new int[4];

		public void action() {
			switch (step) {
				case 0: // initalization of variables.
					offers[0] = -1;
					offers[1] = -1;
					offers[2] = -1;
					offers[3] = -1;
					step = 1;
					break;
				case 1: // Send the request message to input lanes agents
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					for (int i = 0; i < inLaneAgents.length; ++i) {
						msg.addReceiver(inLaneAgents[i]);
					} 
					msg.setContent(Settings.CrossToLaneRequesOffers);
					msg.setConversationId(conIdOffer);
					msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(msg);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdOffer),
											 MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
					step = 2;
					break;
				case 2: // Receive all proposals/refusals from lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// This is an offer
							int laneOffer = Integer.parseInt(reply.getContent());
							for(int i=0; i<inLaneAgents.length; i++) {
								if( inLaneAgents[i].compareTo(reply.getSender())==0 ) {
									offers[i] = laneOffer;
									break;
								}
							}
						}
						repliesCnt++;
						if (repliesCnt >= inLaneAgents.length) {
							System.out.println(offers[0] + "+" + offers[1] + " + " + chaPri + " > " + offers[2] + "+" + offers[3]);
							// All replies received
							if( (offers[0]+offers[1]+chaPri)>(offers[2]+offers[3]) ) { // vertical
								traDir = "v";
								chaPri = 50;
							}
							else { // horizontal
								traDir = "h";
								chaPri = -50;
							}
							if(Settings.print)
								System.out.println(myAgent.getLocalName() + " has trafic direction " + traDir + ".");
							step = 3;
						}
					}
					else {
						block();
					}
					break;
			}
		}

		public boolean done() {
			return (step == 3);
		}
		
	}
	
	
	private class MovingVehicle extends Behaviour {
		// used on switch-case
		private int step = 0;
		// The template to receive replies
		private MessageTemplate mt;
		// The counter of replies from lane agents
		private int repliesCnt = 0;
		// conversations ID of this behaviour
		private String conIdMove = "lane-Move";
		
		// used for storing the empty spaces from input lanes
		private int[] emptySpacesInLane = new int[2];
		// used of store the right input and output lanes based on the traffic direction
		private AID[] accInLaneAgents = new AID[2];
		private AID[] accOutLaneAgents = new AID[2];
		// store the vehicles there is about to be send across
		private Vehicle[] vehicles = new Vehicle[2];
		// to count the number of lanes there has to reply if there is able to move a vehicle
		private int vehiclesToMove = 0;
		
		public void action() {
			switch (step) {
				case 0: // initalization of variables.
					emptySpacesInLane[0] = -1;
					emptySpacesInLane[1] = -1;
					vehicles[0] = null;
					vehicles[1] = null;
					repliesCnt = 0;
					if( traDir.compareTo("v")==0 ) {
						accInLaneAgents[0] = inLaneAgents[0];
						accInLaneAgents[1] = inLaneAgents[1];
						accOutLaneAgents[0] = outLaneAgents[0];
						accOutLaneAgents[1] = outLaneAgents[1];
					}
					else {
						accInLaneAgents[0] = inLaneAgents[2];
						accInLaneAgents[1] = inLaneAgents[3];
						accOutLaneAgents[0] = outLaneAgents[2];
						accOutLaneAgents[1] = outLaneAgents[3];
					}
					step = 1;
					break;
				case 1: // Sending request to the right outgoing lanes to get the number of free spaces in there lane.
					ACLMessage msgFreeSpace = new ACLMessage(ACLMessage.REQUEST);
					msgFreeSpace.addReceiver(accOutLaneAgents[0]);
					msgFreeSpace.addReceiver(accOutLaneAgents[1]);
					msgFreeSpace.setContent(Settings.CrossToLaneRequesSpaces);
					msgFreeSpace.setConversationId(conIdMove);
					msgFreeSpace.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(msgFreeSpace);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
											 MessageTemplate.MatchInReplyTo(msgFreeSpace.getReplyWith()));
					step = 2;
					break;
				case 2: // Receive all inform/failure from lane agents about free spaces in lane.
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Stores the number of free spaces in the right place
							int emptySpaces = Integer.parseInt(reply.getContent());
							if( accOutLaneAgents[0].compareTo(reply.getSender())==0 ) {
								emptySpacesInLane[0] = emptySpaces;
							}
							else {
								emptySpacesInLane[1] = emptySpaces;
							}
						}
						repliesCnt++;
						if (repliesCnt >= 2) {
							// All replies received
							step = 3;
						}
					}
					else {
						block();
					}
					break;
				case 3: // If the outgoing lane has a free space in the lane, a request for a vehicle to the opposite input lane is made.
					repliesCnt = 0;
					ACLMessage msgGetVehicle = new ACLMessage(ACLMessage.REQUEST);
					if( emptySpacesInLane[0]>0 ) {
						msgGetVehicle.addReceiver(accInLaneAgents[0]);
						repliesCnt++;
					}
					if( emptySpacesInLane[1]>0 ) {
						msgGetVehicle.addReceiver(accInLaneAgents[1]);
						repliesCnt++;
					}
					msgGetVehicle.setContent(Settings.CrossToLaneRequestRetrieveVehicle);
					msgGetVehicle.setConversationId(conIdMove);
					msgGetVehicle.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(msgGetVehicle);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
											 MessageTemplate.MatchInReplyTo(msgGetVehicle.getReplyWith()));
					if( repliesCnt>0 )
					{
						// if there are vehicles to move
						step = 4;
					}
					else {
						// no vehicles to move
						if(Settings.print)
							System.out.println(myAgent.getLocalName() + ": Out lanes have no space for vehicle!");
						step = 6;
					}
					break;
				case 4: // Receiving vehicle from the right input lane.
					ACLMessage replyInLane = myAgent.receive(mt);
					if (replyInLane != null) {
						if (replyInLane.getPerformative() == ACLMessage.INFORM) {
							try {
								// Saves the vehicle in the right place
								if( accInLaneAgents[0].compareTo(replyInLane.getSender())==0 ) {
									vehicles[0] = (Vehicle) replyInLane.getContentObject();
								}
								if ( accInLaneAgents[1].compareTo(replyInLane.getSender())==0 ) {
									vehicles[1] = (Vehicle) replyInLane.getContentObject();
								}
							}
							catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						repliesCnt--;
						if( repliesCnt==0 ) {
							// All replies received
							step = 5;
						}
					}
					else {
						block();
					}
					break;
				case 5: // Sending vehicle to the right outgoing lane.
					try {
						if(Settings.print)
							System.out.println("Cross " + crossId + " sending vehicles " + vehicles[0] + " to " + accOutLaneAgents[0].getLocalName() + " and " + vehicles[1] + " to " + accOutLaneAgents[1].getLocalName() + ".");
						if( vehicles[0]!=null ) {
							ACLMessage replyOutLane1 = new ACLMessage(ACLMessage.PROPAGATE);
							myAgent.send(replyOutLane1);
							replyOutLane1.addReceiver(accOutLaneAgents[0]);
							replyOutLane1.setContentObject((Serializable)vehicles[0]);
							replyOutLane1.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
							myAgent.send(replyOutLane1);
						}
						if( vehicles[1]!=null ) {
							ACLMessage replyOutLane2 = new ACLMessage(ACLMessage.PROPAGATE);
							myAgent.send(replyOutLane2);
							replyOutLane2.addReceiver(accOutLaneAgents[1]);
							replyOutLane2.setContentObject((Serializable)vehicles[1]);
							replyOutLane2.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
							myAgent.send(replyOutLane2);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					step = 6;
					break;
			}
		}
		
		public boolean done() {
			return (step == 6);
		}
	}
	
}