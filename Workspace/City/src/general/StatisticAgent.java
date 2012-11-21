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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import general.Settings;

public class StatisticAgent extends Agent 
{
	private String typeOfAgent = "StatisticAgent";						// The type of the agent
    DFAgentDescription[] outgoingLaneAgents;							// A list of all Lane agents
    StatisticInterface statistic = StatisticInterface.getInstance();	// Singleton constructer for the class used for applying statistics 
    
    /******************** Setup the agent ********************/
	Behaviour behaviourRequestAllOutgoingCars = new TickerBehaviour( this, 500 )
    {
		public void onStart()
		{
			updateOuterOutputLaneAgents();	
        }

		protected void onTick()
		{
			requestAllOutputVehicle();
        }
    };
	
    Behaviour behaviourProcessRecivedCars = new CyclicBehaviour()
    {
		public void action()
		{
			statistic.checkAndInisiateNewSample();
			recivedOutputVehicle();
		}
    };
    
	protected void setup() 
	{
		//System.out.println(typeOfAgent + getAID().getName()+" is ready.");
		addBehaviour(behaviourRequestAllOutgoingCars);
		addBehaviour(behaviourProcessRecivedCars);

	}

	
    /******************** Support functions ********************/
	//Get outer output lane agents	
	private void updateOuterOutputLaneAgents()
	{	
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "lane" );
        dfd.addServices(sd);
        DFAgentDescription[] allLaneAgents;
        try 
        {
        	allLaneAgents = DFService.search(this, dfd);
        	outgoingLaneAgents = new DFAgentDescription[Settings.sizex*2+Settings.sizey*2];
        	int[] idsOfInputLanes = Settings.getSpecialCaseOutputLanes();
        	int index = 0; 
        	
            for(int i = 0; i < allLaneAgents.length; i++)
            	for(int k = 0; k < idsOfInputLanes.length; k++)
            		if( idsOfInputLanes[k] == Settings.covertLocalLaneNameToInt( allLaneAgents[i].getName().getLocalName() ) )
            		{
                    	outgoingLaneAgents[index] = allLaneAgents[i];
                    	index++;
            		}
		} 
        catch (FIPAException e) { e.printStackTrace(); }					
    }

	
	private void requestAllOutputVehicle()
	{
		for (int i = 0; i< outgoingLaneAgents.length; i++)
			requestOutputVehicle( outgoingLaneAgents[i].getName() );
	}
	
	private void requestOutputVehicle(AID agentName)
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent(Settings.CrossToLaneRequestRetrieveVehicle);
	    msg.addReceiver( agentName );
		send(msg);
	}
	
	private void recivedOutputVehicle()
	{	
		MessageTemplate mt = MessageTemplate.MatchPerformative( ACLMessage.INFORM );	
		ACLMessage reply = receive(mt);


		if(reply != null)	
		{
			try 
			{
				requestOutputVehicle( reply.getSender() );				//See if lane has more cars
				Vehicle vehicle = (Vehicle) reply.getContentObject();
				statistic.addVehicle(vehicle);
			} catch (UnreadableException e) { e.printStackTrace(); }
			
		}
		else
			behaviourProcessRecivedCars.block();		
	}

}	
