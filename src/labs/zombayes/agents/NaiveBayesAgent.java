package src.labs.zombayes.agents;


// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.labs.zombayes.agents.SurvivalAgent;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.ElementWiseOperator;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;



public class NaiveBayesAgent extends SurvivalAgent {

    public static class NaiveBayes extends Object {

        public static final FeatureType[] FEATURE_HEADER = {FeatureType.CONTINUOUS,
                                                             FeatureType.CONTINUOUS,
                                                             FeatureType.DISCRETE,
                                                             FeatureType.DISCRETE};

        private Map<Integer, Map<Integer, Double>> classFeatureMean;
        private Map<Integer, Map<Integer, Double>> classFeatureVariance;
        private Map<Integer, Double> classPriorProbabilities;

        public NaiveBayes() {
            classFeatureMean = new HashMap<>();
            classFeatureVariance = new HashMap<>();
            classPriorProbabilities = new HashMap<>();
        }

        private void normalize(Matrix X) {
            for (int i = 0; i < X.getShape().getNumCols(); i++) {
                double mean = 0.0;
                double max = Double.MIN_VALUE;
                double min = Double.MAX_VALUE;

                for (int j = 0; j < X.getShape().getNumRows(); j++) {
                    double value = X.get(j, i);
                    mean += value;
                    max = Math.max(max, value);
                    min = Math.min(min, value);
                }
                mean /= X.getShape().getNumRows();

                for (int j = 0; j < X.getShape().getNumRows(); j++) {
                    double value = X.get(j, i);
                    X.set(j, i, (value - mean) / (max - min));
                }
            }
        }

        public void fit(Matrix X, Matrix y_gt) {
            int numClasses = y_gt.getShape().getNumCols();
            int numFeatures = X.getShape().getNumCols();

            normalize(X);


            for (int i = 0; i < numClasses; i++) {
                int count = 0;
                for (int j = 0; j < y_gt.getShape().getNumRows(); j++) {
                    if (y_gt.get(j, i) == 1) {
                        count++;
                    }
                }
                classPriorProbabilities.put(i, (double) count / y_gt.getShape().getNumRows());
            }

            for (int i = 0; i < numClasses; i++) {
                Map<Integer, Double> featureMean = new HashMap<>();
                Map<Integer, Double> featureVariance = new HashMap<>();
                for (int j = 0; j < numFeatures; j++) {
                    double mean = 0.0;
                    double variance = 0.0;
                    int count = 0;
                    for (int k = 0; k < X.getShape().getNumRows(); k++) {
                        if (y_gt.get(k, i) == 1.0) {
                            mean += X.get(k, j);
                            count++;
                        }
                    }
                    if (count > 0) {
                        mean /= count;
                        for (int k = 0; k < X.getShape().getNumRows(); k++) {
                            if (y_gt.get(k, i) == 1.0) {
                                variance += Math.pow(X.get(k, j) - mean, 2);
                            }
                        }
                        variance /= count;
                    }
                    featureMean.put(j, mean);
                    featureVariance.put(j, variance == 0.0 ? 1.0 : variance);
                }
                classFeatureMean.put(i, featureMean);
                classFeatureVariance.put(i, featureVariance);
            }
        }

        public int predict(Matrix x) {
            double best_prob = Double.NEGATIVE_INFINITY;
            int best_class = -1;
            for (int i = 0; i < classPriorProbabilities.size(); i++) {
                double prob = Math.log(classPriorProbabilities.get(i));
                for (int j = 0; j < x.getShape().getNumCols(); j++) {
                    double mean = classFeatureMean.get(i).get(j);
                    double variance = classFeatureVariance.get(i).get(j);
                    double likelihood = -Math.log(Math.sqrt(2 * Math.PI * variance)) - Math.pow(x.get(0, j) - mean, 2) / (2 * variance);
                    prob += likelihood;
                }
                if (prob > best_prob) {
                    best_prob = prob;
                    best_class = i;
                }
            }
            return best_class;
        }

    }
    
    private NaiveBayes model;

    public NaiveBayesAgent(int playerNum, String[] args)
    {
        super(playerNum, args);
        this.model = new NaiveBayes();
    }

    public NaiveBayes getModel() { return this.model; }

    @Override
    public void train(Matrix X, Matrix y_gt)
    {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getModel().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector)
    {
        return this.getModel().predict(featureRowVector);
    }

}
