package src.labs.infexf.agents;

import java.util.HashSet;
import java.util.Stack;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.DistanceMetrics;
import edu.cwru.sepia.environment.model.state.ResourceType;
import java.util.Set;


// JAVA PROJECT IMPORTS


public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state)
    {
        //stateview - where agent is, where archer is -> run away from archers and get to gold
        // archer is a different get state, the state you are getting closer to archer is more danger, while getting closer to gold is less danger
        // archer = negative influence, gold = positive influence
        // A* algorithm, need a heauristic function
        float totalNegativeFactor = 0f;
        int agent = this.getPlayerNumber();
        int srcX = src.getXCoordinate();
        int srcY = src.getYCoordinate();
        int dstX = dst.getXCoordinate();
        int dstY = dst.getYCoordinate();

        float distance = DistanceMetrics.chebyshevDistance(srcX, srcY, dstX, dstY);
        

        for (Integer enemyID: this.getOtherEnemyUnitIDs()) {
            Unit.UnitView enemy = state.getUnit(enemyID);
            if (enemy.equals(null)) {
                continue;
            }
            int range = enemy.getTemplateView().getRange();
            int distanceToEnemy = DistanceMetrics.chebyshevDistance(dstX, dstY, enemy.getXPosition(), enemy.getYPosition());
            float spaceBetweenEnemy = distanceToEnemy - range;
            float adjustedSpaceBetweenEnemy = Math.max(spaceBetweenEnemy, 1f);
            /*
              if (adjustedDistance <= range) {
                prox += exponentialDecay(adjustedSpaceBetweenEnemy);
            }
            else {
                prox += 0f;
            }
             */
           
            //totalNegativeFactor += 1000000f * exponentialDecay(-adjustedSpaceBetweenEnemy * 0.25f);
            //getting trapped by 
            totalNegativeFactor += 1000000f * inverse(adjustedSpaceBetweenEnemy);
           
        }


        return distance + totalNegativeFactor;
    }

    /*
     private float exponentialDecay(float distance) {
        return (float) Math.exp(-distance * 0.25f);
    }
     */
    

    private float inverse(float distance) {
        return 1/(distance * distance);
    }


    
/*
 private float positiveFactor(StateView state, int dstX, int dstY) {
        int townHallX = 0;
        int townhallY = 0;
        for (Integer unitID : state.getAllUnitIds()) {
            UnitView unit = state.getUnit(unitID);
            if (unit.getTemplateView().getName().equals("TownHall")) {
                townHallX = unit.getXPosition();
                townhallY = unit.getYPosition();
                float adjustedDistance = Math.max(DistanceMetrics.chebyshevDistance(dstX, dstY, townHallX, townhallY), 1f);
                return adjustedDistance;
                
            }
            Vertex entry = this.getEntryPointVertex();
            int entryX = entry.getXCoordinate();
            int entryY = entry.getYCoordinate();

            if (!state.isUnitAt(townHallX, townhallY)) {
                float distance2 = DistanceMetrics.chebyshevDistance(entryX, entryY, dstX, dstY);
                return (distance2 * distance2);

            }
        }
        return 1f;
    } 
 */


    
    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        
         // should check if the current plan goes into contains the enemy/resoures. Still applys to game state graph. -> even getting into the arhcer's range. 
        Stack<Vertex> current = this.getCurrentPlan();
        Stack<Vertex> copy = new Stack<>();
        copy.addAll(current);
    
        while (!copy.isEmpty()) {
            Vertex currentVertex = copy.pop();
            int currentX = currentVertex.getXCoordinate();
            int currentY = currentVertex.getYCoordinate();
            for (ResourceNode.ResourceView resource: state.getAllResourceNodes()) {
                if (resource.getType() == ResourceNode.Type.TREE && resource.getXPosition() == currentX && resource.getYPosition() == currentY) {
                    return true;
                }
            }
             for (Integer enemyID: this.getOtherEnemyUnitIDs()) {
                Unit.UnitView enemy = state.getUnit(enemyID);
                if (enemy == null) {
                    continue;
                }
                int range = enemy.getTemplateView().getRange();
                int distance = DistanceMetrics.chebyshevDistance(currentX, currentY, enemy.getXPosition(), enemy.getYPosition());
                float spaceBetweenEnemy = distance - range;
                if (spaceBetweenEnemy <= 0f) {
                    return true;
                }
            }
             
        }
        
        return false;
        
    }

}

/*
 * package src.labs.infexf.agents;

import java.util.Stack;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.DistanceMetrics;
import edu.cwru.sepia.environment.model.state.ResourceType;


// JAVA PROJECT IMPORTS


public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state)
    {
        //stateview - where agent is, where archer is -> run away from archers and get to gold
        // archer is a different get state, the state you are getting closer to archer is more danger, while getting closer to gold is less danger
        // archer = negative influence, gold = positive influence
        // A* algorithm, need a heauristic function
        //int srcX = src.getXCoordinate();
        //int srcY = src.getYCoordinate();
        int dstX = dst.getXCoordinate();
        int dstY = dst.getYCoordinate();

        //float distance = DistanceMetrics.chebyshevDistance(srcX, srcY, dstX, dstY);
        float negativeFactor = negativeFactor(state, dstX, dstY);
        //float positiveFactor = positiveFactor(state, dstX, dstY);

        return negativeFactor;
    }

    //

    private float negativeFactor(StateView state, int dstX, int dstY) {
        float totalNegativeFactor = 0f;
        for (Integer enemyID: this.getOtherEnemyUnitIDs()) {
            Unit.UnitView enemy = state.getUnit(enemyID);
            int range = enemy.getTemplateView().getRange();
            int distance = DistanceMetrics.chebyshevDistance(dstX, dstY, enemy.getXPosition(), enemy.getYPosition());
            float adjustedDistance = Math.max(distance, 1f);
            float prox = Math.max(distance - range, 1f);
            totalNegativeFactor += 100f * (1/adjustedDistance) * (1/prox);
            
        }
        return totalNegativeFactor;
    }
    
     /*
       private float positiveFactor(StateView state, int dstX, int dstY) {
        for (Integer unitID: state.getAllUnitIds()) {
            UnitView unit = state.getUnit(unitID);
            String unitName = unit.getTemplateView().getName();
            if (unitName == "TownHall") {
                int townHallX = unit.getXPosition();
                int townhallY = unit.getYPosition();
                int distance = DistanceMetrics.chebyshevDistance(dstX, dstY, townHallX, townhallY);
                float adjustedDistance = Math.max(distance, 1f);
                return 1/adjustedDistance;
            }
        }
        return 1f;
    }
      */
 
