package impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import constants.Constants;
import model.Node;
import model.State;

public class Start {
	
	static int count = 0;
	

	public void solveHeuristic() {
		
	}

	public static void main(String[] args) throws Exception 
	{
//		System.setOut(new PrintStream("C:\\Users\\Shipra\\Desktop\\output.txt"));
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Shipra\\Desktop\\parallelSMHATemp.txt", true)));
		PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Shipra\\Desktop\\parallelSMHATemp.txt", true)));
//		out.println("\n");
		final State startState = HeuristicSolverUtility.createRandom(Constants.dimension);
//		out.println("Start state is: "+startState.hashCode());
		List<State> randomGoalStates = createNewGoalStates();
		new SMHA().SMHAstar(startState, out, randomGoalStates);
		new CorrectedParallelSMHA().CorrectedParallelSMHA(startState, randomGoalStates, out1);
//		new MergeAfterDelay(startState, out);
	}
	
	private static List<State> createNewGoalStates() {// possibly remove goalNode from termination condition.
		List<State>randomGoalStates = new ArrayList<State>();
		for(int i=1;i<=Constants.InadmissibleHeuristicsCount;i++) {
			State state = HeuristicSolverUtility.createRandom(Constants.dimension, Constants.randomisationFactor);
			
			randomGoalStates.add(state);
		}
		return randomGoalStates;
	}
	
}
