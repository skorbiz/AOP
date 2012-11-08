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

import general.Settings;
import gui.GUIInterface;

public class GUIAgent extends Agent 
{
	private String typeOfAgent = "GUIAgent";			// The type of the agent
	GUIInterface guiInterface = new GUIInterface();		// The gui interface
    DFAgentDescription[] laneAgents;					// A list of all Lane agents
    DFAgentDescription[] crossAgents;					// A list of all cross agents

    //Jade communications
    private String lanesRelyWithNumberOfCars = "Reply with number of cars";
    private String crossRelyWithLights = "Reply with number of cars";

	Behaviour guiUpdateBehavior = new TickerBehaviour( this, 2000 )
    {
		public void onStart(){
        	System.out.println("Created GUI agent");
			updateCrossAgents();
			updateLaneAgents();
        }
        
		protected void onTick() {
			updateCars();
			updateLights();
			requestCars();
			requestLights();
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

	//Get lane agents	
	private void updateLaneAgents()
	{	
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "lane" );
        dfd.addServices(sd);
        try 
        {
        	laneAgents = DFService.search(this, dfd);
		} 
        catch (FIPAException e) { e.printStackTrace(); }
	}

	//Get cross agents
	private void updateCrossAgents()
	{	
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "cross" );
        dfd.addServices(sd);
        try 
        {
        	crossAgents = DFService.search(this, dfd);
		} 
        catch (FIPAException e) { e.printStackTrace(); }
	}

	//Sends mesages to all lanes	
	private void requestCars()
	{
	     ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	     msg.setReplyWith(lanesRelyWithNumberOfCars);
	     msg.setContent(Settings.GuiToLaneRequestCars);
	     for (int i = 0; i< laneAgents.length; i++)
	        msg.addReceiver( laneAgents[i].getName() );
	     send(msg);	     
	}

	//Sends mesages to all crosses
	private void requestLights()
	{
	     ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	     msg.setReplyWith(crossRelyWithLights);
	     msg.setContent(Settings.GuiToCrossRequestLights);
	     for (int i = 0; i< crossAgents.length; i++)
	        msg.addReceiver( crossAgents[i].getName() );
	     send(msg);	     
	}
	
	private void updateLights()
	{		 
		 int[] lights = new int[Settings.sizex*Settings.sizey];				// Create array for the recived data
		 for(int i = 0; i < lights.length; i++)								// Initialises with -1 to indicate no change
			 lights[i] = -1;												//	

		 //Convert all recived ansewers to fit in the data array
		 MessageTemplate mt = MessageTemplate.and(  
					MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
					MessageTemplate.MatchInReplyTo(crossRelyWithLights));
	
		 ACLMessage reply = receive( mt );

		 for(int i = 0; reply != null; i++)
		 {			 			 
			 //Get lane numbers if not outer lanes
			 int crossNumber = Settings.covertLocalCrossNameToInt(reply.getSender().getLocalName() );
			 
			 //Get and save number of cars in the lane
			 int light = Integer.parseInt(reply.getContent());
			 lights[crossNumber] = light;
						 
			 //Retrieve new message
			 reply = receive( mt );
				
		 }			 
		 guiInterface.updateLights( lights );
	}
	
	
	
	private void updateCars()
	{		 
		 int[] cars = new int[Settings.sizex*Settings.sizey*4];				// Create array for the recived data
		 for(int i = 0; i < cars.length; i++)								// Initialises with -1 to indicate no change
			 cars[i] = -1;													//	

		 //Convert all recived ansewers to fit in the data array
		 MessageTemplate mt = MessageTemplate.and(  
					MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
					MessageTemplate.MatchInReplyTo(lanesRelyWithNumberOfCars));
	
		 ACLMessage reply = receive( mt );

		 for(int i = 0; reply != null; i++)
		 {			 			 
			 //Get lane numbers if not outer lanes
			 int laneNumber = Settings.covertLocalLaneNameToInt(reply.getSender().getLocalName() );
			 if(laneNumber < 0)
			 {
				 reply = receive( mt );
				 continue;
			 }
			 
			 //Get and save number of cars in the lane
			 int carsInLane = Integer.parseInt(reply.getContent());
			 cars[laneNumber] = carsInLane;
						 
			 //Retrieve new message
			 reply = receive( mt );
				
		 }
		guiInterface.updateCars( cars );
	}
	
}
