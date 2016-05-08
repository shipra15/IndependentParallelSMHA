package algorithms;

import impl.HeuristicSolverUtility;
import model.State;

public class GenericLinearConflict {

//	public static int calculate(State s, State goal) {
//		int dimension = s.getDimension();
//		int[] source = new int[dimension*dimension];
//		int[] destination = new int[dimension*dimension];
//		
//		//populate destination from goal
//		int counter = 0;
//		int heuristic = 0;
//		for(int i=0;i<dimension;i++)
//		{
//			for(int j=0;j<dimension;j++)
//			{
//				destination[goal.getCellValue(i, j)] = counter;
//				counter++;
//			}
//		}
//		
//		//populate source from s
//		counter = 0;
//		for(int i=0;i<dimension;i++)
//		{
//			for(int j=0;j<dimension;j++)
//			{
//				source[counter] = s.getCellValue(i, j);
//				counter++;
//			}
//		}
//		
//		for(int i=0;i<dimension*dimension;i++)
//		{
//			for(int j=i+1;j<dimension*dimension;j++)
//			{
//				if(destination[source[i]] > destination[source[j]])
//					heuristic++;
//			}
//		}
//		
//		return heuristic;
//	}
	
	public static int calculate(State s, State goal) {
		int dimension = s.getDimension();
		int[] source ;
		int[] destination ;
		
		//populate destination from goal
		int counter = 0;
		int heuristic = 0;
		for(int k=0;k<dimension;k++)
		{
			counter = 0;
			source = new int[dimension];
			destination = new int[dimension*dimension];
			
			for(int a=0;a<dimension;a++) {
				source[a] = -1;
				
			}
			for(int b=0;b<dimension*dimension;b++) {
				destination[b] = -1;
			}
			
			for(int j=0;j<dimension;j++)
			{
				destination[goal.getCellValue(k, j)] = counter;
				source[counter] = s.getCellValue(k, j);
				counter++;
			}
		

		
			for(int i=0;i<dimension;i++)
			{
				for(int j=i+1;j<dimension;j++)
				{
					if(((destination[source[i]] != -1) && (destination[source[j]] != -1)) && (destination[source[i]] > destination[source[j]]))
						heuristic++;
				}
			}
		
		}
		
		for(int k=0;k<dimension;k++)
		{
			counter = 0;
			source = new int[dimension];
			destination = new int[dimension*dimension];
			
			for(int a=0;a<dimension;a++) {
				source[a] = -1;
				
			}
			for(int b=0;b<dimension*dimension;b++) {
				destination[b] = -1;
			}
			
			for(int j=0;j<dimension;j++)
			{
				destination[goal.getCellValue(j, k)] = counter;
				source[counter] = s.getCellValue(j, k);
				counter++;
			}
		

		
			for(int i=0;i<dimension;i++)
			{
				for(int j=i+1;j<dimension;j++)
				{
					if(((destination[source[i]] != -1) && (destination[source[j]] != -1)) && (destination[source[i]] > destination[source[j]]))
						heuristic++;
				}
			}
		
		}
		
		return heuristic*2;
	}
	
	public static void main(String args[]) {
		State randomstState = HeuristicSolverUtility.createRandom(3);
		HeuristicSolverUtility.printState(randomstState);
		
		State goalState = HeuristicSolverUtility.createRandom(3);
		HeuristicSolverUtility.printState(goalState);
		
		System.out.println(calculate(randomstState, goalState));
		
	}
}
