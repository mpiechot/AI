package UnitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import Logic.Field;
import Logic.Point;
import Logic.Portals;
import Logic.Snake.Direction;
import Logic.Field.CellType;
import Util.HamiltonPath;
import Util.Node;
import Util.TempSnake;
import Util.UtilFunctions;

public class HamiltonPathTest {

	@Test
	public void testTailRightOverHead() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[13];
		snakePos[0] = new Point(4,4);
		snakePos[1] = new Point(5,4);
		snakePos[2] = new Point(5,5);
		snakePos[3] = new Point(5,6);
		snakePos[4] = new Point(5,7);
		snakePos[5] = new Point(4,7);
		snakePos[6] = new Point(3,7);
		snakePos[7] = new Point(2,7);
		snakePos[8] = new Point(2,6);
		snakePos[9] = new Point(2,5);
		snakePos[10] = new Point(2,4);
		snakePos[11] = new Point(3,4);
		snakePos[12] = new Point(3,5);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake, snake,portal);
		assertEquals("Point [x=4, y=4] <- Point [x=4, y=5] <- Point [x=4, y=6] <- Point [x=3, y=6] <- Point [x=3, y=5]", path.getPath());
	}
	@Test
	public void testTailOverHead() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[19];
		snakePos[0] = new Point(8,6);
		snakePos[1] = new Point(9,6);
		snakePos[2] = new Point(9,7);
		snakePos[3] = new Point(10,7);
		snakePos[4] = new Point(10,8);
		snakePos[5] = new Point(10,9);
		snakePos[6] = new Point(10,10);
		snakePos[7] = new Point(10,11);
		snakePos[8] = new Point(9,11);
		snakePos[9] = new Point(8,11);
		snakePos[10] = new Point(7,11);
		snakePos[11] = new Point(7,10);
		snakePos[12] = new Point(6,10);
		snakePos[13] = new Point(6,9);
		snakePos[14] = new Point(6,8);
		snakePos[15] = new Point(6,7);
		snakePos[16] = new Point(7,7);
		snakePos[17] = new Point(8,7);
		snakePos[18] = new Point(8,8);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake,snake,portal);
		assertEquals("Point [x=9, y=7] <- Point [x=9, y=8] <- Point [x=9, y=9] <- Point [x=9, y=10] <- Point [x=8, y=10] <- Point [x=8, y=9] <- Point [x=7, y=9] <- Point [x=7, y=8] <- Point [x=8, y=8]", path.getPath());
	}
	@Test
	public void testTailNextToHead() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[18];
		snakePos[0] = new Point(15,10);
		snakePos[1] = new Point(15,9);
		snakePos[2] = new Point(16,9);
		snakePos[3] = new Point(17,9);
		snakePos[4] = new Point(17,10);
		snakePos[5] = new Point(17,11);
		snakePos[6] = new Point(17,12);
		snakePos[7] = new Point(16,12);
		snakePos[8] = new Point(15,12);
		snakePos[9] = new Point(14,12);
		snakePos[10] = new Point(13,12);
		snakePos[11] = new Point(12,12);
		snakePos[12] = new Point(12,11);
		snakePos[13] = new Point(12,10);
		snakePos[14] = new Point(12,9);
		snakePos[15] = new Point(13,9);
		snakePos[16] = new Point(14,9);
		snakePos[17] = new Point(14,10);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake, snake,portal);
		System.out.println(path);
		System.out.println(path.getPath());
		assertEquals("Point [x=16, y=9] <- Point [x=16, y=10] <- Point [x=16, y=11] <- Point [x=15, y=11] <- Point [x=14, y=11] <- Point [x=13, y=11] <- Point [x=13, y=10] <- Point [x=14, y=10]", path.getPath());
	}
	public void testSnakeFormsSnail() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[31];
		snakePos[0] = new Point(15,10);
		snakePos[1] = new Point(15,9);
		snakePos[2] = new Point(15,8);
		snakePos[3] = new Point(16,8);
		snakePos[4] = new Point(16,9);
		snakePos[5] = new Point(16,10);
		snakePos[6] = new Point(16,11);
		snakePos[7] = new Point(15,11);
		snakePos[8] = new Point(14,11);
		snakePos[9] = new Point(14,10);
		snakePos[10] = new Point(14,9);
		snakePos[11] = new Point(14,8);
		snakePos[12] = new Point(14,7);
		snakePos[13] = new Point(14,6);
		snakePos[14] = new Point(15,6);
		snakePos[15] = new Point(16,6);
		snakePos[16] = new Point(17,6);
		snakePos[17] = new Point(18,6);
		snakePos[18] = new Point(18,7);
		snakePos[19] = new Point(18,8);
		snakePos[20] = new Point(18,9);
		snakePos[21] = new Point(18,10);
		snakePos[22] = new Point(18,11);
		snakePos[23] = new Point(18,12);
		snakePos[24] = new Point(18,13);
		snakePos[25] = new Point(17,13);
		snakePos[26] = new Point(16,13);
		snakePos[27] = new Point(15,13);
		snakePos[28] = new Point(14,13);
		snakePos[29] = new Point(14,12);
		snakePos[30] = new Point(15,12);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake, snake,portal);
		assertEquals("Point [x=15, y=8] <- Point [x=15, y=7] <- Point [x=16, y=7] <- Point [x=17, y=7] <- Point [x=17, y=8] <- Point [x=17, y=9] <- Point [x=17, y=10] <- Point [x=17, y=11] <- Point [x=17, y=12] <- Point [x=16, y=12] <- Point [x=15, y=12]", path.getPath());
	}
	@Test
	public void testSnakeBlockedByEnemy() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[25];
		snakePos[0] = new Point(25,12);
		snakePos[1] = new Point(26,12);
		snakePos[2] = new Point(27,12);
		snakePos[3] = new Point(27,13);
		snakePos[4] = new Point(26,13);
		snakePos[5] = new Point(25,13);
		snakePos[6] = new Point(24,13);
		snakePos[7] = new Point(23,13);
		snakePos[8] = new Point(22,13);
		snakePos[9] = new Point(21,13);
		snakePos[10] = new Point(20,13);
		snakePos[11] = new Point(19,13);
		snakePos[12] = new Point(18,13);
		snakePos[13] = new Point(17,13);
		snakePos[14] = new Point(16,13);
		snakePos[15] = new Point(15,13);
		snakePos[16] = new Point(15,14);
		snakePos[17] = new Point(15,15);
		snakePos[18] = new Point(15,16);
		snakePos[19] = new Point(15,17);
		snakePos[20] = new Point(15,18);
		snakePos[21] = new Point(16,18);
		snakePos[22] = new Point(17,18);
		snakePos[23] = new Point(18,18);
		snakePos[24] = new Point(19,18);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		Point[] enemyPos = new Point[30];
		enemyPos[0] = new Point(27,2);
		enemyPos[1] = new Point(26,2);
		enemyPos[2] = new Point(25,2);
		enemyPos[3] = new Point(24,2);
		enemyPos[4] = new Point(24,3);
		enemyPos[5] = new Point(24,4);
		enemyPos[6] = new Point(24,5);
		enemyPos[7] = new Point(24,6);
		enemyPos[8] = new Point(24,7);
		enemyPos[9] = new Point(24,8);
		enemyPos[10] = new Point(24,9);
		enemyPos[11] = new Point(24,10);
		enemyPos[12] = new Point(24,11);
		enemyPos[13] = new Point(25,11);
		enemyPos[14] = new Point(26,11);
		enemyPos[15] = new Point(27,11);
		enemyPos[16] = new Point(28,11);
		enemyPos[17] = new Point(28,12);
		enemyPos[18] = new Point(28,13);
		enemyPos[19] = new Point(28,14);
		enemyPos[20] = new Point(27,14);
		enemyPos[21] = new Point(26,14);
		enemyPos[22] = new Point(25,14);
		enemyPos[23] = new Point(24,14);
		enemyPos[24] = new Point(23,14);
		enemyPos[25] = new Point(22,14);
		enemyPos[26] = new Point(21,14);
		enemyPos[27] = new Point(20,14);
		enemyPos[28] = new Point(19,14);
		enemyPos[29] = new Point(18,14);
		TempSnake enemy = new TempSnake(enemyPos);
		for(Point p : enemy.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake, enemy,portal);
		assertEquals("Point [x=17, y=13] <- Point [x=17, y=14] <- Point [x=16, y=14] <- Point [x=16, y=15] <- Point [x=16, y=16] <- Point [x=16, y=17] <- Point [x=17, y=17] <- Point [x=17, y=16] <- Point [x=17, y=15] <- Point [x=18, y=15] <- Point [x=19, y=15] <- Point [x=20, y=15] <- Point [x=21, y=15] <- Point [x=22, y=15] <- Point [x=23, y=15] <- Point [x=24, y=15] <- Point [x=25, y=15] <- Point [x=26, y=15] <- Point [x=27, y=15] <- Point [x=28, y=15] <- Point [x=28, y=16] <- Point [x=28, y=17] <- Point [x=28, y=18] <- Point [x=27, y=18] <- Point [x=26, y=18] <- Point [x=25, y=18] <- Point [x=24, y=18] <- Point [x=23, y=18] <- Point [x=22, y=18] <- Point [x=21, y=18] <- Point [x=20, y=18] <- Point [x=20, y=17] <- Point [x=21, y=17] <- Point [x=22, y=17] <- Point [x=23, y=17] <- Point [x=24, y=17] <- Point [x=25, y=17] <- Point [x=26, y=17] <- Point [x=27, y=17] <- Point [x=27, y=16] <- Point [x=26, y=16] <- Point [x=25, y=16] <- Point [x=24, y=16] <- Point [x=23, y=16] <- Point [x=22, y=16] <- Point [x=21, y=16] <- Point [x=20, y=16] <- Point [x=19, y=16] <- Point [x=18, y=16] <- Point [x=18, y=17] <- Point [x=19, y=17] <- Point [x=19, y=18]", path.getPath());
	}
	@Test
	public void testSnakeAtCorner() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[18];
		snakePos[0] = new Point(10,18);
		snakePos[1] = new Point(9,18);
		snakePos[2] = new Point(8,18);
		snakePos[3] = new Point(7,18);
		snakePos[4] = new Point(6,18);
		snakePos[5] = new Point(5,18);
		snakePos[6] = new Point(5,17);
		snakePos[7] = new Point(5,16);
		snakePos[8] = new Point(5,15);
		snakePos[9] = new Point(5,14);
		snakePos[10] = new Point(5,13);
		snakePos[11] = new Point(5,12);
		snakePos[12] = new Point(4,12);
		snakePos[13] = new Point(3,12);
		snakePos[14] = new Point(2,12);
		snakePos[15] = new Point(1,12);
		snakePos[16] = new Point(1,13);
		snakePos[17] = new Point(1,14);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake,snake,portal);
		assertEquals("Point [x=5, y=18] <- Point [x=4, y=18] <- Point [x=4, y=17] <- Point [x=4, y=16] <- Point [x=4, y=15] <- Point [x=4, y=14] <- Point [x=4, y=13] <- Point [x=3, y=13] <- Point [x=3, y=14] <- Point [x=3, y=15] <- Point [x=3, y=16] <- Point [x=3, y=17] <- Point [x=3, y=18] <- Point [x=2, y=18] <- Point [x=1, y=18] <- Point [x=1, y=17] <- Point [x=2, y=17] <- Point [x=2, y=16] <- Point [x=1, y=16] <- Point [x=1, y=15] <- Point [x=2, y=15] <- Point [x=2, y=14] <- Point [x=1, y=14]", path.getPath());
	}
	
	
	@Test
	public void testNextDirectionToChoose() {
		Field f = Field.defaultField(30, 20);
		Point[] snakePos = new Point[13];
		snakePos[0] = new Point(4,4);
		snakePos[1] = new Point(5,4);
		snakePos[2] = new Point(5,5);
		snakePos[3] = new Point(5,6);
		snakePos[4] = new Point(5,7);
		snakePos[5] = new Point(4,7);
		snakePos[6] = new Point(3,7);
		snakePos[7] = new Point(2,7);
		snakePos[8] = new Point(2,6);
		snakePos[9] = new Point(2,5);
		snakePos[10] = new Point(2,4);
		snakePos[11] = new Point(3,4);
		snakePos[12] = new Point(3,5);
		TempSnake snake = new TempSnake(snakePos);
		for(Point p : snake.segments())
			f.setCell(CellType.SNAKE, p);
		HamiltonPath hpath = new HamiltonPath();
		Portals portal= new Portals();
		Node path = hpath.getMaxPath(snake.headPosition(), f, snake,snake,portal);
		assertEquals(Direction.UP,UtilFunctions.getDirection(path.getFrom().getActual(), path.getActual()));
		path = path.getFrom();
		assertEquals(Direction.UP,UtilFunctions.getDirection(path.getFrom().getActual(), path.getActual()));
		path = path.getFrom();
		assertEquals(Direction.RIGHT,UtilFunctions.getDirection(path.getFrom().getActual(), path.getActual()));
		path = path.getFrom();
		assertEquals(Direction.DOWN,UtilFunctions.getDirection(path.getFrom().getActual(), path.getActual()));
	}
}
