package impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

import model.Action;
import model.Node;
import model.State;
import model.StateConstants;
import queues.AnchorQueue;
import queues.InadmissibleHeuristicQueue;
import algorithms.LinearConflict;
import algorithms.ManhattanDistance;
import constants.Constants;

public class SMHA {
	

//	private HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
	private Node nGoal = null;
	List<Node> randomGoalNodes = null;
	private int pathLength = 0;
	private Long startTime = null;
	private Long endTime = null;
	private Boolean timedOut = false;
	PrintWriter printWriter = null;
	private Integer expandCOunt = 0;
	public void SMHAstar(State randomState, PrintWriter out, List<State> goalStates) throws InterruptedException 
	{
		printWriter = out;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				printWriter.println("serial smha timed out");
				printWriter.flush();
				timedOut = true;
				return;
			}
		}, 60000);
		startTime = System.currentTimeMillis();
		Node nStart = createNode(randomState, Constants.w1);
		nStart.setCost(0);
		
		State goalState = HeuristicSolverUtility.generateGoalState(Constants.dimension);
//		printWriter.println("goal state is: "+goalState.hashCode());
		nGoal = createNode(goalState, Constants.w1);
//		printNeighbours(nGoal);
		PriorityQueue<Node> anchorQueue = AnchorQueue.createQueue();
		addToAnchor(nStart, anchorQueue);
		
		List<PriorityQueue<Node>> pqList = new ArrayList<PriorityQueue<Node>>();
		createNewGoalStates(goalStates);
		for(int i=1; i<=Constants.InadmissibleHeuristicsCount; i++)
		{
			PriorityQueue<Node> prq = InadmissibleHeuristicQueue.createQueue(i, randomGoalNodes.get(i-1).getState());
//			prq.add(nStart);
			pqList.add(prq);
		}
		
		int i=0;
		for(PriorityQueue<Node> q: pqList)
		{
			i++;
			addToRandom(nStart, i, q);
		}
		
