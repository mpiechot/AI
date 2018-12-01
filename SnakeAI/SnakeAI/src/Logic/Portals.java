package Logic;
import java.util.Random;

/**
 * Two Portals appear and disappear in random intervalls on the Field. 
 * If a snakes head moves on a Portal it is teleported to the corresponding 
 * other portal. If the portals disappear while a snake is still teleporting, the 
 * part of the snake that has not moved through is cut off. 
 * 
 * Functions to use for other groups:
 * - use the getPortal functions to get the Point of the portals
 * - use the isActive() function to know whether the Portal is active!!! 
 *   Important (portal1 and portal2 even when not active contain the last Position) 
 * - use the getTTL() function to know how long a portal is going to last
 * 
 * @author Chris Meier, Florian Riedl
 */

public class Portals{
    
    // Time to Live 
    private double ttl;
    private final int maxTTL;
    private final int minTTL;
    
    /* the amount the ttl is decremented; Per game tick the decrement is called 
    2 times for each snake; default value is therefore 0.5 */
    private final double ttlDecrement;
    
    /* each round when the portal is not active, this is the propability if a new
       pair appears. The value ranges from 0.0 to 1.0  */
    private final double randomPopUpWk;
    
    // the minimum Distance that should be between the Portals
    private final int minDistancePortals;
    private Point portal1;
    private Point portal2;
   
    // marks if the portal is active, on the field or not
    private boolean active;
    
    // counts the Elements of a snake, which have passed through the portals  
    private int counterSnakeElements;
   
    public Portals(){
        ttl=0;           
        maxTTL=120;
        minTTL=30;
        ttlDecrement=0.5;        
        randomPopUpWk=0.005;
        minDistancePortals=12;        
        active=false;   
        counterSnakeElements=0;
    }
    
    // GETTER and SETTER
    public Point getPortal1(){
    	return portal1;
    }
    
    public Point getPortal2(){
    	return portal2;
    }
    
    public boolean isActive(){
    	return active;
    } 
    
    public double getTTL(){
    	return ttl;
    }
    
    /**
     *  chooses random Value between maxTTL and minTTL and sets the Objects ttl var
     */
    private void setNewTTL(){
        Random randgen = new Random();
        do{
            ttl = randgen.nextInt(maxTTL)+1;            
        }while(ttl <= minTTL);
   }
          
    /**
     * checks if a snakes segment is on a portal
     * @param snake the current snake that is active
     * @return true if the snake is on the portal
     */
    public boolean isSnakeOnPortal(Snake snake){
        return snake.segments().contains(portal1) || snake.segments().contains(portal2);
    }
    
    /**
     * checks if it is time a portal should pop up or a portal should disappear
     * @param field
     * @param snake 
     */
    public void portalAppeareance(Field field, Snake snake){
        Random randgen = new Random();        
        if (ttl > 0){
            // the portal is active and living
            ttl= ttl - ttlDecrement;
            // check if a snake is currently running through
            if(isSnakeOnPortal(snake)){
                counterSnakeElements=counterSnakeElements+1;
            }              
        }
        else{
            /* the portal is inactive and it is decided random whether a 
            new should pop up */
            if(!active){            
                 if(randgen.nextDouble() <= randomPopUpWk){
                        appear(field);                        
                    }
                }                   
            /* the portal is still active but ttl has reached 0, therfore it has 
            to disappear */
            else{
                disappear(field, snake);               
            }       
        }
    }
    
    /**
     * teleports the head of the snake, that is on a portal, to the corresponding
     * other portal and updated the field
     * @param field
     * @param snake 
     */             
    public void teleportHead(Field field, Snake snake){
    	if (ttl > 0 && isSnakeOnPortal(snake)){
            if (snake.headPosition().equals(portal1)){
                teleportHeadToPortal(portal2, field, snake);                
            }
            else{
                teleportHeadToPortal(portal1, field, snake);
            }
    	}
    }    
    /**
     * teleports the head of the given snake to a portal 
     * @param portal    corresponding other portal the snakes head isn't on
     * @param field
     * @param snake     snake that is to be teleported
     */
    private void teleportHeadToPortal(Point portal, Field field, Snake snake){
        snake.setHead(portal);
        // deletes one segment of snake, cause the head was "moved"
        field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
        counterSnakeElements=1;
    } 
    
    /**
     * sets the Portal to a new available random location on the GameField
     * @param field 
     */
    private void appear(Field field){
        // set Portals to active
        this.active=true;
        // initialize new random time to live
        setNewTTL();
        // assign to new points
        do{
            portal1 = getRandPoint(field);
            portal2 = getRandPoint(field);
        }while(!isPortalPositionProper(field));        
        // add the portals to the Field
        field.addPortal(this);
    }
    
    /**
     * removes Portals from field and cuts of the given snake if it hasn't fully
     * passed through 
     * @param field
     * @param snake 
     */
    public void disappear(Field field, Snake snake){
        this.active=false;
        // if snake is has not fully gone through portal remove the rest 
        if(isSnakeOnPortal(snake)){
            int countSegmentsToDelete= snake.segments().size() - counterSnakeElements;
            /* retrieves and deletes segments from Snake and sets the 
            position in field to a SPACE */
            for(int i=0; i < countSegmentsToDelete; i++){
                field.setCell(Field.CellType.SPACE, snake.segments().pollFirst());
            }            
            counterSnakeElements=0;
        }
        field.removePortal(this);
    }    
    
    /**
     * checks if the actual positions of the portals are possible, valid and proper
     * @param field
     * @return true if the portals are not the same Point, are on a free Cell and 
     * are a minimum Distance away from each other
     */
    private boolean isPortalPositionProper(Field field){
         return field.cell(portal1) == Field.CellType.SPACE
            && field.cell(portal2) == Field.CellType.SPACE
            && portal1 != portal2 
            && calcPointDistance(portal1,portal2) >= minDistancePortals;          
    }
    
        
    /**
     * create random Point in the game Field
     * @param field
     * @return random Point on the game Field
     */
    public Point getRandPoint(Field field){
        Random randgen = new Random();        
        int x = randgen.nextInt(field.width());
        int y = randgen.nextInt(field.height());
        Point p =  new Point(x, y);
        return p;
    }

    /**
     * calculates the euclidian distance between two Points
     * @param point1
     * @param point2
     * @return the distance between point1 and point2 
     */
    private int calcPointDistance(Point point1, Point point2){
        return Math.abs(point1.x-point2.x)+ Math.abs(point1.y-point2.y);
       }            
 }

