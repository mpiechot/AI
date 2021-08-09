package ai.implementation.util;

import de.itdesign.codebattle.api.model.Position;

public class GridNode {
	private Position pos;
	private boolean walkable;
	private boolean opened;
	private boolean closed;
	private double fCosts;
	private double hCosts;
	private double gCosts;
	private double nCosts;
	private GridNode parent;
	
	public GridNode(Position pos, boolean walkable)
	{
		this.pos = pos;
		this.walkable = walkable;
	}

	public Position getPos() {
		return pos;
	}

	public boolean isWalkable() {
		return walkable;
	}

	public void setWalkable(boolean walkable) {
		this.walkable = walkable;
	}

	public GridNode getParent() {
		return parent;
	}

	public void setParent(GridNode parent) {
		this.parent = parent;
	}

	public double gethCosts() {
		return hCosts;
	}

	public void sethCosts(double hCosts) {
		this.hCosts = hCosts;
	}
	public double getnCosts() {
		return nCosts;
	}
	
	public void setnCosts(double nCosts) {
		this.nCosts = nCosts;
	}
	public double getfCosts() {
		return fCosts;
	}
	
	public void setfCosts(double fCosts) {
		this.fCosts = fCosts;
	}
	public double getgCosts() {
		return gCosts;
	}
	
	public void setgCosts(double gCosts) {
		this.gCosts = gCosts;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof GridNode)
		{
			GridNode node = (GridNode)obj;
			if(pos.getX() == node.getPos().getX() && pos.getY() == node.getPos().getY())
				return true;
		}
		return super.equals(obj);
	}
	
}
