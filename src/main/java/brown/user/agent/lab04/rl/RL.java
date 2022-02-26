package brown.user.agent.lab04.rl; 

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import brown.user.agent.library.offline.AbsOfflineLearningAgent;

public abstract class RL extends AbsOfflineLearningAgent {
	private static final Random random = new Random();
	
	// keep track of historical states
	private List<Integer> myStates;
	
	private boolean trainingMode;
	
	// hyperparameters
	private final int numPossibleActions;
	private final int numPossibleStates;
	protected double learningRate; // a.k.a. alpha. set this to 0 for a non-learning agent (e.g. to train against).
	private double discountFactor; // a.k.a gamma
	protected double explorationRate; // a.k.a. epsilon. set this to 0 for a competition agent (so it always follows the best policy).
	
	// persistent variables from Q-learning algorithm
	private int s; // your current state
	private int a; // the next action you will take
	
	// your Q-table
	private double[][] Q;
	
	// your policy (a.k.a. pi)
	private int[] policy;
	
	public RL(int numPossibleStates, int numPossibleActions, int initialState, double learningRate,
			double discountFactor, double explorationRate, boolean trainingMode) {
		super();
		this.numPossibleActions = numPossibleActions;
		this.numPossibleStates = numPossibleStates;
		this.learningRate = learningRate;
		this.discountFactor = discountFactor;
		this.explorationRate = explorationRate;
		this.myStates = new ArrayList<>();
		this.trainingMode = trainingMode;
		
		// initialize Q to 12 for all state-action pairs (avg score).
		this.Q = new double[this.numPossibleStates][this.numPossibleActions];
		for (double[] row : this.Q) {
			for (int i = 0; i < row.length; i++) {
				row[i] = 12.0;
			}
		}
		
		// initialize policy to random actions
		this.policy = new int[this.numPossibleStates];
		for (int i = 0; i < this.policy.length; i++) {
			this.policy[i] = random.nextInt(this.numPossibleActions);
		}
		
		// begin with initial state and action.
		this.s = initialState;
		this.a = this.policy[this.s];
	}
	
	public void setTrainingMode(boolean trainingMode) {
		this.trainingMode = trainingMode;
	}
	
	@Override
	public int nextMove() {
		// TODO:
		// change this if you want
		// this is called each round to get your agent's action

		return this.a;
	}
	
	@Override
	public void afterRound() {
		// TODO:
		// fill this in
		
		// this is called after each round. do your learning/updating here.
		
		int nextState = this.determineState();
		
		// ...
		
		this.s = nextState;
	}
	
	/**
	 * Max of a table row.
	 */
	public static double max(double[] row) {
		if (row.length == 0) {
			return Double.NaN;
		}
		
		double max = Double.NEGATIVE_INFINITY;
		for (double n : row) {
			max = Math.max(n, max);
		}
		return max;
	}
	
	/**
	 * Argmax with randomized tiebreak.
	 */
	public static int argmax(double[] row) {
		List<Integer> res = new ArrayList<>(row.length);
		double max = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < row.length; i++) {
			if (Math.abs(row[i] - max) < 0.0001) {
				res.add(i);
			} else if (row[i] > max) {
				res.clear();
				res.add(i);
				max = row[i];
			}
		}
		
		if (res.isEmpty()) {
			return -1;
		}
		
		return res.get(random.nextInt(res.size()));
	}
	
	
	public int getNumPossibleStates() {
		return numPossibleStates;
	}
	
	public int getNumPossibleActions() {
		return numPossibleActions;
	}
	
	public List<Integer> getMyStates() {
		return Collections.unmodifiableList(myStates);
	}
	
	public abstract int determineState();
}
