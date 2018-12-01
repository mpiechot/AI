package UnitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import Logic.Field;
import Logic.Field.CellType;
import Logic.Point;
import Logic.Portals;
import Util.Node;
import Util.PathFinder;
import Util.TempSnake;

/**
 * Siehe SnakeDoku für genauen Testaufbau und erwartetem Ergebnis.
 * @author Marco
 *
 */
public class PathfindingTest {

	@Test
	public void testOneMove() {
		Field f = Field.defaultField(30, 20);
		PathFinder find = new PathFinder();
		Portals portal= new Portals();
		Point[] p = new Point[1];
		p[0] = new Point(2,2);
		Node node = find.getMinPath(new TempSnake(p), new Point(3,2), f,portal);
		assertEquals("Point [x=3, y=2] <- Point [x=2, y=2]", node.getPath());
	}
	@Test
	public void testLeftMoves() {
		Field f = Field.defaultField(30, 20);
		PathFinder find = new PathFinder();
		Portals portal= new Portals();
		Point[] p = new Point[1];
		p[0] = new Point(10,10);
		Node node = find.getMinPath(new TempSnake(p), new Point(2,10), f,portal);
		assertEquals("Point [x=2, y=10] <- Point [x=3, y=10] <- Point [x=4, y=10] <- Point [x=5, y=10] <- Point [x=6, y=10] <- Point [x=7, y=10] <- Point [x=8, y=10] <- Point [x=9, y=10] <- Point [x=10, y=10]", node.getPath());
	}
	@Test
	public void testRightMoves() {
		Field f = Field.defaultField(30, 20);
		PathFinder find = new PathFinder();
		Portals portal= new Portals();
		Point[] p = new Point[1];
		p[0] = new Point(2,10);
		Node node = find.getMinPath(new TempSnake(p), new Point(10,10), f,portal);
		assertEquals("Point [x=10, y=10] <- Point [x=9, y=10] <- Point [x=8, y=10] <- Point [x=7, y=10] <- Point [x=6, y=10] <- Point [x=5, y=10] <- Point [x=4, y=10] <- Point [x=3, y=10] <- Point [x=2, y=10]", node.getPath());
	}
	@Test
	public void testDownMoves() {
		Field f = Field.defaultField(30, 20);
		PathFinder find = new PathFinder();
		Portals portal= new Portals();
		Point[] p = new Point[1];
		p[0] = new Point(10,2);
		Node node = find.getMinPath(new TempSnake(p), new Point(10,10), f,portal);
		assertEquals("Point [x=10, y=10] <- Point [x=10, y=9] <- Point [x=10, y=8] <- Point [x=10, y=7] <- Point [x=10, y=6] <- Point [x=10, y=5] <- Point [x=10, y=4] <- Point [x=10, y=3] <- Point [x=10, y=2]", node.getPath());
	}
	@Test
	public void testUpMoves() {
		Field f = Field.defaultField(30, 20);
		PathFinder find = new PathFinder();
		Portals portal= new Portals();
		Point[] p = new Point[1];
		p[0] = new Point(10,10);
		Node node = find.getMinPath(new TempSnake(p), new Point(10,2), f,portal);
		assertEquals("Point [x=10, y=2] <- Point [x=10, y=3] <- Point [x=10, y=4] <- Point [x=10, y=5] <- Point [x=10, y=6] <- Point [x=10, y=7] <- Point [x=10, y=8] <- Point [x=10, y=9] <- Point [x=10, y=10]", node.getPath());
	}
	@Test
	public void testNoPath(){
		Field f = Field.defaultField(30, 20);
		for(int x=5;x<13;x++)
		{
			for(int y=5;y<13;y++)
			{
				if(x==5 || x==12)
				{
					f.setCell(CellType.WALL, new Point(x,y));
				}
				if(y==5 || y==12)
				{
					f.setCell(CellType.WALL, new Point(x,y));
				}
			}
		}
		PathFinder find = new PathFinder();
		Point[] p = new Point[1];
		Portals portal= new Portals();
		p[0] = new Point(10,10);
		Node node = find.getMinPath(new TempSnake(p), new Point(10,2), f,portal);
		assertEquals(null,node);
	}
//	@Test
//	public void testLeftMovesWithPortals() {
//		Field f = Field.defaultField(30, 20);
//		PathFinder find = new PathFinder();
//		Portals portal= new Portals();
//		portal.appearTest(f, new Point(8,10), new Point(3,10));
//		Point[] p = new Point[1];
//		p[0] = new Point(10,10);
//		f.draw();
//		Node node = find.getMinPath(new TempSnake(p), new Point(2,10), f,portal);
//		assertEquals("Point [x=2, y=10] <- Point [x=3, y=10] <- Point [x=8, y=10] <- Point [x=9, y=10] <- Point [x=10, y=10]", node.getPath());
//	}
}
