package src.pas.tetris.agents;


import java.util.ArrayList;
// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;
import edu.bu.tetris.utils.Coordinate;
import edu.bu.tetris.game.minos.Mino.Orientation;



public class TetrisQAgent
    extends QAgent
{

    public static final double EXPLORATION_PROB = 0.05;

    private Random random;

    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int numPixelsInImage = 5;
        final int outDim = 1;

        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(numPixelsInImage, 32));
        //qFunction.add(new ReLU());
        qFunction.add(new ReLU());
        qFunction.add(new Dense(32, 32));
        qFunction.add(new ReLU());
        //qFunction.add(new Tanh());
        qFunction.add(new Dense(32, outDim));        

        return qFunction;
    }

    /**
        This function is for you to figure out what your features
        are. This should end up being a single row-vector, and the
        dimensions should be what your qfunction is expecting.
        One thing we can do is get the grayscale image
        where squares in the image are 0.0 if unoccupied, 0.5 if
        there is a "background" square (i.e. that square is occupied
        but it is not the current piece being placed), and 1.0 for
        any squares that the current piece is being considered for.
        
        We can then flatten this image to get a row-vector, but we
        can do more than this! Try to be creative: how can you measure the
        "state" of the game without relying on the pixels? If you were given
        a tetris game midway through play, what properties would you look for?
     */

