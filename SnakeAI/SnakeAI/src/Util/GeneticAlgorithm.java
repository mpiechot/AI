//package Util;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Random;
//
//import Brains.HorstAI;
//import Brains.RandomBrain;
//import Logic.Apple;
//import Logic.Field;
//import Logic.Game;
//import Logic.Point;
//import Logic.Snake;
//import Logic.SnakeBrain;
//import javafx.scene.paint.Color;
//
/**
 * GeneticAlgorithm used for finding the best values for the alphaBeta evaluation function.
 * This code needs to be updated before using it again. Also the alphaBeta Algorithm needs a better evaluation function.
 */
//public class GeneticAlgorithm {
//
//	private static Random r = new Random();
//	public static void main(String[] args)
//	{
//		int[][] population = new int[10][6];
//		int[] wins = new int[10];
//		int max =0;
//		int[] good = new int[10];
//		population[0][0] = 6021;
//		population[0][1] = 84591;
//		population[0][2] = -48241;
//		population[0][3] = 94523;
//		population[0][4] = -15323;
//		population[0][5] = -6788;
//		for(int i=1;i<population.length;i++)
//		{
//			population[i][0] = r.nextInt(100000);
//			population[i][1] = r.nextInt(100000);
//			population[i][2] = -r.nextInt(100000);
//			population[i][3] = r.nextInt(100000);
//			population[i][4] = -r.nextInt(100000);
//			population[i][5] = -r.nextInt(100000);
//		}
//		for(int rounds=0;rounds<10;rounds++)
//		{
//			wins = new int[10];
//			for(int i=0;i<population.length;i++)
//			{
//				for(int j=0;j<100;j++)
//				{
//					Field field = Field.defaultField(30, 20);
//					field.addApple(new Apple(50, 1, new Point(1,2)), new Point(1,2));
//					field.addApple(new Apple(50, 1, new Point(3,2)), new Point(3,2));
//					field.addApple(new Apple(50, 1, new Point(2,1)), new Point(2,1));
//					field.addApple(new Apple(50, 1, new Point(2,3)), new Point(2,3));
//					
//					Point start1 = new Point(2, 2);
//					Point start2 = new Point(27, 17);
//					ArrayList<Point> startPositions = new ArrayList<Point>();
//					startPositions.add(start1);
//					startPositions.add(start2);
//					ArrayList<SnakeBrain> brains = new ArrayList<SnakeBrain>();
//					brains.add(new HorstAI(population[i]));
//					brains.add(new RandomBrain());
//					ArrayList<Color> colors = new ArrayList<Color>();
//					colors.add(Color.YELLOWGREEN);
//					colors.add(Color.AZURE);
//					Game game = new Game(brains, startPositions, colors, field, 0.1);
//					game.run();
//		
//					if(game.getSnakes().get(0).alive())
//					{
//						wins[i]++;
//					}			
//				}
//			}
//			System.out.println("Round "+rounds + ":" + Arrays.toString(wins));
//			for(int i=0;i<wins.length;i++)
//			{
//				if(wins[i] > max)
//				{
//					max = wins[i];
//					good = population[i];
//				}
//			}
//			population = selection(population,wins);
//		}
//		
//		System.out.println(Arrays.toString(good));
//		System.out.println(max);
//		
//	}
//	private static int[][] selection(int[][] population, int[] wins)
//	{
//		int[][] newPopulation = new int[population.length][population[0].length];
//		for(int pop1=0;pop1<newPopulation.length;pop1++)
//		{
//			int sum = 0;
//			for(int i : wins)
//				sum+= i;
//			
//			int cross1 = r.nextInt(sum);
//			int index1 =0;
//			for(int i=0;i<wins.length;i++)
//			{
//				cross1 -= wins[i];
//				if(cross1 < 0)
//				{
//					index1 = i;
//					break;
//				}
//			}
//			int cross2 = r.nextInt(sum);
//			int index2 =0;
//			for(int i=0;i<wins.length;i++)
//			{
//				cross2 -= wins[i];
//				if(cross2 < 0)
//				{
//					index2 = i;
//					break;
//				}
//			}
//			newPopulation[pop1] = crossover(population[index1],population[index2]);
//			double mutation = Math.random();
//			if(mutation <0.01)
//			{
//				index2 = r.nextInt(6);
//				System.out.println("Mutate!");
//				newPopulation[pop1][index2] = r.nextInt(100000);
//			}
//		}
//		return newPopulation;
//	}
//	private static int[] crossover(int[] pop1, int[] pop2){
//		int j = r.nextInt(pop1.length-1);
//		int[] cross = new int[pop1.length]; 
//		for(int i=0;i<pop1.length;i++)
//		{		
//			if(i>j)
//				cross[i] = pop2[i];
//			else
//				cross[i] = pop1[i];
//		}
//		return cross;
//	}
//	private static void mutation(int[][] population){
//		
//	}
//}