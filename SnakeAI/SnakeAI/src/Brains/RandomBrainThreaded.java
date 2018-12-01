package Brains;

import java.lang.management.ManagementFactory;
import java.util.Random;

import javafx.scene.paint.Color;
import Logic.GameInfo;
import Logic.Point;
import Logic.Snake;
import Logic.SnakeBrain;
import Logic.Field.CellType;
import Logic.Snake.Direction;

public class RandomBrainThreaded implements SnakeBrain {
	long startingTime;
	public RandomBrainThreaded() {
		long id = Thread.currentThread().getId();
		startingTime =  ManagementFactory.getThreadMXBean().getThreadCpuTime(id);
		
	}

	private class CalculationThread extends Thread {
		public Direction last;
		private GameInfo gameInfo;
		private Snake snake;

		public CalculationThread(GameInfo gameInfo, Snake snake) {
			last = null;
			this.gameInfo = gameInfo;
			this.snake = snake;
		}
		private Direction nextDirection(GameInfo gameInfo, Snake snake) {
			Random rand = new Random();
			Direction d;
			if (rand.nextDouble() < 0.95 && last != null && isMoveValid(last, snake, gameInfo)) {
				d = last;
			} else {
				do {
					d = Direction.values()[rand.nextInt(4)];
				} while(!isMoveValid(d, snake, gameInfo) && isValidMovePossible(snake, gameInfo));
			}
			
			last = d;
			
			return d;
		}
		
		private boolean isMoveValid(Direction d, Snake snake, GameInfo gameInfo) {
			Point newHead = new Point(snake.headPosition().x, snake.headPosition().y);
			switch(d) {
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
			
			return gameInfo.field().cell(newHead) == CellType.SPACE || gameInfo.field().cell(newHead) == CellType.APPLE 
					|| gameInfo.field().cell(newHead) == CellType.FEATUREWALL || gameInfo.field().cell(newHead) == CellType.CHANGESNAKE || gameInfo.field().cell(newHead) == CellType.CHANGEHEADTAIL;
		}
		
		private boolean isValidMovePossible(Snake snake, GameInfo gameInfo) {
			return isMoveValid(Direction.DOWN, snake, gameInfo) || isMoveValid(Direction.UP, snake, gameInfo) || isMoveValid(Direction.LEFT, snake, gameInfo) || isMoveValid(Direction.RIGHT, snake, gameInfo);
		}
		
		public void run() {
			double sum = 0;
			for (int i = 0;i < 200000;i++) {
				//fancy KI stuff (das hier wird wegoptimiert, eure echten Berechnungen nicht)
				sum += Math.log(i)/Math.sqrt(i);
			}
			System.out.println(sum);
			last = nextDirection(gameInfo, snake);
		}
	}

	@Override
	public Direction nextDirection(GameInfo gameInfo, Snake snake) {
		CalculationThread t = new CalculationThread(gameInfo, snake); //hier könntet ihr auch weitere Parameter übergeben oder sogar eine Instanz eurer bisherigen Brain-Klasse
		long id = Thread.currentThread().getId();
		t.start();
		while(ManagementFactory.getThreadMXBean().getThreadCpuTime(id) - startingTime < 3000000000l && t.isAlive()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (t.isAlive()) {
			//hier euer Notfallcode, was soll passieren wenn Rechnen nicht fertig, schnelle Entscheidung
		} else {
			return t.last;
		}
		return null;
	}
	

}
