# AI
A collection of projects about AI


## SnakeAI

The SnakeAI Project was done while the "team-project" at the university. The topic was to write an ai to play snake against another snake. We had to write our own ai on one side and on the other implement new features like power ups. Those power ups had to be covered by the ai as well.
As mentioned, this was a team-project, so we worked in groups of two and competed against the other groups with our ai.

### Keypoints for SnakeAI
- Alpha-Beta-Pruning
- A* Pathfinding
- Hamilton Path
- AI Strategys

### Algorithms

**A* Pathfinding:** The first thing we implemented was a pathfinding algorithm, so that the snake can find the shortest path to the apple or its target. This algorithm is implemented in the `Pathfinding.java` file.

**Hamilton Path:** For the next step we noticed that the snake easily locks itself up when the snake gets longer. To solve this situation we have to calculate a path to the body part that will get free first and thus let the snake move freely again. But we cant use the A* Pathfinding for this situation since we dont want to be as fast as possible at this position. It's the oppisite: we want to find the longest path to this position.
We found a nice explanation of an algorihm online and used it to calculate the hamilton path (the longest path) to a selected Position. For this Algorihm we implemented some JUnit-Tests to test the implementation.

**Alpha-Beta-Pruning:** With the above implementation we already had an AI that can survive a little while but it doesnt attack the opponent snake in any way. We thought of other ways to implement the AI and came up with Alpha-Beta-Pruning. This algorithm helps to look some steps into the future and lets the snake choose a optimal route to win the game. The only problem we had was that the semester was soon over and our implementation was buggy. (And it still is).

### Implemented Features

As a group we implemented two simple Powerups for the snake-battle. The first one was a powerup to switch the snakes head with its tail. That was simple to implement, because the snakes are stored in a linked list and we could just reverse the list.

Our second powerup was a feature to swap both snakes. So if our AI "eats" this powerup it will change the snakebody with the opponents snake. Later we discovered that this feature was way to powerful, because we could just circle around this item and then "eat" the powerup to change the snakebodies. Now the opponents snake is in a position where it can only eat itself -> we win. This is only possible if the snake is at least 8 segments long.

## CodeItAward

This project was a competition in writing a AI for a strategy game. It was held by itDesign Tübingen and I participated twice in it but wasnt able to take part in the finals because of other things on the same day (like the legend of zelda orchestra :D )

### Keypoints for CodeItAward
- A*-Pathfinding
- Unit-Management
- Combat Management
- Alpha-Beta-Pruning
- Trace Algorithm

### Algorithms

**Pathfinding:** First I implemented the A* Pathfinding Algorithm so that my units can find the shortest path to their destination. Later I tried another pathfinding algorithm, the so called _Trace Algorithm_. But the main Problem here was to solve Pathfinding with multiple units. If a Unit tries to walk towards a resource location and another unit wants to walk with ressources back to the base the pathfinding needs to find a solution for the problem that one unit has to make room for the other unit so that they dont block each others way forever. I haven't solved it entirely but it worked a bit better with waiting. During the competition they released new maps for the AI's. One map was with small ways from the base to the resources and that kind of destroyed my solution of waiting. Here one unit had to move back until the other unit can pass the unit and the path ahead is free again.

**Unit and Combat Management:** The second focus of my AI was when to build new units and what units should be built. In this competition there were 3 types of units. A collector, a warrior and an archer. Archers were quite overpowered during the competition, so I developed their behaviour and focused more on this unit. My strategy here was to keep a fixed distance to all enemy units so that the archer can shoot at them but they cant attack back (as long as they arent archers themself).

For the unit creation I tried a more dynamic way by observing the current state of the gamefield and by calculating different scores decide which unit to build. This function looks like this:
```java
private double getScore(UnitType type, ClientRoundState roundState)
{
  double score = 0;
  switch(type)
  {
    case WARRIOR:
      if(UnitMovement.enemyUnitPos.size() >0)
      {
        score -= UnitMovement.stayCounter*150;

        //Decide on the count of enemy units if we need more warriors
        if(myUnits.get(UnitType.WARRIOR).size() < UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()){
          if(myUnits.get(UnitType.WARRIOR).size()<=4)
          score += 300;
        }
        else{
          score -= 200;
        }

        //if there aren't any enemies left, we dont need more warriors so reduce the score!
        if(noEnemys())
          score -= 300;

        //warriors are good in defeating collectors, so build warriors if there are enemy collectors
        if(!UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).isEmpty())
          score += UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size()*80;

        //warriros are bad in defeating archers, so build less if there are any enemy archers
        if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
          score -= UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*170;
      }
      break;
    case ARCHER:
      if(UnitMovement.enemyUnitPos.size() >0)
      {
        score += UnitMovement.stayCounter*150;
        if(myUnits.get(UnitType.WARRIOR).size() < UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size())
          score -= 100;
        else
          score += 300;
        if(noEnemys())
          score -= 300;
        if(!UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).isEmpty())
          score += UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size()*30;
        if(!UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).isEmpty())
          score += UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()*208;
        if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
          score += UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*310;
      }
      break;
    case COLLECTOR:
      if(noEnemys())
        score += 700;
      else if(ownUnitCount > 12)
        score -= 30;

      if(myUnits.get(UnitType.COLLECTOR).size() < UnitMovement.enemyUnitPos.get(UnitType.COLLECTOR).size())
        score += 591;

      if(!UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).isEmpty())
        score -= UnitMovement.enemyUnitPos.get(UnitType.WARRIOR).size()*90;

      if(!UnitMovement.enemyUnitPos.get(UnitType.ARCHER).isEmpty())
        score -= UnitMovement.enemyUnitPos.get(UnitType.ARCHER).size()*100;

      //To get ressources we need at least 4 Collectors!
      if(myUnits.get(UnitType.COLLECTOR).size() < 4)
        score += 591;

      break;
  }
  return score;
}
```
