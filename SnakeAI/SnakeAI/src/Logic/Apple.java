/*
 * Represents one target object, could be subclassed to implement other objects
 * Author: Thomas Stüber
 * */

package Logic;

public class Apple {
	int score;
	int grow;
	Point position;
	
	public Apple(int score, int grow, Point position) {
		this.score = score;
		this.grow = grow;
		this.position = position;
	}
	
	//apply bonus or malus for the eaten object to the snake which ate it
	public void apply(Snake snake) {
		snake.grow(grow);
		snake.changeScore(score);
	}

	public double getScore() {
		return score;
	}

	public Point getPosition() {
		return position;
	}
}
