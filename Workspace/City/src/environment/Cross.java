package environment;

import vehicle.Vehicle;
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
					cfp.setContent(Integer.toString(crossId));
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
							int testIn = inLane(laneId, crossId);
							int testOut = outLane(laneId, crossId);
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
					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
					for (int i = 0; i < inLaneAgents.length; ++i) {
						cfp.addReceiver(inLaneAgents[i]);
					} 
					cfp.setContent(Integer.toString(crossId));
					cfp.setConversationId(conIdOffer);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdOffer),
	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1: // Receive all proposals/refusals from lane agents
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
							step = 2;
						}
					}
					else {
						block();
					}
					break;
			}
		}

		public boolean done() {
			return (step == 2);
		}
		
	}
	
	
//	private class MovingCars extends Behaviour {
//		private int step = 0;
//		private MessageTemplate mt;
//		private int repliesCnt = 0;
//		private String conIdMove = "lane-Move";
//		private int[] emptySpacesInLane = new int[2];
//		private AID[] accInLaneAgents = new AID[2];
//		private AID[] accOutLaneAgents = new AID[2];
//		
//		public void action() {
//			switch (step) {
//				case 0:
//					emptySpacesInLane[0] = -1;
//					emptySpacesInLane[1] = -1;
//					if( traDir.compareTo("v")==0 ) {
//						accInLaneAgents[0] = inLaneAgents[0];
//						accInLaneAgents[1] = inLaneAgents[1];
//						accOutLaneAgents[0] = outLaneAgents[0];
//						accOutLaneAgents[1] = outLaneAgents[1];
//					}
//					else {
//						accInLaneAgents[0] = inLaneAgents[2];
//						accInLaneAgents[1] = inLaneAgents[3];
//						accOutLaneAgents[0] = outLaneAgents[2];
//						accOutLaneAgents[1] = outLaneAgents[3];
//					}
//					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
//					cfp.addReceiver(accOutLaneAgents[0]);
//					cfp.addReceiver(accOutLaneAgents[1]);
//					
//					cfp.setContent(Integer.toString(crossId));
//					cfp.setConversationId(conIdMove);
//					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
//					myAgent.send(cfp);
//					// Prepare the template to get proposals
//					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
//											 MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
//					step = 1;
//					break;
//				case 1: // Receive all proposals/refusals from lane agents
//					ACLMessage reply = myAgent.receive(mt);
//					if (reply != null) {
//						// Reply received
//						if (reply.getPerformative() == ACLMessage.INFORM) {
//							// This is an offer
//							int emptySpaces = Integer.parseInt(reply.getContent());
//							if( accOutLaneAgents[0].compareTo(reply.getSender())==0 ) {
//								emptySpacesInLane[0] = emptySpaces;
//							}
//							else {
//								emptySpacesInLane[1] = emptySpaces;
//							}
//						}
//						repliesCnt++;
//						if (repliesCnt >= inLaneAgents.length/2) {
//							// We received all replies
//							step = 2;
//						}
//					}
//					else {
//						block();
//					}
//					break;
//				case 2:
//					ACLMessage cfp2 = new ACLMessage(ACLMessage.REQUEST);
//					cfp2.addReceiver(inLaneAgents[0]);
//					cfp2.addReceiver(inLaneAgents[1]);
//					cfp2.setContent(Integer.toString(crossId));
//					cfp2.setConversationId(conIdMove);
//					cfp2.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
//					myAgent.send(cfp2);
//					// Prepare the template to get proposals
//					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdMove),
//					MessageTemplate.MatchInReplyTo(cfp2.getReplyWith()));
//					break;
//			}
//		}
//		
//		public boolean done() {
//			return (step == 4);
//		}
//	}
	
	
	
