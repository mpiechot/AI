package Logic;

public class BrainThread extends Thread {
	private SnakeBrain brain;
	private Snake.Direction nextMove;
	private GameInfo gameInfo;
	private Snake snake;
	private boolean finished;

	public BrainThread(SnakeBrain brain, GameInfo gi, Snake s) {
		this.brain = brain;
		gameInfo = gi;
		snake = s;
		finished = false;
		nextMove = Snake.Direction.LEFT;
	}
	
	public void run() {
		finished = false;
		nextMove = brain.nextDirection(gameInfo, snake);
		finished = true;
	}
	
	public Snake.Direction nextMove() {
		return nextMove;
	}
}