//		visited.put(nStart.hashCode(), true);
		
		while(anchorQueue.isEmpty() == false) {
			
			i = 0;
			for(PriorityQueue<Node> p: pqList)
			{
				i++;
				PriorityQueue<Node> selected = null;
				Node node = null;
				Node anchorNode = anchorQueue.peek();
				while(anchorNode != null && (anchorNode.getExpandedByAnchor() || anchorNode.getExpandedByInadmissible()))
				{
					anchorQueue.remove();
					anchorNode = anchorQueue.peek();
				}
				Node inadmissibleNode = p.peek();
				while(inadmissibleNode != null && (inadmissibleNode.getExpandedByAnchor() || inadmissibleNode.getExpandedByInadmissible()))
				{
					p.remove();
					inadmissibleNode = p.peek();
				}
				Boolean result  = expandAnchor(anchorNode, inadmissibleNode, i);
				if(result)
				{
					selected = anchorQueue;
//					HeuristicSolverUtility.printState(selected.peek().getState());
					
					if(testTerminationCondition(anchorKey(selected.peek())))
					{
						System.out.println("expansion count is: "+expandCOunt);
						return;
					}
					if(timedOut)
						return;
					
					node = selected.remove();
//					printWriter.println("\n expanded state is: "+node.hashCode()+"cost is: "+anchorKey(node));
					node.setExpandedByAnchor(true);
				}
				else
				{
					selected = p;
//					HeuristicSolverUtility.printState(selected.peek().getState());
				
					if(testTerminationCondition(inadmissibleNodeKey(selected.peek(), i)))
						return;
					if(timedOut)
						return;
					
					node = selected.remove();
//					printWriter.println("expanded state is: "+node.hashCode()+"cost is: "+inadmissibleNodeKey(node, i));
					node.setExpandedByInadmissible(true);
				}
				
				expandNode(anchorQueue, pqList, node);
			}
	
		}
		
		
	}
	
	private Boolean testTerminationCondition(Double key) {
		
		Boolean result = false;
		if(nGoal.getCost() <= key)
			result = true;
		for(Node goalNode: randomGoalNodes) {
			if(goalNode.getCost() + Constants.randomisationFactor <= key)
				result = true;
		}
		
		if(result)
		{
			System.out.println("path found");
//			executorService.shutdownNow();
			endTime = System.currentTimeMillis();
			printWriter.println("time taken is for serial SMHA* : "+(endTime-startTime)+"  expansion count is: "+expandCOunt);
//			printWriter.print("  expansion count is: "+expandCOunt);
			printWriter.flush();

			return true;
		}
		return false;
	}
	
	private void createNewGoalStates(List<State> goalStates) {// possibly remove goalNode from termination condition.
		randomGoalNodes = new ArrayList<Node>();
		for(State state: goalStates) {
			Node randomGoalNode = createNode(state, Constants.w1);
			randomGoalNodes.add(randomGoalNode);
		}
	}
	
	private Node createNode(State state, Double weight)
	{
		if(StateConstants.nodeMap.get(state.hashCode()) != null)
		{
			return StateConstants.nodeMap.get(state.hashCode());
		}
		else
		{
			Node node = new Node(state, weight);
			StateConstants.nodeMap.put(state.hashCode(), node);
			return StateConstants.nodeMap.get(state.hashCode());
		}
		
	}
	
	private void expandNode(PriorityQueue<Node> anchorPQ, List<PriorityQueue<Node>> listPQ, Node toBeExpanded) throws InterruptedException
	{
		expandCOunt++;
		Long t1 = 0l, t2 = 0l;
		t1 = System.nanoTime();
		State state = toBeExpanded.getState();
		List<Action> listOfPossibleActions = state.getPossibleActions();
		Iterator<Action> actIter = listOfPossibleActions.iterator();
//		printWriter.println("3 " +(System.nanoTime() - t1));
//		printWriter.println( "Neighbours are: ");
		while(actIter.hasNext()) {
//			t1 = System.nanoTime();

			Action actionOnState = actIter.next();
			State newState = actionOnState.applyTo(state);
//			printWriter.print(newState.hashCode()+" ");
			Node newNode = createNode(newState, Constants.w1);
//			visited.put(newNode.hashCode(), true);

			if(newNode.getCost() > toBeExpanded.getCost()+1)
			{
				newNode.setParent(toBeExpanded);
				if(!newNode.getExpandedByAnchor())
				{
					addToAnchor(newNode, anchorPQ);
					if(!newNode.getExpandedByInadmissible())
					{
						addOrUpdateNodeToInadmissibleQueues(listPQ, newNode);
					}
				}
			}

		}
	}
	
	private void addToAnchor(Node node, PriorityQueue<Node> anchorQueue)
	{
		Long t1= System.nanoTime();
		if(node.insertedIntoQueues[0] == 0)
		{
			node.setExpandedByAnchor(false);
			anchorQueue.offer(node);
			node.insertedIntoQueues[0] = 1;
		}
//		printWriter.println("Serial addToAnchor: "+(System.nanoTime() - t1));
	}

	private void addToRandom(Node node, int heuristic, PriorityQueue<Node> queue)
	{
		Long t1 = System.nanoTime();
		if(node.insertedIntoQueues[heuristic] == 0)
		{
			node.setExpandedByAnchor(false);
			node.setExpandedByInadmissible(false);
			queue.offer(node);
			node.insertedIntoQueues[heuristic] = 1;
		}
//		printWriter.println("Serial addToRandom: "+(System.nanoTime() - t1));
	}
	

	
	private void addOrUpdateNodeToInadmissibleQueues(List<PriorityQueue<Node>> listPQ, Node toBeAdded)
	{
		int heuristic = 0;
		for(PriorityQueue<Node> pq: listPQ)
		{
			heuristic++;
			if(inadmissibleNodeKey(toBeAdded, heuristic) <= Constants.w2*anchorKey(toBeAdded))
			{
				addToRandom(toBeAdded, heuristic, pq);
			}
		}
	}
	
	
	private Boolean expandAnchor(Node anchor, Node inadmissible, int heuristic)
	{
		if(inadmissible == null)
			return true;
		if(anchor == null)
			return false; 
		Boolean result = false;
		
		Double minKeyAnchor = anchorKey(anchor);
		Double minKeyInadmissible = inadmissibleNodeKey(inadmissible, heuristic);
		if(minKeyInadmissible <= Constants.w2*minKeyAnchor)
		{
			result = false;
		}
		else
		{
			result = true;
//			printWriter.println("anchor expanded");
		}
		return result;
	}
	
	private Double anchorKey(Node anchor)
	{
		return (anchor.getCost() + Constants.w1* LinearConflict.calculate(anchor.getState()));
	}
	
	private Double inadmissibleNodeKey(Node inadmissible, int heuristic)
	{
		return inadmissible.getCost() +Constants.w1*RandomHeuristicGenerator.generateRandomHeuristic
				(heuristic, inadmissible.getState(), randomGoalNodes.get(heuristic-1).getState());
	}
	
//	public void printNeighbours(Node atomicNode) {
//		printWriter.println("neighbours are :");
//		State state = atomicNode.getState();
//		List<Action> listOfPossibleActions = state.getPossibleActions();
//		Iterator<Action> actIter = listOfPossibleActions.iterator();
//		while(actIter.hasNext()) {
//			Action actionOnState = actIter.next();
//			State newState = actionOnState.applyTo(state);
//			printWriter.println(newState.hashCode());
//		}
//	}

	public static void main(String args[]) throws IOException, InterruptedException
	{
		PrintWriter out =  new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Shipra\\Desktop\\outputNew.txt", false)));
		out.println("start");
//		new SMHA().SMHAstar(HeuristicSolverUtility.createRandom(Constants.dimension), out);
	}
	
}
