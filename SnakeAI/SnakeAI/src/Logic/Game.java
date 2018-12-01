/*
 * Stores the current state of the game, implements main logic main loop
 * Author: Thomas Stüber
 * */

package Logic;
import Logic.Portals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.paint.Color;
//import Brains.AwesomeBrain;
//import Brains.HorstAI;
//import Brains.NCageBrain;
//import Brains.NotSoRandomBrain1;
import Brains.RandomBrain;
//import Brains.SuperBrain;


public class Game {
	private ArrayList<Snake> snakes;
	private Field field;
	private Random rand;
	private boolean drawOutput;        
	private int gameSpeed;
	private int gameticks;
//	private double appleProbability; //probability per move that an apple spawns 
//	private double featureWallProbability; //probability per move that this feature spawns
	//0 = apple, 1 = featureWall, 2 = changeSnake, 3 = changeHeadTail, 4 = speedUp 5=cutSnake 6=OpenField
	private double[] probabilitys; //probability per move that this feature spawns
	private int playersLeft; //is decreased every time a player dies
	private int currentSnake;
	private Portals portal;
	private boolean snakeDone2ndMove;
	private int OpenFieldTTL;
	
	public int getOpenFieldTTL(){
		return OpenFieldTTL;
	}
	
	public ArrayList<Snake> getSnakes() {
		return snakes;
	}
	
	public Portals getPortal(){
        return portal;
	}
	
	public Field getField() {
		return field;
	}

    public void setOutput(boolean value) {
        this.drawOutput=value;
    }
    
    public void setGameSpeed(int speed) {
        this.gameSpeed=speed;
    }

	public Game(ArrayList<SnakeBrain> brains, ArrayList<Point> startPositions, ArrayList<Color> colors, Field field, double[] probabilitys) {
		this.drawOutput=true;
        this.gameSpeed=1000;
		this.field = field;
		currentSnake = 0;
		this.gameticks=0;
		GameInfo gameInfo = new GameInfo(this);
		snakes = new ArrayList<Snake>();
		playersLeft = brains.size();
		snakeDone2ndMove = true; // = true causes that snake only do double moves in the turn after eaten speedUp feature
		OpenFieldTTL=ThreadLocalRandom.current().nextInt(30, 120 + 1);  //Random Value, how long the Field stays open
		
		//adding the snakes
		for (int i = 0;i < brains.size();i++) {
			addSnake(new Snake(startPositions.get(i),gameInfo, brains.get(i), colors.get(i)), startPositions.get(i));
		}
		
		this.portal= new Portals();
		
		rand = new Random();
		
		this.probabilitys = probabilitys;
//		this.appleProbability = appleProbability;
//		this.featureWallProbability = featureWallProbability;
	}
	
	//add a snake to the game
	public void addSnake(Snake snake, Point start) {
		snakes.add(snake);
		field.setCell(Field.CellType.SNAKE, start);
	}

