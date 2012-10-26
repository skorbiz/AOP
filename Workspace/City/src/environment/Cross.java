package environment;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Cross extends Agent {
	
	// The the identifier of the cross
	private String x;
	private String y;
	// Which agents the cross want to find
	private String agentToFind = "lane";
	// The list of known seller agents
	private AID[] laneAgents;
	// Id of the conversation
	private String conId = "lane-trade";
	// Number of PROPOSE agent
	private int lanePro = 0;
	// traffic direction, 0 for vertical, 1 for horizontal
	private int traDir = 0;
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
			x = (String) args[0];
			y = (String) args[1];
			System.out.println("cross identifier are: " + x + y);
	
			// Add a TickerBehaviour that schedules a request to seller agents every minute
			addBehaviour(new TickerBehaviour(this, 5000) {
				protected void onTick() {
					ticks++;
					if( ticks%2==0 ) {
//						System.out.println("Finding: " + agentToFind + " agents.");
						// Update the list of seller agents
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
//								System.out.println(laneAgents[i].getName());
							}
						}
						catch (FIPAException fe) {
							fe.printStackTrace();
						}
	          
						// Perform the request
						myAgent.addBehaviour(new RequestLaneForPrices());	
					}
					else {
//						System.out.println("Finding: " + agentToFind + " agents.");
						// Update the list of seller agents
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
//								System.out.println(laneAgents[i].getName());
							}
						}
						catch (FIPAException fe) {
							fe.printStackTrace();
						}
	          
						// Perform the request
						myAgent.addBehaviour(new RequestLaneForMovingCar());	
					}
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
  

	private class RequestLaneForPrices extends Behaviour {
		private int[] offers = new int[2];	// The offers from the four lanes, up/down or left/right
//		private AID bestSeller; // The agent who provides the best offer 
//		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
	
		public void action() {
			switch (step) {
				case 0:
					// Send the cfp to all lanes
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < laneAgents.length; ++i) {
						cfp.addReceiver(laneAgents[i]);
					} 
					cfp.setContent(x + y);
					cfp.setConversationId(conId);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conId),
	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					// Receive all proposals/refusals from lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							lanePro++;
							// This is an offer 
							String ori = reply.getContent().substring(0,1);
							int offer = Integer.parseInt(reply.getContent().substring(1));
//							if (bestSeller == null || offer < bestPrice) {
//								// This is the best offer at present
//								bestPrice = offer;
//								bestSeller = reply.getSender();
//							}
							if( 0==ori.compareTo("v") ) {
								offers[0] += offer;
							}
							else if(0==ori.compareTo("h")) {
								offers[1] += offer;
							}
//							System.out.println("Lane " + reply.getSender().getName() + " offers: " + offer + ".");
//							System.out.println("Offers is now " + offers[0] + " vs. " + offers[1]);
						}
						repliesCnt++;
						if (repliesCnt >= laneAgents.length) {
							// We received all replies
							step = 2; 
						}
					}
					else {
						block();
					}
					break;
				case 2:
					System.out.println("Decision based on: " + offers[0] + " + " + chaPri + " > " + offers[1] + ", from " + lanePro + " agent(s).");
					if( offers[0]+chaPri>offers[1] ) { // vertical
						chaPri = 2;
						traDir = 0;
						System.out.println("Trafic direction: up/down. Price to change is " + chaPri + " and traffic direction is " + traDir + ".");
					}
					else { // horizontal
						chaPri = -2;
						traDir = 1;
						System.out.println("Trafic direction: left/right. Price to change is " + chaPri + " and traffic direction is " + traDir + ".");
					}
					lanePro = 0;
					System.out.println("");
