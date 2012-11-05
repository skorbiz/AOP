package environment;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentController;

import gui.GUIInterface;

public class MainAgent extends Agent {
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Main-agent " + getAID().getName() + " is ready.");

		addBehaviour(new SetUpAgents());
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Main-agent " + getAID().getName() + " terminating.");
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
			
			// Set-up normal lane agents
			for(int i=0; i<4; i++) {
				String stg = Integer.toString(i);
				args = new Object[] {i};
				createAgent("Lane"+stg,"environment.Lane",args);
			}
			// Set-up special lane agents
			for(int i=-1; i>-5; i--) {
				String stg = Integer.toString(i);
				args = new Object[] {i};
				createAgent("Lane"+stg,"environment.Lane",args);
			}
			
			// Set-up cross agent
			args = new Object[] {1};
			createAgent("Cross"+"1","environment.Cross",args);

			//Set-up gui agent
//			args = new Object[] {"GUI"};
//			createAgent("GUI","gui.GUIAgent", args);
			
//			System.out.println("SetUpAgents OneShotBehaviour ended");
			
			doDelete();
		}
	}
}
