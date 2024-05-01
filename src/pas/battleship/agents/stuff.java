package src.pas.battleship.agents;


import java.util.List;

// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;
import edu.bu.battleship.game.EnemyBoard;
import edu.bu.battleship.game.Constants.Rendering.Board;
import java.util.ArrayList;
import edu.bu.battleship.game.PlayerView;
import edu.bu.battleship.game.Constants;



public class ProbabilisticAgent extends Agent {

    public ProbabilisticAgent(String name) {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(GameView game) {
        List<Coordinate> bestMoves = new ArrayList<>();
        double highestProb = 0;
    
        int rows = game.getGameConstants().getNumRows();
        int cols = game.getGameConstants().getNumCols();
    
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                Coordinate currentMove = new Coordinate(x, y);
                if (game.getEnemyBoardView()[x][y] == null) {
                    bestMoves.add(currentMove);
                } else if (game.getEnemyBoardView()[x][y] == Outcome.HIT) {
                    double hitFactor = 0;
                    double total = 0;
                    for (Coordinate neigh : getAdjCoordinates(game, currentMove)) {
                        if (game.getEnemyBoardView()[neigh.getXCoordinate()][neigh.getYCoordinate()] == Outcome.HIT) {
                            hitFactor += 1;
                        }
                        total += 1;
                    }
                    double currentProb = total > 0 ? hitFactor / total : 0;
                    if (currentProb > highestProb) {
                        bestMoves.clear();
                        bestMoves.add(currentMove);
                        highestProb = currentProb;
                    } else if (currentProb == highestProb) {
                        bestMoves.add(currentMove);
                    }
                }
            }
        }
    
        Coordinate move = bestMoves.isEmpty() ? null : bestMoves.get((int) (Math.random() * bestMoves.size()));
    
        if (move == null) {
            int newX = (int) (Math.random() * rows);
            int newY = (int) (Math.random() * cols);
            move = new Coordinate(newX, newY);
        }
    
        return move;
    }

    private List<Coordinate> getAdjCoordinates(GameView game, Coordinate move) {
        List<Coordinate> coordinates = new ArrayList<>();

        int[] xOffset = {1, 0, -1, 0};
        int[] yOffset = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            int newX = move.getXCoordinate() + xOffset[i];
            int newY = move.getYCoordinate() + yOffset[i];

            if (game.isInBounds(new Coordinate(newX, newY))) {
                coordinates.add(new Coordinate(newX, newY));
            }
        }

        return coordinates;
    }




    @Override
    public void afterGameEnds(final GameView game) {}

}
