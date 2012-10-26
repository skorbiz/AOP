package environment;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentController;

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
			
			// Set-up lane agents
			args = new Object[] {"h","1","0","1"};
			createAgent("h101","environment.Lane",args);
			args = new Object[] {"h","1","1","0"};
			createAgent("h110","environment.Lane",args);
			args = new Object[] {"h","1","1","2"};
			createAgent("h112","environment.Lane",args);
			args = new Object[] {"h","1","2","1"};
			createAgent("h121","environment.Lane",args);
			args = new Object[] {"v","1","0","1"};
			createAgent("v101","environment.Lane",args);
			args = new Object[] {"v","1","1","0"};
			createAgent("v110","environment.Lane",args);
			args = new Object[] {"v","1","1","2"};
			createAgent("v112","environment.Lane",args);
			args = new Object[] {"v","1","2","1"};
			createAgent("v121","environment.Lane",args);
			
			// Set-up cross agent
			args = new Object[] {"1","1"};
			createAgent("11","environment.Cross",args);
			
//			System.out.println("SetUpAgents OneShotBehaviour ended");
		}
	}
}
