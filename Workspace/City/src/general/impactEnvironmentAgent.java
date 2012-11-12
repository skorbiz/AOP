package general;

import java.io.IOException;
import java.io.Serializable;

import vehicle.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import general.Settings;
import gui.GUIInterface;

public class impactEnvironmentAgent extends Agent 
{
	private String typeOfAgent = "impactEnvirenmentAgent";			// The type of the agent
    DFAgentDescription[] ingoingLaneAgents;							// A list of all Lane agents

    
    /******************** Setup the agent ********************/
	Behaviour RequestFreeSpaceBehaviour = new TickerBehaviour( this, 500 )
    {
		public void onStart(){
			updateOuterInputLaneAgents();	
        }
		
		protected void onTick(){
			requestIfRandomLaneHasSpace();
        }
    };
	
    Behaviour insertCarIfSpaceBehaviour = new CyclicBehaviour()
    {
		public void action(){
			insertCar();
		}
    };
    
	protected void setup() 
	{
		//System.out.println(typeOfAgent + getAID().getName()+" is ready.");
		addBehaviour(RequestFreeSpaceBehaviour);
		addBehaviour(insertCarIfSpaceBehaviour);

	}

	
    /******************** Support functions ********************/
	//Get outer input lane agents	
	private void updateOuterInputLaneAgents()
	{	
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "lane" );
        dfd.addServices(sd);
        DFAgentDescription[] allLaneAgents;
        try 
        {
        	allLaneAgents = DFService.search(this, dfd);
        	ingoingLaneAgents = new DFAgentDescription[Settings.sizex*2+Settings.sizey*2];
        	int[] idsOfInputLanes = Settings.getOuterInputLanes();
        	int index = 0; 
        	
            for(int i = 0; i < allLaneAgents.length; i++)
            	for(int k = 0; k < idsOfInputLanes.length; k++)
            		if(Settings.covertLocalLaneNameToInt(allLaneAgents[i].getName().getLocalName()) == idsOfInputLanes[k])
            		{
                    	ingoingLaneAgents[index] = allLaneAgents[i];
                    	index++;
            		}
		} 
        catch (FIPAException e) { e.printStackTrace(); }
    }
	
	//Send request to random lane if it has space to insert car
	private void requestIfRandomLaneHasSpace()
	{		
	    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	    msg.setContent(Settings.CrossToLaneRequesSpaces);	
	    msg.addReceiver( ingoingLaneAgents[ (int) (Math.random()*ingoingLaneAgents.length) ].getName() );
	    send(msg);
	}
	
	//Inserts cars into lanes if the respond with free space
	private void insertCar()
	{
		MessageTemplate mt = MessageTemplate.MatchPerformative( ACLMessage.INFORM );	
		ACLMessage reply = receive( mt );
		
		if(reply != null)	
		{		 
			int emptySpaces = Integer.parseInt(reply.getContent());
			if(emptySpaces > 0)
				try 
				{
					ACLMessage msg = reply.createReply();
					msg.setPerformative(ACLMessage.PROPAGATE);
					msg.setContentObject((Serializable) new Vehicle());
					send(msg);
				} 
				catch (IOException e) { e.printStackTrace(); }
		}
		else
			insertCarIfSpaceBehaviour.block();		 
	}
}
