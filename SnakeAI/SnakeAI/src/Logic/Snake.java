/*
 * Represents a snake
 * Author: Thomas Stüber
 * */

package Logic;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.LinkedList;

import Logic.Field.CellType;
import javafx.scene.paint.Color;


public class Snake {
	private int score; //current score of this snake
	private LinkedList<Point> segments; //snake segments, snake head is last element
	private int grow; //tail of the snake isn't deleted while moving as long as grow is > 0
	private GameInfo gameInfo;
	private SnakeBrain brain;
	private boolean alive;
	private Color color;
	private boolean canSetWall;
	private boolean isSpeededUp;
	private int speedUpTicksLeft; //number ticks the speedup lasts
	private BrainThread thread;
	
	//move directions
	public enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN
	}
	
	public Snake(Point startPosition, GameInfo gameInfo, SnakeBrain brain, Color color) {
		super();
		this.score = 0;
		this.segments = new LinkedList<Point>();
		this.segments.add(startPosition);
		this.grow = 0;
		this.gameInfo = gameInfo;
		this.brain = brain;
		this.alive = true;
		this.color = color;
		this.canSetWall = false;
		this.isSpeededUp = false;
		this.thread = new BrainThread(brain, gameInfo, this);
	}
	
	public void changeScore(int delta) {
		score += delta;
	}
	
	public void grow(int n) {
		grow += n;
	}
	
	public void move() {
		long id = thread.getId();
		thread.start();
		long starttime =  ManagementFactory.getThreadMXBean().getThreadCpuTime(id);
		//System.out.println(starttime);
		while(thread.isAlive()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (thread.isAlive()) {
			thread.stop();
			String color = "";
			if (this.color == Color.YELLOWGREEN) {
				color = "yellowgreen";
			} else if (this.color == Color.BLUEVIOLET) {
				color = "blueviolet";
			}
			System.out.println(color + " snake considered to long which direction to take");
		}
		Snake.Direction direction = thread.nextMove();
		
		thread = new BrainThread(brain, gameInfo, this);
		Point head = segments.getLast();
		
		//calculate new head position
		Point newHead = new Point(head.x, head.y);
		switch(direction) {
		case DOWN:
			newHead.y++;
			break;
		case LEFT:
			newHead.x--;
			break;
		case RIGHT:
			newHead.x++;
			break;
		case UP:
			newHead.y--;
			break;
		default:
			break;
		}
		if (newHead.x == -1) {
			newHead.x = gameInfo.field().width()-1;
		}
		if (newHead.x == gameInfo.field().width()) {
			newHead.x = 0;
		}
		if (newHead.y == -1) {
			newHead.y = gameInfo.field().height()-1;
		}
		if (newHead.y == gameInfo.field().height()) {
			newHead.y = 0;
		}
		
		segments.addLast(newHead);
		
		if (grow == 0) { //don't grow, delete tail
			Point rp = segments.removeFirst();
			//If Tail is on edge, replace it with WALL and not SPACE
			if(rp.x==0||rp.x==29||rp.y==0||rp.y==19){
				gameInfo.field().setCell(Field.CellType.WALL, rp); 		
			}
			else{
			  gameInfo.field().setCell(Field.CellType.SPACE, rp);
			}
		} else { //tail isn't deleted, snake grew one field
			grow--;
		}
	}
	
	public Point headPosition() {
		return segments.getLast();
	}
	
	public LinkedList<Point> segments() {
		return segments;
	}
	
	public boolean alive() {
		return alive;
	}
	
	public void kill() {
		alive = false;
	}
	
	public Color color() {
		return color;
	}
	
	//sets the attribute canSetWall to a given boolean
	public void setCanSetWall(boolean newCanSetWall) {
		canSetWall = newCanSetWall;
	}
	
	//returns the attribute canSetwall
	public boolean getCanSetWall() {
		return canSetWall;
	}
	
	//sets the wall on the field at a given point in a given direction, if the snake has recently eaten the feature "Wall"
	public void setWall(Point centerPoint, Direction direction) {
		if (canSetWall) {
			gameInfo.field().setWall(centerPoint, direction);
			setCanSetWall(false);
		}
	}
	
	// speeds the snake up
	public void speedUp() {
		isSpeededUp = true;
		speedUpTicksLeft = 10;
	}
	
	// returns, whether the snake is speeded up or not
	public boolean isSpeededUp() {
		return isSpeededUp;
	}
	
	// returns, how many ticks the speedup lasts
	public int getSpeedUpTicksLeft() {
		return speedUpTicksLeft;
	}
	
	// decrements speedUpTicksLeft
	public void decSpeedUpTicksLeft() {
		speedUpTicksLeft--;
		if (speedUpTicksLeft <= 0) {
			isSpeededUp = false;
		}
	}
	
	public double getScore() {
		// TODO Auto-generated method stub
		return score;
	}
	
	public void setHead(Point portal) {
		segments.addLast(portal);	
	}
	
	public void switchHeadTail() {
		Collections.reverse(segments);
	}
	
	public void setSegments(LinkedList<Point> segments) {
		this.segments = segments;
	}

	public void cutTail() {
		int snakeSize = this.segments.size();
		if (snakeSize > 1){
			int cutSize = 3 + snakeSize / 4;
			if (cutSize > snakeSize - 1)
				cutSize = snakeSize - 1;
			Point positionToDelete;
			for(int i = 0; i < cutSize; i++){
				positionToDelete = this.segments.removeFirst();
				gameInfo.field().setCell(CellType.SPACE, positionToDelete);
			}
		}
		
		
	}
	public SnakeBrain getBrain()
	{
		return brain;
	}
}
