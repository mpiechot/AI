package ai.implementation.util;

import de.itdesign.codebattle.api.model.Position;

public class Node {
	private Node from;
	private Position actual;
	private int gCosts;
	private int hCosts;
	
	public Node(Node from, Position actual, int heuristik, int moveCost)
	{
		this.from = from;
		this.actual = actual;
		hCosts = heuristik;
		gCosts = moveCost;
	}
	/**
	 * calculate the F-Costs of this Node. F-Cost = gCosts + hCosts
	 * @return calculated F-Costs
	 */
	public int getFCost()
	{
		return gCosts + hCosts;
	}
	/** 
	 * @return gCosts of this Node
	 */
	public int getGCosts()
	{
		return gCosts;
	}
	/** 
	 * @return hCosts of this Node
	 */
	public int getHCosts()
	{
		return hCosts;
	}
	/** 
	 * @return predecessor of this Node or null if we reached the end of the Path
	 */
	public Node getFrom() {
		return from;
	}
	/**
	 * update the predecessor of this Node. Use this Method if you dont need the costs of the Nodes
	 * @param from new predecessor
	 */
	public void setFrom(Node from) {
		this.from = from;
	}
	/** 
	 * @return actual - the path-position
	 */
	public Position getActual() {
		return actual;
	}
	/**
	 * Update this Node if we found a better Path to actual. Use this Function if
	 * you need the costs of the Node
	 * @param from - new predecessor of this node
	 * @param newGCost - new calculated gCosts of this Node
	 */
	public void updateNode(Node from, int newGCost)
	{
		this.from = from;
		gCosts = newGCost;
	}
}