private int getEmptyCells(GameView game) {
    int emptyCells = 0;
    for (int r = 2; r < Board.NUM_ROWS; r++) {
        for (int c = 0; c < Board.NUM_COLS; c++) {
            if (!game.getBoard().isCoordinateOccupied(c, r)) {
                emptyCells++;
            }
        }
    }
    return emptyCells;
}




 private double getAggregateHeight(GameView game) {
    double heightSum = 0f;
    for (int r = 2; r < Board.NUM_ROWS; r++) {
        double height = 0;
        for (int c = 0; c < Board.NUM_COLS; c++) {
            if (game.getBoard().isCoordinateOccupied(c, r)) {
                height = Board.NUM_ROWS - c;
                break;
            }
        }
        heightSum += height;
    }
    return heightSum;
}


     private double getBumpiness(GameView game) {
        double bumpiness = 0f;
        for (int r = 2; r < Board.NUM_COLS - 1; r++) {
            double height1 = 0;
            double height2 = 0;
            for (int c = 0; c < Board.NUM_ROWS; c++) {
                if (game.getBoard().isCoordinateOccupied(r, c)) {
                    height1 = Board.NUM_ROWS - c;
                    break;
                }
            }
            for (int c = 0; c < Board.NUM_ROWS; c++) {
                if (game.getBoard().isCoordinateOccupied(r + 1, c)) {
                    height2 = Board.NUM_ROWS - c;
                    break;
                }
            }
            bumpiness += Math.abs(height1 - height2);
        }
        return bumpiness;
     }

        private int getHoles(GameView game) {
            int holes = 0;
            for (int c = 0; c < Board.NUM_COLS; c++) {
                boolean found = false;
                for (int r = 0; r < Board.NUM_ROWS; r++) {
                    if (game.getBoard().isCoordinateOccupied(c, r)) {
                        found = true;
                    } else if (found) {
                        holes++;
                    }
                }
            }
            return holes;
        }


    private int getLinesCleared(GameView game, Mino potentialAction) {
        int linesCleared = 0;
        try {
            Matrix grayscale = game.getGrayscaleImage(potentialAction);
            for (int r = 0; r < grayscale.getShape().getNumRows(); r++) {
                int occupiedForLineCount = 0;
                boolean isRowFull = true;
                for (int c = 0; c < grayscale.getShape().getNumCols(); c++) {
                    if (grayscale.get(r, c) == 0.0) {
                        isRowFull = false;
                        break;
                    }
                    else {
                        occupiedForLineCount++;
                    }
    
                }
                if (isRowFull && occupiedForLineCount == Board.NUM_COLS) {
                    linesCleared++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linesCleared; 
    }


@Override
public Matrix getQFunctionInput(final GameView game, final Mino potentialAction) {
    Matrix features = Matrix.zeros(1, 5); 
    try {
        features.set(0, 0, getAggregateHeight(game));
        features.set(0, 1, getHoles(game));
        features.set(0, 2, getBumpiness(game));
        features.set(0, 3, getLinesCleared(game, potentialAction));
        features.set(0, 4, getEmptyCells(game) - getHoles(game));
    } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    return features;
}




    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game,
                                 final GameCounter gameCounter)
    {
        long totalActions = gameCounter.getTotalGamesPlayed() * gameCounter.getCycleLength() + gameCounter.getCurrentMoveIdx();
        double explorationProb = Math.min(1.0, EXPLORATION_PROB / (1.0 + totalActions / gameCounter.getNumTrainingGames()));
        return this.getRandom().nextDouble() <= explorationProb;
    }



    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */


    private double getProbForTSpin(GameView game, Mino mino) {
        double prob = 0;
        if (mino.getType() == Mino.MinoType.T) {
            if (isTSpinPossible(game, mino)) {
                prob = 1;
            }
            
        }
        return prob;
    }

    private boolean isTSpinPossible(GameView game, Mino mino) {
        Matrix grayscale = null;
        Boolean isTSpinPossible = false;
        try {
            grayscale = game.getGrayscaleImage(mino);
            int pivotRow = mino.getPivotBlockCoordinate().getXCoordinate();
            int pivotCol = mino.getPivotBlockCoordinate().getYCoordinate();
            int[] rowOffsets = {0, 0, 1, -1};
            int[] colOffsets = {1, -1, 0, 0};
            int numOccupied = 0;
            for (int i = 0; i < 4; i++) {
                int newRow = pivotRow + rowOffsets[i];
                int newCol = pivotCol + colOffsets[i];
                if (newRow >= 0 && newRow < Board.NUM_ROWS-2 && newCol >= 0 && newCol < Board.NUM_COLS) {
                    if (grayscale.get(newRow, newCol) == 1.0) {
                        numOccupied++;
                    }
                }
            }
            if (numOccupied >= 3) {
                isTSpinPossible = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return isTSpinPossible;

    }
    public Mino getExplorationMove(final GameView game) {
        /*
        int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        List<Mino> finalPositions = game.getFinalMinoPositions();
        Map<Mino, Integer> minoOccur = new HashMap<>();
        int occurThreshold = 10;
        Mino mino = finalPositions.get(randIdx);
        Integer occurrences = minoOccur.get(mino);
        while (occurrences != null && occurrences > occurThreshold) {
            randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
            mino = finalPositions.get(randIdx);
            occurrences = minoOccur.get(mino);
        }
        minoOccur.put(mino, occurrences != null ? occurrences + 1 : 1);       


               double epsilon = 0.1;
       double rand = this.getRandom().nextDouble();
       Mino mino = null;
       if (rand < epsilon) {
           int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
           mino = game.getFinalMinoPositions().get(randIdx);
       } else {
           mino = game.getFinalMinoPositions().get(0);
           double maxQValue = Double.NEGATIVE_INFINITY;
           for (Mino potentialAction : game.getFinalMinoPositions()) {
               Matrix qFunctionInput = getQFunctionInput(game, potentialAction);
               Matrix qValue;
            try {
                qValue = this.getQFunction().forward(qFunctionInput);
                if (qValue.get(0, 0) > maxQValue) {
                    maxQValue = qValue.get(0, 0);
                    mino = potentialAction;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           }
       }        
        return mino;


 int randI = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        return game.getFinalMinoPositions().get(randI);
         */
        double epsilon = 0.1;
        double rand = this.getRandom().nextDouble();
        Mino mino = null;
        if (rand < epsilon) {
            int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
            mino = game.getFinalMinoPositions().get(randIdx);
        } else {
            mino = game.getFinalMinoPositions().get(0);
            double maxQValue = Double.NEGATIVE_INFINITY;
            for (Mino potentialAction : game.getFinalMinoPositions()) {
                Matrix qFunctionInput = getQFunctionInput(game, potentialAction);
                Matrix qValue;
             try {
                 qValue = this.getQFunction().forward(qFunctionInput);
                 if (qValue.get(0, 0) > maxQValue) {
                     maxQValue = qValue.get(0, 0);
                     mino = potentialAction;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
            }
        }        
         return mino;
       
    }
    
    

    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a phase, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
                               LossFunction lossFunction,
                               Optimizer optimizer,
                               long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                                                  lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */

    @Override
    public double getReward(final GameView game)
    {
        double score = game.getScoreThisTurn();
        
        int totalHeight = 200;
        int totalHoles = 190;
        int totalBumpiness = 180;


        double weightHeight = 0.0001;
        double weightHoles = 0.0003;
        double weightBumpiness = 0.0015;


        double normalizedHeight = getAggregateHeight(game) / totalHeight;
        double normalizedHoles = getHoles(game) / totalHoles;
        double normalizedBumpiness = getBumpiness(game) / totalBumpiness;


        double reward = score;
        reward -= normalizedHeight * weightHeight;
        reward -= normalizedHoles * weightHoles;
        reward -= normalizedBumpiness * weightBumpiness; 

        if (game.didAgentLose()) {
            reward -= 1.5;
        }
        return reward;
    }

}
