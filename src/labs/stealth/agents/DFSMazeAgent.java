package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;

import java.util.HashSet;   // will need for dfs
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;     // will need for dfs
import java.util.Set;       // will need for dfs


// JAVA PROJECT IMPORTS


public class DFSMazeAgent
    extends MazeAgent
{

    public DFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        Set<Vertex> visited = new HashSet<Vertex>();
        Stack<Path> stack = new Stack<>();
        visited.add(src);
        stack.add(new Path(src));
        while (!stack.isEmpty()) {
            Path current = stack.pop();
            Vertex currentVertex = current.getDestination();
            int currentX = currentVertex.getXCoordinate();
            int currentY = currentVertex.getYCoordinate();
            if ((DistanceMetrics.chebyshevDistance(currentX, currentY, goal.getXCoordinate(), goal.getYCoordinate()) <= 1) && !currentVertex.equals(goal)) {
                return current;
            }
            visited.add(currentVertex);
            for (Direction dir: Direction.values()) {
                int neighborX = currentX + dir.xComponent();
                int neighborY = currentY + dir.yComponent();
                Vertex neighbor = new Vertex(neighborX, neighborY);
                if ((state.inBounds(neighborX, neighborY)) && (!visited.contains(neighbor)) && !(state.isResourceAt(neighborX, neighborY))) {
                   Path newPath = new Path(neighbor, 1f, current);
                   stack.add(newPath);

                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        Stack<Vertex> current = this.getCurrentPlan();
        Stack<Vertex> copy = new Stack<>();
        copy.addAll(current);
    
        while (!copy.isEmpty()) {
            Vertex currentVertex = copy.pop();
            int currentX = currentVertex.getXCoordinate();
            int currentY = currentVertex.getYCoordinate();
            if (state.isResourceAt(currentX, currentY)) {
                return true;
            } 
        }
        return false;
    }


}