	public static void main(String[] args) {
		Field field = Field.defaultField(30, 20);
		
		Point start1 = new Point(2, 2);
		Point start2 = new Point(27, 17);
		ArrayList<Point> startPositions = new ArrayList<Point>();
		startPositions.add(start1);
		startPositions.add(start2);
		ArrayList<SnakeBrain> brains = new ArrayList<SnakeBrain>();
		brains.add(new RandomBrain());
		brains.add(new RandomBrain());
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(Color.YELLOWGREEN);
		colors.add(Color.AZURE);
		double[] probabilitys = {0.1, 0.005, 0.003, 0.003, 0.003, 0.003, 0.003};
		Game game = new Game(brains, startPositions, colors, field, probabilitys);
		game.run();
	}
	
	
	//main loop
	public void run() {
		while (playersLeft > 1) {
			//System.out.println(portal.getTTL());
			nextStep();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.out.println("Das ist garnicht mal so gut...");
				e.printStackTrace();
			}
		}
	}


	public void nextStep() {
		if (playersLeft > 1) {
			
			addingFeatures();
			
			//finding next snake which is alive
			Snake snake = snakes.get(currentSnake);
			while (snake.alive() == false) {
				currentSnake++;
			}
			
			//portal: checks if portals appear or disappear
            portal.portalAppeareance(field, snake);
            
			//moving the current snake
            snake.move();
			
            updateField(snake);
            
			//drawing of the field and everything
//			field.draw();
			
			//next player, same player if player has 2nd move
			if (snake.isSpeededUp() && !snakeDone2ndMove) {
				snakeDone2ndMove = true;
				snake.decSpeedUpTicksLeft();
			} else {
				currentSnake++;
				if (snake.getSpeedUpTicksLeft() > 0) { // causes that snake only do double moves in the turn after eaten speedUp feature
					snakeDone2ndMove = false;
				}
			}
			if (currentSnake == snakes.size()) {
				currentSnake = 0;
			}
		}
	}
	
	// adding features
	public void addingFeatures() {
		for (int i = 0; i <= 6; i++) {
			if (rand.nextDouble() <= probabilitys[i] && !field.isFeatureActive(i)){
				Point position = new Point(0,0);
				do {
					position.x = rand.nextInt(field.width());
					position.y = rand.nextInt(field.height());
				} while (field.cell(new Point(position.x,position.y)) != Field.CellType.SPACE);
				field.setFeature(i, position);
			}
		}
	}
	
	// update the field
	public void updateField(Snake snake) {
		Point headPosition = snake.headPosition();
		switch (field.cell(headPosition)) {
		case SPACE:
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case APPLE:
			//System.out.println(headPosition);
			Apple apple = field.getApple(headPosition);
			apple.apply(snake);
			field.removeApple(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case FEATUREWALL:
			snake.setCanSetWall(true);
			field.removeFeatureWall(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case PORTAL:
			if (portal.isActive()) portal.teleportHead(field,snake);
			break;
		case CHANGESNAKE:
			Random rand = new Random();
			if (rand.nextBoolean()) {
				field.removeChangeSnake(headPosition);
				field.setCell(Field.CellType.SNAKE, headPosition);
				break;
			}
			Snake otherSnake = null;
			for (Snake s : snakes)
				if(s != snake) otherSnake = s;
			LinkedList<Point> snake1Segments = otherSnake.segments();
			otherSnake.setSegments(snake.segments());
			snake.setSegments(snake1Segments);
			field.removeChangeSnake(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case CHANGEHEADTAIL:
			snake.switchHeadTail();
			field.removeChangeHeadTail(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case SPEEDUP:
			snake.speedUp();
			field.removeSpeedUp(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			break;
		case CUTTAIL:
			snake.cutTail();
			field.removeCutTail(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
		case OPENFIELD:
			field.hasOpenField();
			field.removeOpenField(headPosition);
			field.setCell(Field.CellType.SNAKE, headPosition);
			field.setFieldIsOpenTrue();
		default: //snake hit itself or the wall, walks thru wall when FieldIsOpen is true
			if(field.getFieldIsOpen()){
				if(headPosition.x==0){
					field.setCell(Field.CellType.SNAKE, new Point(field.width()-2,field.height()-1-headPosition.y));					
					snake.setHead(new Point(field.width()-2,field.height()-1-headPosition.y));
					field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
					field.setCell(Field.CellType.WALL, headPosition);					
				}
				else if(headPosition.x==29){
					field.setCell(Field.CellType.SNAKE, new Point(1,field.height()-1-headPosition.y));					
					snake.setHead(new Point(1,field.height()-1-headPosition.y));
					field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
					field.setCell(Field.CellType.WALL, headPosition);					
				}
				else if(headPosition.y==0){
					field.setCell(Field.CellType.SNAKE, new Point(field.width()-1-headPosition.x,field.height()-1));					
					snake.setHead(new Point(field.width()-1-headPosition.x,field.height()-1));
					field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
					field.setCell(Field.CellType.WALL, headPosition);					
				}
				else if(headPosition.y==19){
					field.setCell(Field.CellType.SNAKE, new Point(field.width()-1-headPosition.x,1));					
					snake.setHead(new Point(field.width()-1-headPosition.x,1));
					field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
					field.setCell(Field.CellType.WALL, headPosition);					
				}
				else
					break;
			}
			String causeOfDeath = "", color = "";
			if (field.cell(headPosition) ==  Field.CellType.WALL) {
				causeOfDeath = "wall";
			} else if (field.cell(headPosition) ==  Field.CellType.SNAKE) {
				causeOfDeath = "snake";
			}
			if (snake.color() == Color.YELLOWGREEN) {
				color = "yellowgreen";
			} else if (snake.color() == Color.BLUEVIOLET) {
				color = "blueviolet";
			}
			System.out.println(color + " snake died because of hitting a " + causeOfDeath);
			
			field.setCell(Field.CellType.SNAKE, headPosition);
			snake.kill();
			playersLeft--;
		}
		
		if(field.getFieldIsOpen()){
			OpenFieldTTL--;
//			System.out.println(OpenFieldTTL);
			if(OpenFieldTTL<=0){
				field.setFieldIsOpenFalse();
				OpenFieldTTL=ThreadLocalRandom.current().nextInt(30, 120 + 1);
			}
		}
		
        //portal: prevent portals from being eaten
		if(portal.isActive()){
			field.setCell(Field.CellType.PORTAL, portal.getPortal1());
			field.setCell(Field.CellType.PORTAL, portal.getPortal2());
		}
	}
}
