

package impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import algorithms.GenericLinearConflict;
import algorithms.GenericManhattanDistance;
import algorithms.LinearConflict;
import algorithms.ManhattanDistance;
import constants.Constants;
import model.Action;
import model.Node;
import model.State;
import queues.AnchorQueue;
import queues.InadmissibleHeuristicQueue;

public class CorrectedParallelSMHA {
	
	HashMap<Integer,PriorityQueue<Node>> queueList = new HashMap<Integer,PriorityQueue<Node>>();
	HashMap<Integer,HashMap<Integer, Node>> nodeMaps = new HashMap<Integer,HashMap<Integer, Node>>();
	HashMap<Integer, Node> goalMap = new HashMap<Integer, Node>();
	HashMap<Integer, HashMap<Integer, Node>> sendToAnchorMap = new HashMap<Integer, HashMap<Integer, Node>>();
	HashMap<Integer, HashMap<Integer, Node>> sendFromAnchorMap = new HashMap<Integer, HashMap<Integer, Node>>();
	ExecutorService executorService = null;
	AtomicInteger anchorExpansionCount = new AtomicInteger(); //only for logging
	AtomicInteger inadmissibleExpansionCount = new AtomicInteger(); 
	Long startTime = null;
	PrintWriter printWriter = null;
	private Boolean timedOut = false;
	
