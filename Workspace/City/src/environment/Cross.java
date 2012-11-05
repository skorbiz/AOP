package environment;

import vehicle.Vehicle;
import general.Settings;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
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
	// Which agents the cross want to find
	private String agentToFind = "lane";
	// The list of input and output lane agents
	private AID[] inLaneAgents = new AID[4];
	private AID[] outLaneAgents = new AID[4];
	// traffic direction, "v" for vertical, "h" for horizontal
	private String traDir = "v";
	// Price for changing direction
	private int chaPri = 2;
	
	private int ticks = 0;
  
	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Cross-agent " + getAID().getName() + " is ready.");
	    
		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			crossId = (Integer) args[0];
			System.out.println("cross identifier are: " + crossId);
			
			// Add a TickerBehaviour that schedules a request to seller agents every minute
			addBehaviour(new TickerBehaviour(this, 5000) {
				protected void onTick() {
					if( ticks==0 ) {
						addBehaviour(new FindingRightLanes());
					}
					else {
						addBehaviour(new RequestLaneOffers());
					}
					ticks++;
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println("Not all parameters are specified");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Cross-agent " + getAID().getName() + " terminating.");
	}
  

	private class FindingRightLanes extends Behaviour {
		private AID[] laneAgents;
		private String conIdFind = "lane-finding";
		private int repliesCnt = 0; // The counter of replies from lane agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
	
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
						System.out.println("Found " + result.length + " " + agentToFind + " agent(s).");
						for (int i = 0; i < result.length; ++i) {
							laneAgents[i] = result[i].getName();
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					step = 1;
					break;
				case 1: // Send the cfp to all lanes
					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
					for (int i = 0; i < laneAgents.length; ++i) {
						cfp.addReceiver(laneAgents[i]);
					} 
					cfp.setContent(Settings.CrossToLaneRequestLocalID);
					cfp.setConversationId(conIdFind);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdFind),
	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 2;
					break;
				case 2: // Receive all proposals/refusals from lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							// This is an offer
							int laneId = Integer.parseInt(reply.getContent());
							int testIn = Settings.inLane(laneId, crossId);
							int testOut = Settings.outLane(laneId, crossId);
							if( testIn!=-1 ) {
								inLaneAgents[testIn] = reply.getSender();
							}
							else if( testOut!=-1 ) {
								outLaneAgents[testOut] = reply.getSender();
							}
						}
						repliesCnt++;
						if (repliesCnt >= laneAgents.length) {
							// We received all replies
							step = 3;
							System.out.println(myAgent.getLocalName() + " in0: " + inLaneAgents[0]);
							System.out.println(myAgent.getLocalName() + " in1: " + inLaneAgents[1]);
							System.out.println(myAgent.getLocalName() + " in2: " + inLaneAgents[2]);
							System.out.println(myAgent.getLocalName() + " in3: " + inLaneAgents[3]);
							System.out.println(myAgent.getLocalName() + " out0: " + outLaneAgents[0]);
							System.out.println(myAgent.getLocalName() + " out1: " + outLaneAgents[1]);
							System.out.println(myAgent.getLocalName() + " out2: " + outLaneAgents[2]);
							System.out.println(myAgent.getLocalName() + " out3: " + outLaneAgents[3]);
							System.out.println("");
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
		private String conIdOffer = "lane-offers";
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt;
		private int[] offers = new int[4];
		private int step = 0;

		public void action() {
			switch (step) {
				case 0: // Send the cfp to all lanes
					offers[0] = -1;
					offers[1] = -1;
					offers[2] = -1;
					offers[3] = -1;
					step = 1;
					break;
				case 1:
					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
					for (int i = 0; i < inLaneAgents.length; ++i) {
						cfp.addReceiver(inLaneAgents[i]);
					} 
					cfp.setContent(Settings.CrossToLaneRequesOffers);
					cfp.setConversationId(conIdOffer);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdOffer),
	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
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
							// We received all replies
							System.out.println(inLaneAgents[0] + " offers: " + offers[0]);
							System.out.println(inLaneAgents[1] + " offers: " + offers[1]);
							System.out.println(inLaneAgents[2] + " offers: " + offers[2]);
							System.out.println(inLaneAgents[3] + " offers: " + offers[3]);
							if( offers[0]+offers[1]+chaPri>offers[2]+offers[3] ) { // vertical
								chaPri = 2;
								traDir = "v";
								System.out.println("Trafic direction: up/down (" + traDir + ")");
							}
							else { // horizontal
								chaPri = -2;
								traDir = "h";
								System.out.println("Trafic direction: left/right (" + traDir + ")");
							}
							System.out.println("");
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
	
	
	private class MovingCars extends Behaviour {
		private int step = 0;
		private MessageTemplate mt;
		private int repliesCnt = 0;
		private String conIdMove = "lane-Move";
		private int[] emptySpacesInLane = new int[2];
		private AID[] accInLaneAgents = new AID[2];
		private AID[] accOutLaneAgents = new AID[2];
		private Object[] vehicles = new Object[2];
		private int vehiclesToMove = 0;
		
		public void action() {
			switch (step) {
				case 0:
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
				case 1:
					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
					cfp.addReceiver(accOutLaneAgents[0]);
					cfp.addReceiver(accOutLaneAgents[1]);
					
					cfp.setContent(Settings.CrossToLaneRequestRetrieveVehicle);
					cfp.setConversationId(conIdMove);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
											 MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 2;
					break;
				case 2: // Receive all proposals/refusals from lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// This is an offer
							int emptySpaces = Integer.parseInt(reply.getContent());
							if( accOutLaneAgents[0].compareTo(reply.getSender())==0 ) {
								emptySpacesInLane[0] = emptySpaces;
							}
							else {
								emptySpacesInLane[1] = emptySpaces;
							}
						}
						repliesCnt++;
						if (repliesCnt >= inLaneAgents.length/2) {
							// We received all replies
							step = 3;
						}
					}
					else {
						block();
					}
					break;
				case 3:
					vehiclesToMove = 0;
					ACLMessage cfp2 = new ACLMessage(ACLMessage.REQUEST);
					if( emptySpacesInLane[0]>0 ) {
						cfp2.addReceiver(inLaneAgents[0]);
						vehiclesToMove++;
					}
					else if( emptySpacesInLane[1]>0 ) {
						cfp2.addReceiver(inLaneAgents[1]);	
					}
					cfp2.setContent(Settings.CrossToLaneRequestInsertVehicle);
					cfp2.setConversationId(conIdMove);
					cfp2.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp2);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
					MessageTemplate.MatchInReplyTo(cfp2.getReplyWith()));
					if( vehiclesToMove>0 )
					{
						System.out.println(myAgent.getLocalName() + ": Out lanes have no space for vehicle!");
						step = 6;
					}
					else {
						step = 4;
					}
					break;
				case 4:
					repliesCnt = 0;
					ACLMessage replyInLane = myAgent.receive(mt);
					if (replyInLane != null) {
						// Reply received
						if (replyInLane.getPerformative() == ACLMessage.INFORM) {
							try {
								// This is an offer
								if( accInLaneAgents[0].compareTo(replyInLane.getSender())==0 ) {
									vehicles[0] = replyInLane.getContentObject();
								}
								else {
									vehicles[1] = replyInLane.getContentObject();
								}
							}
							catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						vehiclesToMove--;
						if( vehiclesToMove == repliesCnt ) {
							// We received all replies
							step = 5;
						}
					}
					else {
						block();
					}
					break;
				case 5:
					ACLMessage replyOutLane = new ACLMessage(ACLMessage.REQUEST);
					try {
						if( vehicles[0]!=null ) {
							replyOutLane.addReceiver(accOutLaneAgents[1]);
							replyOutLane.setContentObject((Serializable)vehicles[0]);
						}
						else if( vehicles[0]!=null ) {
							replyOutLane.addReceiver(accOutLaneAgents[0]);
							replyOutLane.setContentObject((Serializable)vehicles[1]);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					replyOutLane.setContent("2");
					replyOutLane.setConversationId(conIdMove);
					replyOutLane.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(replyOutLane);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
					MessageTemplate.MatchInReplyTo(replyOutLane.getReplyWith()));
					step = 6;
					break;
					
			}
		}
		
		public boolean done() {
			return (step == 6);
		}
	}
	
}