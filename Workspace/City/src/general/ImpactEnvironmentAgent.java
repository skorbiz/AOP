package general;

import java.io.IOException;
import java.io.Serializable;

import vehicle.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import general.Settings;
import gui.GUIInterface;

public class ImpactEnvironmentAgent extends Agent 
{
	private String typeOfAgent = "ImpactEnvirenmentAgent";			// The type of the agent
    DFAgentDescription[] ingoingLaneAgents;							// A list of all Lane agents

    private int insertNewCarInterval = Settings.insertNewCarInterval;
    private int insertNewCarIntervalWithRandomness = Settings.insertNewCarInterval;
    private long startTime = System.currentTimeMillis();
    
    /******************** Setup the agent ********************/
	Behaviour requestFreeSpaceBehaviour = new WakerBehaviour( this, insertNewCarIntervalWithRandomness )
    {
		protected void onWake(){
			requestInsertCarInRandomLane();
			calculateNewInsertionTime();
			reset(insertNewCarInterval);
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
		updateOuterInputLaneAgents();	

		addBehaviour(requestFreeSpaceBehaviour);
		addBehaviour(insertCarIfSpaceBehaviour);

	}

	
    /******************** Support functions ********************/
	//Calculates a new insertion time
	private void calculateNewInsertionTime()
	{
		//Add a bit of randomness to the time
		int temp = (int) (Math.random()*insertNewCarInterval/2);
		temp -= insertNewCarInterval/4;
		insertNewCarIntervalWithRandomness += temp;
		
		//If settings is set to constant insertion time to do nothing
		if(Settings.changeInsertionTimeEverySample == false)
			return;
		
		if(startTime + Settings.timeBetweenSamplingsInMilliSeconds < System.currentTimeMillis())
		{
			if(Settings.printStartOfNewSamples)
				System.out.println("Impact agent started new sample");
			insertNewCarInterval += Settings.changeInInsertionTimeEverySample;
			startTime += Settings.timeBetweenSamplingsInMilliSeconds;
		}
	}
	
	
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

	
	private void requestInsertCarInRandomLane()
	{	
		int numberOfCarsToBeInserted = (int) (Math.random()*4);
		int laneToInsertCarsInTo = (int) (Math.random()*ingoingLaneAgents.length);
		
		for(int i = 0; i < numberOfCarsToBeInserted; i++)
		{
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		    msg.setContent(Settings.CrossToLaneRequesSpaces);	
		    msg.addReceiver( ingoingLaneAgents[ laneToInsertCarsInTo ].getName() );
		    send(msg);
		}
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
