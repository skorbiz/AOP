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
	// The type of the agent
	private String typeOfAgent = "GUIAgent";
	
	//The gui interface
	GUIInterface guiInterface = new GUIInterface();


	Behaviour guiUpdateBehavior = new TickerBehaviour( this, 5000 )
    {
        public void onStart()
        {
        	System.out.println("Create GUI");
        }
        
		protected void onTick() 
        {
			System.out.println("UPDATE GUI");
			try 
			{
				requestGuiParameters();
			} 
			catch (FIPAException e) { e.printStackTrace(); }
        }
    };
	
	// Initialisation
	protected void setup() 
	{
		// Printout a welcome message
		System.out.println("GUI agent "+getAID().getName()+" is ready.");
	
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
	
	
	private void requestGuiParameters() throws FIPAException
	{
		System.out.println("Requst gui parameters");
		
		//Get lane agents
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "lane" );
        dfd.addServices(sd);
        DFAgentDescription[] langeAgents;
        langeAgents = DFService.search(this, dfd);
        
		//Sends mesages to all lanes
	     ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
	     msg.setContent("11");// "bla...bla...bla" );
	     //msg.setConversationId("cars in lane");
		 //msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
	     for (int i = 0; i< langeAgents.length; i++)
	        msg.addReceiver( langeAgents[i].getName() );
	     send(msg);
	     
	     //Prepare to get proposals
		 MessageTemplate msgReply = MessageTemplate.and(
				 										MessageTemplate.MatchConversationId("cars in lane"),
				 										MessageTemplate.MatchInReplyTo(msg.getReplyWith())
				 										);
	     

	     //Update latest replys
		 ACLMessage reply = receive( MessageTemplate.MatchPerformative(ACLMessage.PROPOSE) );
		 if (reply == null)
			 return;

		 int offer1 = 0;
		 int offer2 = 0;	
		 
		 String ori = reply.getContent().substring(0,1);
		 int offer = Integer.parseInt(reply.getContent().substring(1));
		 System.out.println(offer);
		 if( 0==ori.compareTo("v") )
			 offer1 += offer;
				
		 else if(0==ori.compareTo("h"))
			 offer2 += offer;

		int[] lights = new int[5*4];
		int[] cars = new int[5*4*4];
		
		cars[0] = offer1;
		cars[1] = offer2;
		cars[2] = 42;
		
		guiInterface.updateFrame(lights, cars);
	}
	
	
	
	
}
