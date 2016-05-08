package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import constants.Constants;
import model.State;
import algorithms.GenericLinearConflict;
import algorithms.GenericManhattanDistance;

public class RandomHeuristicGenerator {

	// Contains random numbers for each heuristic to multiply LC and MD
	private static HashMap<Integer, List<Double>> randomNumberMap = new HashMap<Integer, List<Double>>();

	
	public static Double generateRandomHeuristic(Integer heuristicID,
			State state, State goalState) {
		List<Double> randNums = getRandomNumbersForHeuristic(heuristicID);

		if (heuristicID == 0)
			return (double) (GenericManhattanDistance.calculate(state, goalState)+  GenericLinearConflict.calculate(state, goalState));
//		System.out.println("state is: ");
//		HeuristicSolverUtility.printState(state);
//		System.out.println("goalstate is: ");
//		HeuristicSolverUtility.printState(goalState);
//		System.out.println("MD: "+GenericManhattanDistance.calculate(state, goalState));
//		System.out.println("LC: "+GenericLinearConflict.calculate(state, goalState));
		return  (double)(GenericManhattanDistance.calculate(state, goalState)
				+  GenericLinearConflict.calculate(state, goalState) + Constants.randomisationFactor/Constants.w1);
	}

	
	private static List<Double> getRandomNumbersForHeuristic(Integer id) {
		List<Double> listOfRandomNumbers = new ArrayList<Double>();
		if(randomNumberMap.containsKey(id)) {
			listOfRandomNumbers = randomNumberMap.get(id);
		} else {
			listOfRandomNumbers.add(Math.random());
			listOfRandomNumbers.add(Math.random());
			randomNumberMap.put(id, listOfRandomNumbers);
		}
		return listOfRandomNumbers;
	}
}
