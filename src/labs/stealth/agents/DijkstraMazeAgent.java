package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;   
import edu.cwru.sepia.util.DistanceMetrics;                        // Directions in Sepia

import java.lang.Math;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue; // heap in java
import java.util.Set;
import java.util.Stack;


// JAVA PROJECT IMPORTS


public class DijkstraMazeAgent
    extends MazeAgent
{

    public DijkstraMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {

        Set<Vertex> visited = new HashSet<Vertex>();
        PriorityQueue<Path> pq = new PriorityQueue<>();
        HashMap<Vertex, Float> dist = new HashMap<>();
        visited.add(src);
        dist.put(src, 0f);
        pq.add(new Path(src));

        for (int x = 0; x < state.getXExtent(); x++) {
            for (int y = 0; y < state.getYExtent(); y++) {
                Vertex v = new Vertex(x, y);
                if (!v.equals(src)) {
                    dist.put(v, Float.POSITIVE_INFINITY);
                }
            }
        }

        while (!pq.isEmpty()) {
            Path current = pq.poll();
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
                    float edgeWeight = calculateDistance(dir);
                    float newDist = dist.get(currentVertex) + edgeWeight;
                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        Path newPath = new Path(neighbor, newDist, current);
                        pq.add(newPath);
                    }
                }
            }
        }
		return null;
    }
    private float calculateDistance (Direction dir) {
        if (dir == Direction.EAST || dir == Direction.WEST) {
            return 5f;
        }
        else if (dir == Direction.SOUTH) {
            return 1f;
        }
        else if (dir == Direction.NORTH) {
            return 10f;
        }
        else if (dir == Direction.NORTHEAST || dir == Direction.NORTHWEST) {
            return (float) Math.sqrt(Math.pow(10f, 2) + Math.pow(5f, 2));
        }
        else if (dir == Direction.SOUTHEAST || dir == Direction.SOUTHWEST) {
            return (float) Math.sqrt(Math.pow(1f, 2) + Math.pow(5f, 2));
        }
        return 0f;
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
