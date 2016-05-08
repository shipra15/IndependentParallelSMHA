package queues;

import impl.RandomHeuristicGenerator;

import java.util.Comparator;

import constants.Constants;
import model.Node;
import model.State;

public class InadmissibleHeuristicQueue {

	public static class HeuristicComparator implements Comparator<Node>{	
		
		int heuristic = 0;
		State goalState = null;

		@Override
		public int compare(Node o1, Node o2) {

				int result = ((Double)(o1.getCost() + Constants.w1* RandomHeuristicGenerator.generateRandomHeuristic(heuristic, o1.getState(),
						goalState) - 
						(o2.getCost() + Constants.w1* RandomHeuristicGenerator.generateRandomHeuristic(heuristic, o2.getState(),
								goalState)))).intValue();	
	//			int result = (ManhattanDistance.calculate(o1.getState()) - ( ManhattanDistance.calculate(o2.getState())));	
				
				if (result == 0){
					//Ties among minimal f values are resolved in favor of the deepest node in the search tree
					//i.e. the closest node to the goal
					result =  o2.getCost() - o1.getCost();			
					
				}
				
				return result;
			
		}
		
		public void setHeuristic(int h)
		{
			heuristic = h;
		}

		public State getGoalState() {
			return goalState;
		}

		public void setGoalState(State goalState) {
			this.goalState = goalState;
		}
	}
	
	public static java.util.PriorityQueue<Node> createQueue(int h, State goalState) {
		HeuristicComparator hc = new HeuristicComparator();
		hc.setHeuristic(h);
		hc.setGoalState(goalState);
		return new java.util.PriorityQueue<Node>(10000, hc);
	}

}
