package gui;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import gui.GUIInterface;

public class GUIAgent extends Agent 
{
	private String typeOfAgent = "GUIAgent";			// The type of the agent
	GUIInterface guiInterface = new GUIInterface();		// The gui interface
    DFAgentDescription[] langeAgents;					// A list of all Lane agents

	Behaviour guiUpdateBehavior = new TickerBehaviour( this, 2000 )
    {
		public void onStart(){
        	System.out.println("Created GUI agent");
			updateLaneAgents();
        }
        
		protected void onTick() {
			System.out.println("UPDATE GUI");
			updateCars();
			requestCars();
        }
    };
	
	// Initialisation
	protected void setup() 
	{
		// Printout a welcome message
		System.out.println(typeOfAgent + getAID().getName()+" is ready.");
	
		//Start-up arguments 
		Object[] args = getArguments();
		if (args != null && args.length > 0) 
		{
			// Add a TickerBehaviour that updates the gui
			addBehaviour(guiUpdateBehavior);
		}
		else 
		{
			// Make the agent terminate
			System.out.println("Not all parameters are specified");
			doDelete();
		}
	}

	private void updateLaneAgents()
	{	
		//Get lane agents
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "lane" );
        dfd.addServices(sd);
        try 
        {
        	langeAgents = DFService.search(this, dfd);
		} 
        catch (FIPAException e) { e.printStackTrace(); }
	}
	
	
	private void requestCars()
	{
		//Sends mesages to all lanes
	     ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
	     msg.setContent("11");
	     //msg.setConversationId("cars in lane");
		 //msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
	     for (int i = 0; i< langeAgents.length; i++)
	        msg.addReceiver( langeAgents[i].getName() );
	     send(msg);	     
	}
	
	
	private void updateCars()
	{
		 ACLMessage reply = receive( MessageTemplate.MatchPerformative(ACLMessage.PROPOSE) );
		 int[] cars = new int[5*4*4];

		 for(int i = 0; reply != null; i++)
		 {
			 System.out.println(i);
			 
			 int offer1 = 0;
			 int offer2 = 0;
			 
			 //Johan Left work here! start working with identifing the propper lanes
			 System.out.println(reply.getSender().getLocalName());
			 
			 String ori = reply.getContent().substring(0,1);
			 int offer = Integer.parseInt(reply.getContent().substring(1));
			 if( 0==ori.compareTo("v") )
				 offer1 += offer;
					
			 else if(0==ori.compareTo("h"))
				 offer2 += offer;
				
			cars[0] = offer1;
			cars[1] = offer2;
			cars[2] = 42;
			 
			reply = receive( MessageTemplate.MatchPerformative(ACLMessage.PROPOSE) );
				
		 }
		guiInterface.updateCars( cars );
	}
	
}
