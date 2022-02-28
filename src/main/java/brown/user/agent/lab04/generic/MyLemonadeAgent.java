package brown.user.agent.lab04.generic; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

public class MyLemonadeAgent extends AbsAgent implements IAgent {
	public static final String NAME = "DANNY"; // TODO: name your agent
	
	private List<Integer> myActions;
	private List<Double> myRewards;
	private Map<Integer, Integer> opponents; // opponent ID --> idx in opponentActions
	private List<List<Integer>> opponentActions;
	
	private int auctionID;
	
	public MyLemonadeAgent(String host, int port, ISetup gameSetup, String name) {
		super(host, port, gameSetup, name);
		
		this.auctionID = 0;
		this.myActions = new ArrayList<>();
		this.myRewards = new ArrayList<>();
		this.opponents = new HashMap<>();
		this.opponentActions = new ArrayList<>();
	}
	
	private int nextMove() {
		// TODO:
		// implement your strategy for the lemonade game, returning an action
		// between 0 and 11, inclusive.
		List<Integer> myActions = this.getMyActions();
		List<Integer> opponent1Actions = this.getOpponentActions(0);
		List<Integer> opponent2Actions = this.getOpponentActions(1);
		List<Double> myRewards = this.getMyRewards();

		int play1 = 0;
		int play2 = 0;
		List<Integer> bestplays = new ArrayList<>();
		int bestPlay;

		// feel free to use these lists for move history.
		// if you want to look back multiple moves, please make sure you don't go out-of-bounds.
		switch (myActions.size()) {
			case 0:
				Random ran = new Random();
				return ThreadLocalRandom.current().nextInt(0, 12);
			case 1:
				play1 = ((opponent1Actions.get(0) + opponent2Actions.get(0)) / 2) % 12;
				play2 = (play1 + 6) % 12;
				bestPlay = (Math.abs(play1 - opponent1Actions.get(0)) > Math.abs(play2 - opponent1Actions.get(0)) ? play1 : play2);
				return ((Math.abs(myActions.get(0) - bestPlay) + myActions.get(0)) % 12);
			default:
				for(int x = myActions.size() - 1; x != 0 && x >= myActions.size() - 3; x--) {
					play1 = ((opponent1Actions.get(0) + opponent2Actions.get(0)) / 2) % 12;
					play2 = (play1 + 6) % 12;
					bestPlay = (Math.abs(play1 - opponent1Actions.get(0)) > Math.abs(play2 - opponent1Actions.get(0)) ? play1 : play2);
					bestplays.add(0, bestPlay);
				}
				int averageChange = 0;
				for(int y = 0; y < bestplays.size() - 1; y++) {
					averageChange += bestplays.get(y + 1) - bestplays.get(y);
				}
				averageChange = (int) Math.round((double) averageChange / (double) (bestplays.size() - 1));
				return bestplays.get(bestplays.size() - 1) + averageChange + ThreadLocalRandom.current().nextInt(0, 2) - 1;
		}
	}
	
	
	private void afterRound() {
		// TODO: perform your between-round updates if applicable.
		
		// feel free to use these lists for move history.
		// if you want to look back multiple moves, please make sure you don't go out-of-bounds.
		List<Integer> myActions = this.getMyActions();
		List<Integer> opponent1Actions = this.getOpponentActions(0);
		List<Integer> opponent2Actions = this.getOpponentActions(1);
		List<Double> myRewards = this.getMyRewards();
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
        IGameAction action = new GameAction(this.nextMove());
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
	    
	    this.updateHistory(actions, rewards, this.publicID);
	    this.afterRound();
	}
	
	public List<Integer> getMyActions() {
		return Collections.unmodifiableList(myActions);
	}
	
	public List<Double> getMyRewards() {
		return Collections.unmodifiableList(myRewards);
	}
	
	public List<Integer> getOpponentActions(int opponentNum) {
		if (opponentActions.isEmpty()) {
			return new ArrayList<>();
		}
		if (opponentNum >= opponentActions.size()) {
			opponentNum = opponentActions.size() - 1;
		}
		return Collections.unmodifiableList(opponentActions.get(opponentNum));
	}
	
	public void updateHistory(Map<Integer, Integer> actions, Map<Integer, Double> rewards, int id) {
		for (Integer agt : actions.keySet()) {
			if (agt.intValue() == id) {
				myActions.add(actions.get(agt));
			} else {
				if (!opponents.containsKey(agt)) {
					opponents.put(agt, opponents.size());
					opponentActions.add(new ArrayList<>());
				}
				opponentActions.get(opponents.get(agt)).add(actions.get(agt));
			}
		}
		
		myRewards.add(rewards.get(id));
	}
	
	public static void main(String[] args) throws InterruptedException {
		List<String> agents = Arrays.asList(MyLemonadeAgent.class.getCanonicalName(), MyLemonadeAgent.class.getCanonicalName(), MyLemonadeAgent.class.getCanonicalName());
		List<String> names = Arrays.asList(NAME + "_0", NAME + "_1", NAME + "_2");
        new BasicSimulation(agents, names, "input_configs/lemonade_game.json", 2121, "outfile", false).run();
	}
}
