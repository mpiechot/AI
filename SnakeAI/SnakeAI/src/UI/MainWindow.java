package UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import Brains.AwesomeBrain;
import Brains.AwesomeNeuralBrain;
import Brains.AwesomeNormalBrain;
import Brains.HorstAI;
import Brains.NCageBrain;
import Brains.NotSoRandomBrain1;
import Brains.NotSoRandomBrain2;
import Brains.RandomBrain;
import Brains.RandomBrainThreaded;
import Brains.SmartBrain;
import Brains.SuperBrain;
import Brains.WallBrain;
import Logic.Apple;
import Logic.Field;
import Logic.Field.CellType;
import Logic.Game;
import Logic.Point;
import Logic.Snake;
import Logic.SnakeBrain;
import PrototypKIs.AlphaBetaSnake;
import PrototypKIs.BrainMaster;
import PrototypKIs.NewBrain;
import PrototypKIs.NewBrainTest;
import PrototypKIs.NotMovingBrain;
import PrototypKIs.SurvivalAI;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


public class MainWindow extends Application {
	private int width;
	private int height;
	private int cellWidth;
	private SnakeBrain brain1;
	private SnakeBrain brain2;
	private Canvas canvas;
	private Game game;
	private double gameSpeed;
	private Image speedUpImg = new Image(getClass().getClassLoader().getResourceAsStream("res/speedUp.png"));
	private Image changeSnakes = new Image(getClass().getClassLoader().getResourceAsStream("res/changeSnakes.png"));
	private Image reverseDir = new Image(getClass().getClassLoader().getResourceAsStream("res/reverseDirection.png"));
	private Image appleImg = new Image(getClass().getClassLoader().getResourceAsStream("res/apple.png"));
	private Image cutTailImg = new Image(getClass().getClassLoader().getResourceAsStream("res/cutTail.png"));
	private Image openFieldImg = new Image(getClass().getClassLoader().getResourceAsStream("res/openField.png"));
	
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	//TODO: read this from command line arguments
    	width = 30;
    	height = 20;
    	cellWidth = 30;
    	
    	gameSpeed = 30;
        brain1 = new BrainMaster();
        brain2 = new AlphaBetaSnake();
    	
    	Field field = Field.defaultField(30, 20);
//    	String res1 = runTournament();
//    	String res2 = runTournament2();
    	
//    	System.out.println(res1);
//    	System.out.println(res2);
		
		Point start1 = new Point(2, 2);
		Point start2 = new Point(27, 17);
		ArrayList<Point> startPositions = new ArrayList<Point>();
		startPositions.add(start1);
		startPositions.add(start2);
		ArrayList<SnakeBrain> brains = new ArrayList<SnakeBrain>();
		brains.add(brain1);
		brains.add(brain2);
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(Color.YELLOWGREEN);
		colors.add(Color.BLUEVIOLET);
		double[] probabilitys = {1, 1, 1, 1, 1, 0.002, 0.002};
		game = new Game(brains, startPositions, colors, field, probabilitys);
		//game.run();Apple
		
