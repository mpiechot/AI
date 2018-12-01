package Util;

import Logic.Field;
import Logic.Point;
import Logic.Portals;
import Logic.Snake.Direction;

/**
 * Thread to calculate the shortest path to a target.
 * If there is a path it saves the next Direction in the field result
 * @author Marco
 */
public class MinPathFinderThread extends Thread{
	private PathFinder minPathFinder;
	private Direction result;
	private Field field;
	private Point target;
	private TempSnake mySnake;
	private Portals portals;
	
	public MinPathFinderThread(PathFinder minPathFinder, Direction result, Field field, Point target, TempSnake mySnake,
			Portals portals) {
		this.minPathFinder = minPathFinder;
		this.result = result;
		this.field = field;
		this.target = target;
		this.mySnake = mySnake;
		this.portals = portals;
	}

	public void run()
	{
		Node bestWay = minPathFinder.getMinPath(mySnake, target, field, portals);
		//Gibt es keinen Pfad dorthin?
		if(bestWay != null)
		{	
			//Wir haben einen Pfad
			while(bestWay.getFrom() != null && !bestWay.getFrom().getActual().equals(mySnake.headPosition()))
				bestWay = bestWay.getFrom();	
			result = UtilFunctions.getDirection(bestWay.getFrom().getActual(),bestWay.getActual());
			if(result == null)
			{
				int x = bestWay.getFrom().getActual().x - bestWay.getActual().x; 
				result = (x > 0?Direction.LEFT:(x == 0?null:Direction.RIGHT));
				int y = bestWay.getFrom().getActual().y - bestWay.getActual().y;
				if(result == null)
					result = (y > 0?Direction.UP:Direction.DOWN);
			}
		}
	}
	public Direction getResult() {
		return result;
	}	
}