	public void CorrectedParallelSMHA(State startState, List<State> goalStates, PrintWriter out) {
		printWriter = out;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				printWriter.println("parallel smha timed out"+" anchor expansion count is: "+ anchorExpansionCount
						+" inadmissible expansion count is: "+inadmissibleExpansionCount);
				printWriter.flush();
				timedOut = true;
				return;
			}
		}, 60000);
		init(startState, goalStates);
	}
	
	private void init(State startState, List<State> goalStates) {
		startTime = System.currentTimeMillis();
		anchorExpansionCount.set(0);
		inadmissibleExpansionCount.set(0);
		for(int heuristic=0;heuristic<=Constants.InadmissibleHeuristicsCount;heuristic++) {
		
			HashMap<Integer, Node> map = new  HashMap<Integer, Node>();
			HashMap<Integer, Node> toAnchorMap = new  HashMap<Integer, Node>();
			nodeMaps.put(heuristic, map);
			sendToAnchorMap.put(heuristic, toAnchorMap);
			
			HashMap<Integer, Node> fromAnchorMap = new  HashMap<Integer, Node>();
			sendFromAnchorMap.put(heuristic, fromAnchorMap);
		}
		
		executorService = Executors.newFixedThreadPool(Constants.InadmissibleHeuristicsCount);
		
		createNewGoalStates(goalStates);
		for(int heuristic=0;heuristic<=Constants.InadmissibleHeuristicsCount;heuristic++) {
			
			PriorityQueue<Node> queue = null;
			if(heuristic == 0) {
				queue = AnchorQueue.createQueue();
			}
			else {
				queue = InadmissibleHeuristicQueue.createQueue(heuristic, goalMap.get(heuristic).getState());
			}
			queueList.put(heuristic, queue);

			Node startNode = createNode(startState, heuristic);
			startNode.setCost(0);
			queue.add(startNode);
			if(heuristic == 0)
				System.out.println(anchorKey(startNode));
			else
				System.out.println(inadmissibleNodeKey(startNode, heuristic));

			if(heuristic>0) {
				Node anchorNode = null;
				while(anchorNode == null) {
					anchorNode = queueList.get(0).peek();
				}
				startQueueProcessing(heuristic, anchorKey(anchorNode));
			}
		}
	}
	
	private void startQueueProcessing(final int heuristic,final Double minKeyAnchor) {

		executorService.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				try {
//					sendToAnchorMap.get(heuristic).clear();
					smha(heuristic, minKeyAnchor);
				} catch (Exception e) {
					e.printStackTrace();
					executorService.shutdown();
				}
				return null;
			}
		});
	
	}
	
	private Node createNode(State state, Integer heuristic)
	{
		Node node = nodeMaps.get(heuristic).get(state.hashCode());
		if(node != null)
		{
			return node;
		}
		else
		{
			node = new Node(state, Constants.w1);
			nodeMaps.get(heuristic).put(state.hashCode(), node);
			if(heuristic > 0)
				sendToAnchorMap.get(heuristic).put(state.hashCode(), node);
			else {
				for(int i=1;i<=Constants.InadmissibleHeuristicsCount;i++)
				sendFromAnchorMap.get(i).put(state.hashCode(), node);
			}
			return node;
		}
		
	}
	
	private void createNewGoalStates(List<State> goalStates) {// goalStates do not include anchor goal state
		
		State anchorGoalState = HeuristicSolverUtility.generateGoalState(Constants.dimension);
		goalStates.add(0, anchorGoalState);
		int heuristic = 0;
		for(State state: goalStates) {
			Node randomGoalNode = createNode(state, heuristic);
			goalMap.put(heuristic, randomGoalNode);
			heuristic++;
		}
	}
	
	private void smha(Integer heuristic, Double minKeyAnchor) {
		PriorityQueue<Node> queue = queueList.get(heuristic);
		while(!queue.isEmpty()) {
			Node minKeyNode = queue.peek();
//			Double minKeyInadmissible = inadmissibleNodeKey(minKeyNode, heuristic);
//			if(minKeyInadmissible <=  Constants.w2*minKeyAnchor)
			if(minKeyNode.getCost()+LinearConflict.calculate(minKeyNode.getState()) <= Constants.w2*minKeyAnchor)
			{
				queue.remove();
				expandNode(minKeyNode, heuristic);
			}
			else 
			{
				// get updated states from anchor
				getStatesFromAnchor(heuristic);
				sendFromAnchorMap.get(heuristic).clear();
					minKeyAnchor = startAnchorUpdate(heuristic);
					sendToAnchorMap.get(heuristic).clear();
			}
		}
		System.out.println("queue empty");
	}
	
	private void getStatesFromAnchor(int heuristic) {
		HashMap<Integer, Node> map = sendFromAnchorMap.get(heuristic);
		
		Double minAnchorKey = null;
		if(!map.isEmpty()) {
			//merge states to anchor and send updated bound (in new thread if h!=heuristic) 
			for(Map.Entry<Integer, Node> entry: map.entrySet()) {
				if(entry.getValue().getParent() == null)
					continue;
				Node inadNode = nodeMaps.get(heuristic).get(entry.getValue().hashCode());
				if(inadNode == null || (inadNode.getCost() > entry.getValue().getCost())) {
					inadNode = createNode(entry.getValue().getState(), heuristic);
					inadNode.setParent(createNode(entry.getValue().getParent().getState(),heuristic));
					addOrUpdateNodeToInadmissibleQueues(inadNode, heuristic);
				}
			}

		}
	}
	
	synchronized private Double startAnchorUpdate(int heuristic) {
			HashMap<Integer, Node> map = sendToAnchorMap.get(heuristic);
			Double minAnchorKey = null;
			if(!map.isEmpty()) {
				//merge states to anchor and send updated bound (in new thread if h!=heuristic) 
				for(Map.Entry<Integer, Node> entry: map.entrySet()) {
					if(entry.getValue().getParent() == null)
						continue;
					Node anchor = nodeMaps.get(0).get(entry.getValue().hashCode());
					if(anchor == null || (anchor.getCost() > entry.getValue().getCost())) {
						anchor = createNode(entry.getValue().getState(), 0);
						anchor.setParent(createNode(entry.getValue().getParent().getState(),0));
						addToAnchor(anchor);
					}
				}

			}
			else if(!queueList.get(0).isEmpty()) {
				//expand anchor and send updated bound in this thread
				Node minAnchor = queueList.get(0).remove();
				expandNode(minAnchor, 0);
			}
			else
				System.out.println("anchor queue empty");
			minAnchorKey = anchorKey(queueList.get(0).peek());

			return minAnchorKey;
	}
	
