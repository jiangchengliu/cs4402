package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;


import java.util.HashSet;       // will need for bfs
import java.util.Queue;         // will need for bfs
import java.util.LinkedList;    // will need for bfs
import java.util.Set;           // will need for bfs
import java.util.Stack;


// JAVA PROJECT IMPORTS


public class BFSMazeAgent
    extends MazeAgent
{

    public BFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        Set<Vertex> visited = new HashSet<Vertex>();
        Queue<Path> queue = new LinkedList<>();
        visited.add(src);
        queue.add(new Path(src));
        

        while (!queue.isEmpty()) {
            Path current = queue.poll();
            Vertex currentVertex = current.getDestination();
            int currentX = currentVertex.getXCoordinate();
            int currentY = currentVertex.getYCoordinate();
            
            if ((DistanceMetrics.chebyshevDistance(currentX, currentY, goal.getXCoordinate(), goal.getYCoordinate()) <= 1) && !current.equals(goal)) {
                return current;
            }
            visited.add(currentVertex);
            for (Direction dir: Direction.values()) {
                int neighborX = currentX + dir.xComponent();
                int neighborY = currentY + dir.yComponent();
                Vertex neighbor = new Vertex(neighborX, neighborY);
                if ((state.inBounds(neighborX, neighborY)) && (!visited.contains(neighbor)) && !(state.isResourceAt(neighborX, neighborY))) {
                   Path newPath = new Path(neighbor, 1f, current);
                   queue.add(newPath);

                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldReplacePlan(StateView state) {
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
