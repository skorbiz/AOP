package environment;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentController;

import general.Settings;
import gui.GUIInterface;

public class MainAgent extends Agent {
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Main-agent " + getAID().getLocalName() + " is ready.");

		addBehaviour(new SetUpAgents());
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Main-agent " + getAID().getLocalName() + " terminating.");
	}
	
	private void setUpAgent(String agentName,String className,Object[] arguments) throws StaleProxyException 
	{
		((AgentController)getContainerController().createNewAgent(agentName,className,arguments)).start();
	}
	
	private void createAgent(String agentName,String className,Object[] arguments)
	{
		try {
			setUpAgent(agentName,className,arguments);
		}
		catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	private class SetUpAgents extends OneShotBehaviour {
		public void action() {
//			System.out.println("SetUpAgents OneShotBehaviour stated");
			Object[] args;
			
			int numberOfNormalInLanes = Settings.sizex*Settings.sizey*4;
			int numberOfSpecialInLanes = Settings.sizex*2 + Settings.sizey*2;
			int numberOfCrosses = Settings.sizex*Settings.sizey;
			
			// Set-up normal lane agents
			for(int i=0; i<numberOfNormalInLanes; i++) {
				String stg = Integer.toString(i);
				args = new Object[] {i};
				createAgent("Lane"+stg,"environment.Lane",args);
			}
			// Set-up special lane agents
			for(int i=-1; i>-numberOfSpecialInLanes-1; i--) {
				String stg = Integer.toString(i);
				args = new Object[] {i};
				createAgent("Lane"+stg,"environment.Lane",args);
			}
			
			// Set-up cross agent
			for(int i=1; i<=numberOfCrosses; i++) {
				String stg = Integer.toString(i);
				args = new Object[] {i};
				createAgent("Cross"+stg,"environment.Cross",args);
			}
			
			//Set-up gui agent
			args = new Object[] {"GUI"};
			createAgent("GUI","gui.GUIAgent", args);
			
//			System.out.println("SetUpAgents OneShotBehaviour ended");
			
			doDelete();
		}
	}
}
