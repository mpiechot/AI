package ai.implementation.util;

import java.util.LinkedList;
import java.util.List;

import de.itdesign.codebattle.api.model.Direction;
import de.itdesign.codebattle.api.model.Position;
import de.itdesign.codebattle.api.model.Unit;

public class MyUnit {
	private Unit unit;
	private Direction direction;
	private List<Direction> disabledDirs;
	private Position target;
	private Position nextStep;
	private boolean attackTarget;
	private int priority;
	
	public MyUnit(Unit unit) {
		this.unit = unit;
		disabledDirs = new LinkedList<>();
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Position getTarget() {
		return target;
	}

	public void setTarget(Position target) {
		this.target = target;
	}

	public boolean isAttackTarget() {
		return attackTarget;
	}

	public void setAttackTarget(boolean attackTarget) {
		this.attackTarget = attackTarget;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Position getNextStep() {
		return nextStep;
	}

	public void setNextStep(Position nextStep) {
		this.nextStep = nextStep;
	}
	public void clearDisableDirs(){
		disabledDirs.clear();
	}
	public List<Direction> getDisableDirs(){
		return disabledDirs;
	}
	
}