private void testTerminationCondition(Double key) {
		Boolean result = false;
		for(int i=0;i<=Constants.InadmissibleHeuristicsCount;i++) {
			
			if(i == 0 && goalMap.get(0).getCost() <= key)
				result = true;
			else if(goalMap.get(i).getCost() + Constants.randomisationFactor <= key) 
				result = true;
		}
		

		
		if(result)
		{
//			printWriter.println("path found");
			printWriter.println("time taken is: "+(System.currentTimeMillis() - startTime)+" anchor expansion count is: "+ anchorExpansionCount
					+" inadmissible expansion count is: "+inadmissibleExpansionCount);
//			printWriter.print("expansion count is: "+ expansionCount);
//			printWriter.println("path found");
//			endTime = System.currentTimeMillis();
//			printWriter.println("time taken is for serial SMHA* : "+(endTime-startTime));
//			printWriter.println("expansion count is: "+expandCOunt);
			printWriter.flush();
			executorService.shutdownNow();
			System.exit(0);
		}
		if(timedOut) {
			executorService.shutdownNow();
			System.exit(0);
		}
	}
	
	private void expandNode(Node toBeExpanded, int heuristic) {
//		System.out.println(heuristic);
		testTerminationCondition(heuristic == 0?anchorKey(toBeExpanded):inadmissibleNodeKey(toBeExpanded, heuristic));
		if(heuristic == 0)
			anchorExpansionCount.incrementAndGet();
		else
			inadmissibleExpansionCount.incrementAndGet();
		
		if(heuristic == 0)
			toBeExpanded.setExpandedByAnchor(true);
		else
			toBeExpanded.setExpandedByInadmissible(true);
		State state = toBeExpanded.getState();
		List<Action> listOfPossibleActions = state.getPossibleActions();
		Iterator<Action> actIter = listOfPossibleActions.iterator();
		while(actIter.hasNext()) {

			Action actionOnState = actIter.next();
			State newState = actionOnState.applyTo(state);
			Node newNode = createNode(newState, heuristic);

			if(newNode.getCost() > toBeExpanded.getCost()+1)
			{
				newNode.setParent(toBeExpanded);
					if(heuristic>0 && !newNode.getExpandedByInadmissible())
					{
						addOrUpdateNodeToInadmissibleQueues(newNode, heuristic);
					}
					else if(heuristic == 0 && !newNode.getExpandedByAnchor())
					{
						addToAnchor(newNode);
					}
			}

		}
	}
	
	private void addToAnchor(Node node)
	{
		if(node.insertedIntoQueues[0] == 0)
		{
			node.setExpandedByAnchor(false);
			queueList.get(0).offer(node);
			node.insertedIntoQueues[0] = 1;
		}
	}
	
	private void addOrUpdateNodeToInadmissibleQueues(Node toBeAdded, int heuristic)
	{
		if(toBeAdded.insertedIntoQueues[heuristic] == 0)
		{
			toBeAdded.setExpandedByInadmissible(false);
			queueList.get(heuristic).offer(toBeAdded);
			toBeAdded.insertedIntoQueues[heuristic] = 1;
		}
	}
	
	private Double anchorKey(Node anchor)
	{
		return (anchor.getCost() + Constants.w1* LinearConflict.calculate(anchor.getState()));
	}
	
	private Double inadmissibleNodeKey(Node inadmissible, int heuristic)
	{
//		System.out.println("inad cost: "+inadmissible.getCost());
//		System.out.println("h value: "+Constants.w1*RandomHeuristicGenerator.generateRandomHeuristic
//				(heuristic, inadmissible.getState(), goalMap.get(heuristic).getState()));
		return inadmissible.getCost() +Constants.w1*RandomHeuristicGenerator.generateRandomHeuristic
				(heuristic, inadmissible.getState(), goalMap.get(heuristic).getState());
	}
	
	public static void main(String args[]) throws IOException {
		final State startState = HeuristicSolverUtility.createRandom(Constants.dimension);
		HeuristicSolverUtility.printState(startState);
		List<State> randomGoalStates = createNewGoalStates(startState);
		PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Shipra\\Desktop\\parallelSMHATemp.txt", true)));
		new CorrectedParallelSMHA().CorrectedParallelSMHA(startState, randomGoalStates, out1);
	}
	
	private static List<State> createNewGoalStates(State startState) {// possibly remove goalNode from termination condition.
		List<State>randomGoalStates = new ArrayList<State>();
		for(int i=1;i<=Constants.InadmissibleHeuristicsCount;i++) {
			State state = HeuristicSolverUtility.createRandom(Constants.dimension, Constants.randomisationFactor);
			System.out.println("LC: "+GenericLinearConflict.calculate(startState, state));
			System.out.println("MD: "+GenericManhattanDistance.calculate(startState, state));
			randomGoalStates.add(state);
			HeuristicSolverUtility.printState(state);
		}
		return randomGoalStates;
	}

}

