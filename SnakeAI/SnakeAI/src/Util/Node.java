package Util;

import Logic.Point;
/**
 * Node class for shortest Path and longest Path calculations. Includes:
 * - Node reference of the predecessor
 * - actual Point (Position)
 * - pathcost to this position
 * - estimated pathcost to the target
 */
public class Node{
	private Node from;
	private Point actual;
	private int gCosts;
	private int hCosts;
	
	/**
	 * creates a Node with given pathcosts
	 * @param from predecessor node
	 * @param actual actual position
	 * @param heuristik estimated pathcost to the target
	 * @param moveCost costs to make the move from the predecessor to this node
	 */
	public Node (Node from, Point actual, int heuristik, int moveCost)
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
	public Point getActual() {
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
	/**
	 * calculate the Path from the actual Node to the End(StartPoint).
	 * This Function is needed for Debugging or JUnitTests
	 * @return String Repräsentation of the Path
	 */
	public String getPath()
	{
		Node t = this;
		String ret = "";
		while(t != null)
		{
			ret+= t.getActual()+" <- ";
			t = t.getFrom();
		}
		return ret.substring(0, ret.length()-4);
	}
	/**
	 * calculates the Path-Length to the Point p. if the path doent contains p
	 * then the function returns 999
	 * @param p the point to calculate the distance
	 * @return the pathdistance from this Node to p
	 */
	public int lengthToDest(Point p)
	{
		Node temp = this;
		int dist = 0;
		while(temp != null)
		{
			dist++;
			if(temp.getActual().equals(p))
				break;
			temp = temp.getFrom();
		}
		if(temp == null)
			return 999;
		return dist;
		
	}
	/**
	 * ToString Method to print the Path
	 */
	@Override
	public String toString()
	{
		String ret = (from != null? "("+from.actual.x +":"+from.actual.y+")":"");
		return "["+ret+" -> ("+actual.x+":"+actual.y+"): G: " +gCosts+" F: " + getFCost()+"]";
	}
}