//					System.out.println("Travel direction: " + traDir);
//					// Send the purchase order to the seller that provided the best offer
//					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//					order.addReceiver(bestSeller);
//					order.setContent("The-Lord-of-the-rings");
//					order.setConversationId(conId);
//					order.setReplyWith("order"+System.currentTimeMillis());
//					myAgent.send(order);
//					// Prepare the template to get the purchase order reply
//					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conId),
//							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 4;
					break;
				case 3:      
					// Receive the purchase order reply
					reply = myAgent.receive(mt);
					if (reply != null) {
						// Purchase order reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Purchase successful. We can terminate
							System.out.println(x+y + " successfully purchased from agent " + reply.getSender().getName());
//							System.out.println("Price = " + bestPrice);
//							myAgent.doDelete();
						}
						else {
							System.out.println("Attempt failed: requested book already sold.");
						}
						step = 4;
					}
					else {
						block();
					}
					break;
			}        
		}
	
		public boolean done() {
			return (step == 4);
		}
	}  // End of inner class RequestPerformer
	
	
	
	private class RequestLaneForMovingCar extends Behaviour {
		private int[] offers = new int[2];	// The offers from the four lanes, up/down or left/right
//		private AID bestSeller; // The agent who provides the best offer 
//		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
	
		public void action() {
			switch (step) {
				case 0:
					// Send the cfp to all lanes
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < laneAgents.length; ++i) {
						cfp.addReceiver(laneAgents[i]);
					} 
					cfp.setContent(x + y);
					cfp.setConversationId(conId);
					cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conId),
	                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					// Receive all proposals/refusals from lane agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							lanePro++;
							// This is an offer 
							String ori = reply.getContent().substring(0,1);
							int offer = Integer.parseInt(reply.getContent().substring(1));
//							if (bestSeller == null || offer < bestPrice) {
//								// This is the best offer at present
//								bestPrice = offer;
//								bestSeller = reply.getSender();
//							}
							if( 0==ori.compareTo("v") ) {
								offers[0] += offer;
							}
							else if(0==ori.compareTo("h")) {
								offers[1] += offer;
							}
//							System.out.println("Lane " + reply.getSender().getName() + " offers: " + offer + ".");
//							System.out.println("Offers is now " + offers[0] + " vs. " + offers[1]);
						}
						repliesCnt++;
						if (repliesCnt >= laneAgents.length) {
							// We received all replies
							step = 2; 
						}
					}
					else {
						block();
					}
					break;
				case 2:
					System.out.println("Decision based on: " + offers[0] + " + " + chaPri + " > " + offers[1] + ", from " + lanePro + " agent(s).");
					if( offers[0]+chaPri>offers[1] ) { // vertical
						chaPri = 2;
						traDir = 0;
						System.out.println("Trafic direction: up/down. Price to change is " + chaPri + " and traffic direction is " + traDir + ".");
					}
					else { // horizontal
						chaPri = -2;
						traDir = 1;
						System.out.println("Trafic direction: left/right. Price to change is " + chaPri + " and traffic direction is " + traDir + ".");
					}
					lanePro = 0;
					System.out.println("");
//					System.out.println("Travel direction: " + traDir);
//					// Send the purchase order to the seller that provided the best offer
//					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//					order.addReceiver(bestSeller);
//					order.setContent("The-Lord-of-the-rings");
//					order.setConversationId(conId);
//					order.setReplyWith("order"+System.currentTimeMillis());
//					myAgent.send(order);
//					// Prepare the template to get the purchase order reply
//					mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conId),
//							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 4;
					break;
				case 3:      
					// Receive the purchase order reply
					reply = myAgent.receive(mt);
					if (reply != null) {
						// Purchase order reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Purchase successful. We can terminate
							System.out.println(x+y + " successfully purchased from agent " + reply.getSender().getName());
//							System.out.println("Price = " + bestPrice);
//							myAgent.doDelete();
						}
						else {
							System.out.println("Attempt failed: requested book already sold.");
						}
						step = 4;
					}
					else {
						block();
					}
					break;
			}        
		}
	
		public boolean done() {
			return (step == 4);
		}
	}  // End of inner class RequestPerformer
}