package ai.implementation;

import java.util.LinkedList;
import java.util.List;

import de.itdesign.codebattle.api.model.ClientRoundState;
import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;

public interface PathFinder {
	public List<Position> blockedPositions = new LinkedList<>();

	
	public Direction giveNextDirection(ClientRoundState roundState, Unit unit, Position target);
}
