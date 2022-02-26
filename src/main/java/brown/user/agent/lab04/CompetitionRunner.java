package brown.user.agent.lab04;

import brown.system.setup.library.Setup;
import brown.user.agent.lab04.generic.MyLemonadeAgent;
import brown.user.agent.lab04.rl.MyLemonadeRLAgent;

public class CompetitionRunner {
	public static void main(String[] args) {
		
		// TODO: comment this out if you did NOT implement MyLemonadeRLAgent.java,
		// and instead implemented the "generic" agent.
		runGenericAgent(args);
		
		
		// TODO: comment this out if you did NOT implement MyLemonadeAgent.java,
		// and instead implemented the RL agent.
		runRLAgent(args);
	}
	
	public static void runRLAgent(String[] args) {
		String NAME = MyLemonadeRLAgent.NAME;
		String HOST = "cslab1a";
        int PORT = 2121;

        if (args.length >= 2) {
            HOST = args[0];
            PORT = Integer.parseInt(args[1]);
        }

		new MyLemonadeRLAgent(HOST, PORT, new Setup(), NAME);
		
		while (true) {}
	}
	
	public static void runGenericAgent(String[] args) {
		String NAME = MyLemonadeAgent.NAME;
		String HOST = "cslab1a";
        int PORT = 2121;

        if (args.length >= 2) {
            HOST = args[0];
            PORT = Integer.parseInt(args[1]);
        }

		new MyLemonadeAgent(HOST, PORT, new Setup(), NAME);
		
		while (true) {}
	}
}
