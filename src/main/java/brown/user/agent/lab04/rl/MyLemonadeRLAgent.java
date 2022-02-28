package brown.user.agent.lab04.rl; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import brown.auction.rules.utility.LemonadeUtility;
import brown.communication.action.IGameAction;
import brown.communication.action.library.GameAction;
import brown.communication.messages.IActionMessage;
import brown.communication.messages.IActionRequestMessage;
import brown.communication.messages.IInformationMessage;
import brown.communication.messages.ISimulationReportMessage;
import brown.communication.messages.ITypeMessage;
import brown.communication.messages.library.ActionMessage;
import brown.platform.accounting.IAccountUpdate;
import brown.simulations.BasicSimulation;
import brown.system.setup.ISetup;
import brown.user.agent.IAgent;
import brown.user.agent.library.AbsAgent;
import brown.user.agent.library.offline.AbsOfflineLearningAgent;
import brown.user.agent.library.offline.BasicOfflineGame;
import brown.user.agent.library.offline.MysteryLemonadeOpponent;
import brown.user.agent.library.offline.OfflineGame;
import brown.user.agent.library.offline.OfflineStagHunt;

public class MyLemonadeRLAgent extends AbsAgent implements IAgent {
	public static final String NAME = ""; // TODO: name your agent
	
	// feel free to change these
	private static final int NUM_TRAINING_ITERATIONS = 100000;
	private static final int NUM_ITERATIONS_PER_PRINT = 1000;
	
	private static final int NUM_POSSIBLE_STATES = 1; // TODO: change this to the correct value.
	private static final int NUM_POSSIBLE_ACTIONS = 12;
	private static final int INITIAL_STATE = 0;
	
	// feel free to change these
	private static double LEARNING_RATE = 0.05;
	private static double DISCOUNT_FACTOR = 0.90;
	private static double EXPLORATION_RATE = 0.05;
	
	private class Learner extends RL {
		public Learner(int numPossibleStates, int numPossibleActions, int initialState,
				double learningRate, double discountFactor, double explorationRate, boolean trainingMode) {
			super(numPossibleStates, numPossibleActions, initialState, learningRate, discountFactor,
					explorationRate, trainingMode);
		}

		@Override
		public int determineState() {
			// TODO(Task 1):
			// Create a state representation of the lemonade game.
			
			// Don't forget to change NUM_POSSIBLE_STATES!
			int play1 = 0;
			int play2 = 0;
			int bestPlay;
			// feel free to use these lists for move history.
			// if you want to look back multiple moves, please make sure you don't go out-of-bounds.
			List<Integer> myActions = this.getMyActions();
			List<Integer> opponent1Actions = this.getOpponentActions(0);
			List<Integer> opponent2Actions = this.getOpponentActions(1);
			List<Double> myRewards = this.getMyRewards();
			List<Integer> myStates = this.getMyStates();
			switch (myActions.size()) {
				case 0:
					return 0;
				case 1:
					play1 = ((opponent1Actions.get(0) + opponent2Actions.get(0)) / 2) % 12;
					play2 = (play1 + 6) % 12;
					bestPlay = (Math.abs(play1 - opponent1Actions.get(0)) > Math.abs(play2 - opponent1Actions.get(0)) ? play1 : play2);
				case 2:

				default:
			}
			// ...
			return 1;
		}
		
		public void train() {
			System.out.println("Training...");
			OfflineGame LEMONADE = new BasicOfflineGame(new LemonadeUtility());
			
			AbsOfflineLearningAgent opponentAgent1 = new MysteryLemonadeOpponent(); // TODO: feel free to change your opponent for training
			AbsOfflineLearningAgent opponentAgent2 = new MysteryLemonadeOpponent(); // TODO: feel free to change your opponent for training
			
			for (int i = 0; i < NUM_TRAINING_ITERATIONS / NUM_ITERATIONS_PER_PRINT; i++) {
				AbsOfflineLearningAgent.train(LEMONADE, Arrays.asList(this, opponentAgent1, opponentAgent2), NUM_ITERATIONS_PER_PRINT);
				List<Double> myRewards = this.getMyRewards();
				double totalReward = 0;
				for (int j = 1; j <= NUM_ITERATIONS_PER_PRINT; j++) {
					totalReward += myRewards.get(myRewards.size() - j);
				}
				// Uncomment below if you want to print your training results.
				// If your agent trains and/or plays against itself, this will print twice, once per agent.
				
				// System.out.println("Average reward for last " + NUM_ITERATIONS_PER_PRINT + " iterations of training: " + (totalReward / NUM_ITERATIONS_PER_PRINT));
			}
			
			System.out.println("Done training.");
		}
		
	}
	
	private final Learner learner;
	private int auctionID;
	
	public MyLemonadeRLAgent(String host, int port, ISetup gameSetup, String name) {
		super(host, port, gameSetup, name);
		
		this.auctionID = 0;
		this.learner = new Learner(NUM_POSSIBLE_STATES, NUM_POSSIBLE_ACTIONS, INITIAL_STATE, LEARNING_RATE, DISCOUNT_FACTOR, EXPLORATION_RATE, true);
		this.learner.train();
		this.learner.setTrainingMode(false); // set to competition mode after pre-training.
		
		// uncomment this print statement to have your program indicate when it's done training
		// System.out.println("training mode off; entering competition");
	}
	
	

	@Override
	public void onInformationMessage(IInformationMessage informationMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActionRequestMessage(IActionRequestMessage tradeRequestMessage) {
		// TODO Auto-generated method stub
		Integer auctionID = tradeRequestMessage.getAuctionID();
		this.auctionID = auctionID;
        // some basic unbalanced probabilities.
        IGameAction action = new GameAction(this.learner.nextMove());
        IActionMessage actionMessage = new ActionMessage(-1, this.ID, auctionID, action);
        this.CLIENT.sendTCP(actionMessage);
		
	}

	@Override
	public void onTypeMessage(ITypeMessage valuationMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSimulationReportMessage(ISimulationReportMessage msg) {
		// TODO Auto-generated method stub
		Map<Integer, Integer> actions = new HashMap<>();
		Map<Integer, Double> rewards = new HashMap<>();
	    for (IActionMessage act : msg.getMarketResults().get(this.auctionID).getTradeHistory().get(0)) {
		    actions.put(act.getAgentID(), ((GameAction)act.getBid()).getAction());
	    }
	    
	    for (IAccountUpdate act : msg.getMarketResults().get(this.auctionID).getUtilities()) {
		    rewards.put(act.getTo(), act.getCost());
	    }
	    
	    this.learner.updateHistory(actions, rewards, this.publicID);
	    this.learner.afterRound();
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Running test 'competition'. Will train first.");
		
		List<String> agents = Arrays.asList(MyLemonadeRLAgent.class.getCanonicalName(), MyLemonadeRLAgent.class.getCanonicalName(), MyLemonadeRLAgent.class.getCanonicalName());
		List<String> names = Arrays.asList(NAME + "_0", NAME + "_1", NAME + "_2");
        new BasicSimulation(agents, names, "input_configs/lemonade_game.json", 2121, "outfile", false).run();
	}
}