		//move interval of the snakes
		//TODO: add mode with "every snake gets as much time as needed"
		Timeline timeline = new Timeline(new KeyFrame(
		        Duration.millis(this.gameSpeed), new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						game.nextStep();
						gameUpdate();
					}
		        	
		        }));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
    	
        primaryStage.setTitle("Super Ultra Deluxe Snake 3000");
        
        canvas = new Canvas(cellWidth*width, cellWidth*height);
        gameUpdate();
        ScrollPane root = new ScrollPane();
        root.setMaxWidth(width*cellWidth);
        root.setMaxHeight(height*cellWidth);
        
        root.setContent(canvas);
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.setWidth(918);
        primaryStage.setHeight(641);
        primaryStage.show();
    }
    
    public void gameUpdate() {
    	GraphicsContext gc = canvas.getGraphicsContext2D();
    	gc.setFill(Color.GREEN);
    	gc.setLineWidth(5);
        gc.fillRect(0, 0, width*cellWidth, height*cellWidth);
        
        Field f = game.getField();
        for (int x = 0;x < f.width();x++) {
        	for (int y = 0;y < f.height();y++) {
            	CellType cell = f.cell(new Point(x,y));
            	switch(cell) {
				case APPLE:
					gc.setFill(Color.GREEN);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth, (y+1)*cellWidth);
					gc.drawImage(appleImg, x*cellWidth, y*cellWidth);
//					gc.setFill(Color.RED);
//					gc.fillRoundRect(x*cellWidth+6, y*cellWidth+6, cellWidth-12, cellWidth-12,10,10);
					break;
				case SNAKE:
					gc.setFill(Color.GREEN);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth, (y+1)*cellWidth);
					gc.setFill(Color.BLACK);
					gc.fillRoundRect(x*cellWidth+1, y*cellWidth+1, cellWidth-2, cellWidth-2,10,10);
					break;
				case SPACE:
					gc.setFill(Color.GREEN);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth, (y+1)*cellWidth);
					break;
				case WALL:
					gc.setFill(Color.DARKGREEN);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth, (y+1)*cellWidth);
					break;
				case FEATUREWALL: //paints the feature "Wall" on the canvas as a wall-pixel-art
					gc.setFill(Color.CORAL);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth,(y+1)* cellWidth);
					gc.setFill(Color.LIGHTGREY);
					gc.fillRect(x*cellWidth, y*cellWidth+2, cellWidth, 2);
					gc.fillRect(x*cellWidth, y*cellWidth+8, cellWidth, 2);
					gc.fillRect(x*cellWidth, y*cellWidth+14, cellWidth, 2);
					gc.fillRect(x*cellWidth, y*cellWidth+20, cellWidth, 2);
					gc.fillRect(x*cellWidth, y*cellWidth+26, cellWidth, 2);
					gc.setFill(Color.SILVER);
					gc.fillRect(x*cellWidth+5, y*cellWidth, 1, 3);
					gc.fillRect(x*cellWidth+17, y*cellWidth, 1, 3);
					gc.fillRect(x*cellWidth+29, y*cellWidth, 1, 3);
					gc.fillRect(x*cellWidth+11, y*cellWidth+3, 1, 6);
					gc.fillRect(x*cellWidth+23, y*cellWidth+3, 1, 6);
					gc.fillRect(x*cellWidth+5, y*cellWidth+9, 1, 6);
					gc.fillRect(x*cellWidth+17, y*cellWidth+9, 1, 6);
					gc.fillRect(x*cellWidth+29, y*cellWidth+9, 1, 6);
					gc.fillRect(x*cellWidth+11, y*cellWidth+15, 1, 6);
					gc.fillRect(x*cellWidth+23, y*cellWidth+15, 1, 6);
					gc.fillRect(x*cellWidth+5, y*cellWidth+21, 1, 6);
					gc.fillRect(x*cellWidth+17, y*cellWidth+21, 1, 6);
					gc.fillRect(x*cellWidth+29, y*cellWidth+21, 1, 6);
					gc.fillRect(x*cellWidth+11, y*cellWidth+27, 1, 3);
					gc.fillRect(x*cellWidth+23, y*cellWidth+27, 1, 3);
					break;
				case PORTAL: // paints the feature "Portal" on the canvas
					gc.setFill(Color.BLACK);
					gc.fillRect(x*cellWidth, y*cellWidth, (x+1)*cellWidth, (y+1)*cellWidth);
					gc.setFill(Color.BLUE);
					gc.fillRoundRect(x*cellWidth+2, y*cellWidth+2, cellWidth-4, cellWidth-4,20,50);
					break;
				case CHANGESNAKE:
					int xPos = x*cellWidth;
					int yPos = y*cellWidth;
					gc.drawImage(changeSnakes, xPos, yPos);
					break;
				case CHANGEHEADTAIL:
					xPos = x*cellWidth;
					yPos = y*cellWidth;
					gc.drawImage(reverseDir, xPos, yPos);
					break;
				case SPEEDUP:
					xPos = x*cellWidth;
					yPos = y*cellWidth;
					gc.drawImage(speedUpImg, xPos, yPos);
					break;
				case CUTTAIL:
					xPos = x*cellWidth;
					yPos = y*cellWidth;
					gc.drawImage(cutTailImg, xPos, yPos);
					break;
				case OPENFIELD:
					gc.drawImage(openFieldImg, x*cellWidth, y*cellWidth);
					break;
				case OPENFIELDPICTURE:
					gc.drawImage(openFieldImg, x*cellWidth, y*cellWidth);
					break;
				default:
					break;
            	}
            }
        }
        gc.setLineWidth(3);
        
        ArrayList<Snake> snakes = game.getSnakes();
        for (Snake snake : snakes) {
        	gc.setStroke(snake.color());
        	LinkedList<Point> segments = snake.segments();
        	if (segments.size() == 1) {
        		gc.strokeOval(segments.get(0).x * cellWidth+7, segments.get(0).y * cellWidth+7, cellWidth-14, cellWidth-14);
        	} else {
        		for (int i = 0;i < segments.size()-1;i++) {
        			Point currentSegment = segments.get(i);
        			Point nextSegment = segments.get(i+1);
        			Snake.Direction currentToNext = relativ(currentSegment, nextSegment);
        			if (currentToNext != null) {
        				switch(currentToNext) {
						case DOWN:
							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+(cellWidth/2), (currentSegment.y+1)*cellWidth-5);
							break;
						case LEFT:
							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+5, currentSegment.y*cellWidth+(cellWidth/2));
							break;
						case RIGHT:
							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), (currentSegment.x+1)*cellWidth-5, currentSegment.y*cellWidth+(cellWidth/2));
							break;
						case UP:
							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+5);
							break;
						default:
							break;
        				}
        			}
        			
        			if (i > 0) {
        				Point predSegment = segments.get(i-1);
            			Snake.Direction predToCurrent = relativ(currentSegment, predSegment);
            			if (predToCurrent != null) {
            				switch(predToCurrent) {
    						case DOWN:
    							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+(cellWidth/2), (currentSegment.y+1)*cellWidth-5);
    							break;
    						case LEFT:
    							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+5, currentSegment.y*cellWidth+(cellWidth/2));
    							break;
    						case RIGHT:
    							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), (currentSegment.x+1)*cellWidth-5, currentSegment.y*cellWidth+(cellWidth/2));
    							break;
    						case UP:
    							gc.strokeLine(currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+(cellWidth/2), currentSegment.x*cellWidth+(cellWidth/2), currentSegment.y*cellWidth+5);
    							break;
    						default:
    							break;
            				}
            			}
        			}
        		}
        		gc.strokeOval(segments.getLast().x * cellWidth+7, segments.getLast().y * cellWidth+7, cellWidth-14, cellWidth-14);
        	}
        }
    }
    
    //TODO: add cases where snake leaves field at one side and enters at another side
    private Snake.Direction relativ(Point p1, Point p2) {
    	if (p1.x == p2.x && p1.y == p2.y-1) {
    		return Snake.Direction.DOWN;
    	} else if (p1.x == p2.x && p1.y == p2.y+1) {
    		return Snake.Direction.UP;
    	} else if (p1.x == p2.x-1 && p1.y == p2.y) {
    		return Snake.Direction.RIGHT;
    	} else if (p1.x == p2.x+1 && p1.y == p2.y) {
    		return Snake.Direction.LEFT;
    	} 
    	return null;
    }
    
    
    int brainClassToInt(SnakeBrain brain) {
//    	if (brain.getClass().getName() == "PrototypKIs.BrainMaster") {
//    		return 0;
//    	} 
    	if (brain.getClass().getName() == "PrototypKIs.AlphaBetaSnake" || brain.getClass().getName() == "PrototypKIs.BrainMaster") {
    		return 0;
    	} 
    	else if (brain.getClass().getName() == "Brains.SmartBrain") {
    		return 1;
    	}
    	else if (brain.getClass().getName() == "Brains.NotSoRandomBrain1") {
    		return 2;
    	}
    	else if (brain.getClass().getName() == "Brains.RandomBrain") {
    		return 3;
    	}
    	else if (brain.getClass().getName() == "Brains.SuperBrain") {
    		return 4;
    	}
    	return -1;
    }
    
    String runTournament() {
    	ArrayList<SnakeBrain> brains = new ArrayList<SnakeBrain>();
//    	brains.add(new BrainMaster());
    	brains.add(new AlphaBetaSnake());
    	brains.add(new SmartBrain());
    	brains.add(new NotSoRandomBrain1());
    	brains.add(new RandomBrain());
    	brains.add(new SuperBrain());
    	HashMap<Integer, Integer> wins = new HashMap<Integer, Integer>();
    	wins.put(0, 0);
    	wins.put(1, 0);
    	wins.put(2, 0);
    	wins.put(3, 0);
    	wins.put(4, 0);
    	int[][] winsAgainst = new int[wins.size()][wins.size()];
    	
    	for(int count=0;count<2;count++)
    	{
	    	for (int i = 0;i < brains.size();i++) {
	    		SnakeBrain b1 = brains.get(i);
	    		for (int j = 0;j < brains.size();j++) {
	    			SnakeBrain b2 = brains.get(j);
	    			if (b1 == b2) {
	    				continue;
	    			}
	    			
	    			Field field = Field.defaultField(30, 20);
	    			field.addApple(new Apple(50, 1, new Point(1,2)), new Point(1,2));
	    			
	    			Point start1 = new Point(2, 2);
	    			Point start2 = new Point(27, 17);
	    			ArrayList<Point> startPositions = new ArrayList<Point>();
	    			startPositions.add(start1);
	    			startPositions.add(start2);
	    			ArrayList<SnakeBrain> bs = new ArrayList<SnakeBrain>();
	    			bs.add(b1);
	    			bs.add(b2);
	    			ArrayList<Color> colors = new ArrayList<Color>();
	    			colors.add(Color.YELLOWGREEN);
	    			colors.add(Color.BLUEVIOLET);
	    			double[] probabilitys = {1, 0.005, 0.002, 0.002, 0.005, 0.001, 0.002};
	    			game = new Game(bs, startPositions, colors, field, probabilitys);
	    			
	    			for (int s = 0;s < 1000;s++) {
	    				game.nextStep();
	    			}
	    			
	    			if (game.getSnakes().get(0).alive() && !game.getSnakes().get(1).alive()) {
	    				System.out.println(b1.getClass().getName());
	    				winsAgainst[brainClassToInt(b1)][brainClassToInt(b2)]++;
	    				wins.put(brainClassToInt(b1), wins.get(brainClassToInt(b1))+1);
	    			} else if (game.getSnakes().get(1).alive() && !game.getSnakes().get(0).alive()) {
	    				System.out.println(b2.getClass().getName());
	    				winsAgainst[brainClassToInt(b2)][brainClassToInt(b1)]++;
	    				wins.put(brainClassToInt(b2), wins.get(brainClassToInt(b2))+1);
	    			} else {
	    				if (game.getSnakes().get(0).getScore() > game.getSnakes().get(1).getScore()) {
	    					System.out.println(b1.getClass().getName());
	    					winsAgainst[brainClassToInt(b1)][brainClassToInt(b2)]++;
	    					wins.put(brainClassToInt(b1), wins.get(brainClassToInt(b1))+1);
	    				} else {
	    					System.out.println(b2.getClass().getName());
	    					winsAgainst[brainClassToInt(b2)][brainClassToInt(b1)]++;
	    					wins.put(brainClassToInt(b2), wins.get(brainClassToInt(b2))+1);
	    				}
	    			}
//	    			System.out.println(wins);
	    			
	    		}
	    	}
    	}
    	String result = "";
    	result += "AlphaBetaSnake = 0\n";
    	result += wins+"\n";
    	for(int i=0;i<winsAgainst.length;i++)
    	{
    		result += i+": " + Arrays.toString(winsAgainst[i])+"\n";
    	}
    	return result;
    }  
