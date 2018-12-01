package Util;

import java.util.LinkedList;

import Logic.Field;
import Logic.Field.CellType;
import Logic.Point;
import Logic.Snake;
import Logic.Snake.Direction;

/**
 * TempSnake is a class to copy the snake for further calculations without changeing the original snake
 * @author Marco
 *
 */
public class TempSnake 
{
	private LinkedList<Point> segments;
	private int grow;
	private boolean alive;
	private String name;
	private double score;
	
	/**
	 * constructor of a temporary snake
	 * @param points - position of the snake body
	 */
	public TempSnake(Point[] points)
	{
		this.segments = new LinkedList<Point>();
		this.grow = 0;
		for(Point p : points)
		{
			segments.add(p);
		}
		this.name = "Test Snake";
		this.alive = true;
		this.score = 0;
	}
	
	/**
	 * constructor of a temporary snake
	 * @param snake - snake of the real game that is supposed to be copied
	 * @param name - belongs to that real snake
	 */
	public TempSnake(Snake snake, String name)
	{
		this.segments = new LinkedList<Point>();
		this.grow = 0;
		for(Point p : snake.segments())
		{
			Point temp = new Point(p.x,p.y);
			segments.add(temp);
		}
		this.name = name;
		this.alive = true;
		this.score = snake.getScore();
	}
	
	/**
	 * constructor of a temporary snake
	 * @param snake - temporary snake that is supposed to be copied to get a new temporary snake
	 */
	public TempSnake(TempSnake snake)
	{
		this.segments = new LinkedList<Point>();
		this.grow = 0;
		for(Point p : snake.segments())
		{
			Point temp = new Point(p.x,p.y);
			segments.add(temp);
		}
		this.name = snake.name;
		this.alive = true;
		this.score = snake.getScore();
	}
	
	/**
	 * constructor of a temporary snake
	 * @param snake - snake of the real game that is supposed to be copied 
	 */
	public TempSnake(Snake snake) 
	{
		this.segments = new LinkedList<Point>();
		this.grow = 0;
		for(Point p : snake.segments())
		{
			Point temp = new Point(p.x,p.y);
			segments.add(temp);
		}
		this.alive = true;
		this.score = snake.getScore();
	}
	
	/**
	 * extends the snake
	 * @param n - extension value, given as integer
	 */
	public void grow(int n) 
	{
		grow += n;
		changeScore(n*10);
	}
	
	/**
	 * moves the snake to a certain direction
	 * @param dir - one of 4 directions to take: DOWN, UP, LEFT, RIGHT
	 */
	public void move(Direction dir) 
	{
		Point head = segments.getLast();
		Point newHead = new Point(head.x, head.y);
		
		switch(dir) {
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
		
		segments.addLast(newHead);
		
		if (grow == 0) { //don't grow, delete tail
			segments.removeFirst();
		} else { //tail isn't deleted, snake grew one field
			grow--;
		}
	}
	
	/**
	 * moves the snake to a certain direction in the field
	 * @param dir - one of 4 directions to take: DOWN, UP, LEFT, RIGHT
	 * @param field - current game field 
	 * @return new head position of the snake after the move
	 */
	public Point move(Direction dir, Field field) 
	{
		Point head = segments.getLast();
		
		//calculate new head position
		Point newHead = new Point(head.x, head.y);
		switch(dir) {
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
			newHead.x = field.width()-1;
		}
		if (newHead.x == field.width()) {
			newHead.x = 0;
		}
		if (newHead.y == -1) {
			newHead.y = field.height()-1;
		}
		if (newHead.y == field.height()) {
			newHead.y = 0;
		}
		
		segments.addLast(newHead);
		
		if (grow == 0) { //don't grow, delete tail
			Point rp = segments.removeFirst();
			field.setCell(CellType.SPACE, rp);
		} else { //tail isn't deleted, snake grew one field
			grow--;
		}
		
		return newHead;
	}
	
	/**
	 * get position of the snake head on the field
	 * @return point in the field
	 */
	public Point headPosition() 
	{
		return segments.getLast();
	}
	
	/**
	 * get position of the whole snake in the field
	 * @return a list of points in the field
	 */
	public LinkedList<Point> segments() 
	{
		return segments;
	}
	
	/**
	 * get current life situation of the snake, living or dead
	 * @return boolean true if snake still lives, else false
	 */
	public boolean alive() 
	{
		return alive;
	}
	
	/**
	 * turns off the snakes life
	 */
	public void kill() 
	{
		alive = false;
	}

	/**
	 * getter method for the name of the snake
	 * @return name as a string 
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * getter method for the game score of the snake
	 * @return score as a floating-point number
	 */
	public double getScore() 
	{
		return score;
	}

	/**
	 * increases or decreases the game score by a value
	 * @param delta - changing value
	 */
	public void changeScore(double delta) 
	{
		this.score += delta;
	}
	
}
