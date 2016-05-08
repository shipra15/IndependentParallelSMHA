package model;

import constants.Constants;

public class Node {
	
	private State state;
	//private Move move;
	private int cost = Integer.MAX_VALUE - 100000;
	private Node parent = null;
	private Action nextAction;
	private Double heuristicCost;
	private Double weight;
	private Boolean expandedByAnchor = false;
	private Boolean expandedByInadmissible = false;
	public Integer[] insertedIntoQueues = new Integer[Constants.InadmissibleHeuristicsCount+1];
	
	public Node() {}
	
	/**
	 * @param state
	 * @param weight - Weight for calculating Key Value
	 */
	public Node(State state , Double weight) {
		this.state = state;
		this.weight = weight;
		for(int i=0; i<= Constants.InadmissibleHeuristicsCount; i++)
		{
			insertedIntoQueues[i] = 0;
		}
	}
	
	public State getState() {
		return state;
	}
	
	/**
	 * equality based on state
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node){
			return state.equals(((Node)obj).state);
		}
		return false;
	}
	
	/**
	 * equality based on state
	 */
	@Override
	public int hashCode() {
		return state.hashCode();
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
		cost = parent.getCost() + 1;
	}
	
	public void setCost(int cost)
	{
		this.cost = cost;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public int getCost() {
		return cost;
	}

	public Action getAction() {
		return nextAction;
	}
	
	public void setAction(Action next){
		this.nextAction = next;
	}

	public Double getKey() {
		return (getCost() + this.weight * heuristicCost);
	}

	public void setHeuristicCost(Double heuristicCost) {
		this.heuristicCost = heuristicCost;
	}
	
	public Boolean getExpandedByAnchor() {
		return expandedByAnchor;
	}

	public void setExpandedByAnchor(Boolean expandedByAnchor) {
		this.expandedByAnchor = expandedByAnchor;
	}

	public Boolean getExpandedByInadmissible() {
		return expandedByInadmissible;
	}

	public void setExpandedByInadmissible(Boolean expandedByInadmissible) {
		this.expandedByInadmissible = expandedByInadmissible;
	}

}