String runTournament2() {
	ArrayList<SnakeBrain> brains = new ArrayList<SnakeBrain>();
//	brains.add(new BrainMaster());
	brains.add(new BrainMaster());
	brains.add(new SmartBrain());
	brains.add(new NotSoRandomBrain1());
	brains.add(new RandomBrain());
	brains.add(new SuperBrain());
	HashMap<Integer, Integer> wins = new HashMap<Integer, Integer>();
	wins.put(0, 0);
	wins.put(1, 0);
	wins.put(2, 0);
	wins.put(3, 0);
	wins.put(4, 0);
	int[][] winsAgainst = new int[wins.size()][wins.size()];
	
	for(int count=0;count<2;count++)
	{
    	for (int i = 0;i < brains.size();i++) {
    		SnakeBrain b1 = brains.get(i);
    		for (int j = 0;j < brains.size();j++) {
    			SnakeBrain b2 = brains.get(j);
    			if (b1 == b2) {
    				continue;
    			}
    			
    			Field field = Field.defaultField(30, 20);
    			field.addApple(new Apple(50, 1, new Point(1,2)), new Point(1,2));
    			
    			Point start1 = new Point(2, 2);
    			Point start2 = new Point(27, 17);
    			ArrayList<Point> startPositions = new ArrayList<Point>();
    			startPositions.add(start1);
    			startPositions.add(start2);
    			ArrayList<SnakeBrain> bs = new ArrayList<SnakeBrain>();
    			bs.add(b1);
    			bs.add(b2);
    			ArrayList<Color> colors = new ArrayList<Color>();
    			colors.add(Color.YELLOWGREEN);
    			colors.add(Color.BLUEVIOLET);
    			double[] probabilitys = {1, 0.005, 0.002, 0.002, 0.005, 0.001, 0.002};
    			game = new Game(bs, startPositions, colors, field, probabilitys);
    			
    			for (int s = 0;s < 1000;s++) {
    				game.nextStep();
    			}
    			
    			if (game.getSnakes().get(0).alive() && !game.getSnakes().get(1).alive()) {
    				System.out.println(b1.getClass().getName());
    				winsAgainst[brainClassToInt(b1)][brainClassToInt(b2)]++;
    				wins.put(brainClassToInt(b1), wins.get(brainClassToInt(b1))+1);
    			} else if (game.getSnakes().get(1).alive() && !game.getSnakes().get(0).alive()) {
    				System.out.println(b2.getClass().getName());
    				winsAgainst[brainClassToInt(b2)][brainClassToInt(b1)]++;
    				wins.put(brainClassToInt(b2), wins.get(brainClassToInt(b2))+1);
    			} else {
    				if (game.getSnakes().get(0).getScore() > game.getSnakes().get(1).getScore()) {
    					System.out.println(b1.getClass().getName());
    					winsAgainst[brainClassToInt(b1)][brainClassToInt(b2)]++;
    					wins.put(brainClassToInt(b1), wins.get(brainClassToInt(b1))+1);
    				} else {
    					System.out.println(b2.getClass().getName());
    					winsAgainst[brainClassToInt(b2)][brainClassToInt(b1)]++;
    					wins.put(brainClassToInt(b2), wins.get(brainClassToInt(b2))+1);
    				}
    			}
//    			System.out.println(wins);
    			
    		}
    	}
	}
	String result = "";
	
	result += "BrainMaster = 0\n";
	result += wins+"\n";
	for(int i=0;i<winsAgainst.length;i++)
	{
		result += i+": " + Arrays.toString(winsAgainst[i])+"\n";
	}
	return result;
}  
}