//	private class RequestLaneForMovingCar extends Behaviour {
//		private Object[] vehicles = new Object[2];	// The offers from the four lanes, up/down or left/right
//		private AID[] revievers = new AID[2];
////		private AID bestSeller; // The agent who provides the best offer 
////		private int bestPrice;  // The best offered price
//		private int repliesCnt = 0; // The counter of replies from seller agents
//		private int vehCnt = 0;
//		private MessageTemplate mt; // The template to receive replies
//		private int step = 0;
//	
//		public void action() {
//			switch (step) {
//				case 0:
//					System.out.println("Sending ACL messages to all lanes");
//					// reset numbers of proposes from lanes
//					lanePro = 0;
//					// Send the cfp to all lanes
//					ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
//					for (int i = 0; i < laneAgents.length; ++i) {
//						cfp.addReceiver(laneAgents[i]);
//					}
//					if ( 0==traDir.compareTo("v") ) {
//						cfp.setContent( traDir + x);	
//					}
//					else {
//						cfp.setContent( traDir + y);
//					}
//					cfp.setConversationId(conIdTrade);
//					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
//					myAgent.send(cfp);
//					// Prepare the template to get proposals
//					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdTrade),
//	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
//					step = 1;
//					break;
//				case 1:
//					// Receive all proposals/refusals from lane agents
//					ACLMessage reply = myAgent.receive(mt);
//					if (reply != null) {
//						// Reply received
//						if (reply.getPerformative() == ACLMessage.INFORM) {
//							// This is an offer
//							lanePro++;
//							String dir = reply.getContent().substring(0,1);
//							int from = Integer.parseInt(reply.getContent().substring(1));
//							System.out.println("dir: " + dir + " from: " + from);
//							if( 0==dir.compareTo("i") ) {
//								try {
//									System.out.println(reply.getContentObject());
////									if ( Integer.parseInt(x)<from ) {
////										vehicles[0] = reply.getContentObject();
////										System.out.println("vehicles[0]");
////									}
////									else {
////										vehicles[1] = reply.getContentObject();
////										System.out.println("vehicles[1]");
////									}
//								}
//								catch (Exception ex) {
//									ex.printStackTrace();
//								}
//							}
//							else if( 0==dir.compareTo("o") ) {
//								if ( Integer.parseInt(x)>from ) {
//									revievers[0] = reply.getSender();
//									System.out.println(reply.getSender());
//								}
//								else {
//									revievers[1] = reply.getSender();
//									System.out.println(reply.getSender());
//								}								
//							}
//						}
//						repliesCnt++;
//						if (repliesCnt >= laneAgents.length) {
//							// We received all replies
//							System.out.println("Got anwsers from all lanes");
//							step = 4; 
//						}
//					}
//					else {
//						block();
//					}
//					break;
////				case 2:
////					System.out.println(revievers[0]);
////					System.out.println(revievers[1]);
////					
////					System.out.println("Setup for moving car");
////					// Send the purchase order to the seller that provided the best offer
////					ACLMessage order1 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
////					order1.addReceiver(revievers[0]);
////					order1.setConversationId(conIdTrade);
////					try {
////						order1.setContentObject((Serializable) vehicles[0]);
////					} catch (IOException e) {
////						e.printStackTrace();
////					}
////					order1.setReplyWith("order"+System.currentTimeMillis());
////					myAgent.send(order1);
////					// Send the purchase order to the seller that provided the best offer
////					ACLMessage order2 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
////					order2.addReceiver(revievers[1]);
////					order2.setConversationId(conIdTrade);
////					try {
////						order2.setContentObject((Serializable) vehicles[1]);
////					} catch (IOException e) {
////						e.printStackTrace();
////					}
////					order2.setReplyWith("order"+System.currentTimeMillis());
////					myAgent.send(order2);
////					
////					// Prepare the template to get the purchase order reply
////					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conIdTrade),
////							MessageTemplate.MatchInReplyTo(order1.getReplyWith()));
////					
////					vehCnt = 2;
////					step = 3;
////					break;
////				case 3:
////					System.out.println("Moving car");
////					// Receive the purchase order reply
////					reply = myAgent.receive(mt);
////					if (reply != null) {
////						// Purchase order reply received
////						if (reply.getPerformative() == ACLMessage.INFORM) {
////							// Purchase successful. We can terminate
////							System.out.println("Successfully moved vehicle.");
////						}
////						else {
////							System.out.println("Attempt failed: vehicle is not inserted.");
////						}
////						vehCnt--;
////						if ( vehCnt==0 )
////							step = 4;
////							System.out.println("Car moved");
////					}
////					else {
////						block();
////					}
////					break;
//			}        
//		}
//	
//		public boolean done() {
//			return (step == 4);
//		}
//	}
	

	private int gridX=1;
	private int gridY=1;
	
	public int inLane(int laneId, int crossId) {
		int returner = -1;
		if( laneId>=(crossId-1)*4 && laneId<crossId*4 ) {
			returner = laneId%4;
		}
		return returner;
	}
	
	public int outLane(int laneId, int crossId) {
		int returner = -1;
		
		// special case if cross on edge
		if( laneId!=0 ) { // lane 0 is never output lane
			if( laneId<0 ) {
				if( crossId<=gridX && -1*crossId==laneId ) { // on top edge
					returner = 0;
//					System.out.print("SPECIAL0 ");
				}
				else if( crossId>=(gridX*gridY-gridX) && -1*(gridX+gridY+(crossId-1)%gridX+1)==laneId ) { // on down edge
					returner = 1;
//					System.out.print("SPECIAL1 ");
				}
				else if( (crossId-1)%gridX==0 && -1*(gridX+gridY+gridX+crossId/gridX)==laneId ) { // on left edge
					returner = 2;
//					System.out.print("SPECIAL2 ");
				}
				else if( crossId%gridY==0 && -1*(gridX+crossId/gridX)==laneId ) { // on right edge
					returner = 3;
//					System.out.print("SPECIAL3 ");
				}
			}
			
			// normal case if cross is not on edge
			else if( (crossId-gridX)*4-1==laneId ) { // up
				returner = 0;
//				System.out.print("NORMAL0 ");
			}
			else if( (crossId+gridX)*4-3==laneId ) { // down
				returner = 1;
//				System.out.print("NORMAL1 ");
			}
			else if( ((crossId-1)*4-2)==laneId ) { // left
				returner = 2;
//				System.out.print("NORMAL2 ");
			}
			else if( ((crossId+1)*4-4)==laneId ) { // right
				returner = 3;
//				System.out.print("NORMAL3 ");
			}
		}
		
		return returner;
	}
}