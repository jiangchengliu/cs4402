package src.pas.battleship.agents;

import java.util.ArrayList;
import java.util.List;

// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;


public class ProbabilisticAgent extends Agent {

    public ProbabilisticAgent(String name) {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game) {
        if (game.getEnemyShipTypeToNumRemaining().isEmpty()) {
            return null;
        }
        int rows = game.getGameConstants().getNumRows();
        int cols = game.getGameConstants().getNumCols();

        Coordinate move = null;
        double bestProb = 0f;

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                Outcome outcome = game.getEnemyBoardView()[x][y];
                if (outcome != Outcome.UNKNOWN) {
                    continue; 
                }
                Coordinate newMove = new Coordinate(x, y);
                double prob = getShipProbability(game, newMove);
                if (prob > bestProb) {
                    bestProb = prob;
                    move = newMove;
                }
            }
        }
        return move;
    }

    private double getShipProbability(GameView game, Coordinate coordinate) {
        double prob = 0.5f;
        List<Coordinate> adjCoordinates = getAdjCoordinates(game, coordinate);
        for (Coordinate adjCoordinate : adjCoordinates) {
            int adjX = adjCoordinate.getXCoordinate();
            int adjY = adjCoordinate.getYCoordinate();
            Outcome outcome = game.getEnemyBoardView()[adjX][adjY];
            if (outcome == Outcome.HIT) {
                prob += 0.80;
            } else if (outcome == Outcome.MISS) {
                prob += 0.1;
            } else if (outcome == Outcome.UNKNOWN) {
                prob += 0.1;
            }
        }
        return prob;
    }

    private List<Coordinate> getAdjCoordinates(GameView game, Coordinate move) {
        List<Coordinate> coordinates = new ArrayList<>();

        int[] xOffset = {1, 0, -1, 0};
        int[] yOffset = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            int newX = move.getXCoordinate() + xOffset[i];
            int newY = move.getYCoordinate() + yOffset[i];

            if (game.isInBounds(newX, newY)) {
                Coordinate neighbor = new Coordinate(newX, newY);
                coordinates.add(neighbor);
            }
        }

        return coordinates;
    }

    @Override
    public void afterGameEnds(final GameView game) {  }

}
