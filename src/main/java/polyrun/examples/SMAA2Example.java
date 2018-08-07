package polyrun.examples;

import polyrun.PolytopeRunner;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.NCubedThinningFunction;

import java.util.Random;

public class SMAA2Example {

    public static void main(String[] args) throws Exception {
        // This example is based on data set presented in paper https://doi.org/10.1287/opre.49.3.444.11220 (see Section 5)

        // Define alternatives
        String[] alternatives = new String[]{
                "IIA1", "IIA2", "IIA3", "IIA4", "IIB1",
                "IIB2", "IIB3", "IIB4", "IIC1", "IIC2",
                "IIC3", "IIC4", "ZERO"
        };

        // Criteria values (Table 1 in hte paper)
        double[][] criteriaValues = new double[][]{
                new double[]{4, 1, 985, 30, 166, 705, 25000, 4.5, 4.2, 15.1, 1.75},
                new double[]{4, 2.5, 985, 30, 166, 765, 25000, 4.5, 4.1, 15.3, 1.69},
                new double[]{4, 1.5, 985, 30, 166, 705, 25000, 4.5, 4.3, 12.7, 1.75},
                new double[]{4, 1.5, 985, 30, 166, 705, 25000, 4.5, 4.3, 12.2, 1.65},
                new double[]{4, 1.5, 985, 35, 177, 705, 25000, 4.5, 4.4, 15.1, 1.68},
                new double[]{4, 2.5, 985, 35, 177, 765, 25000, 4.5, 4.3, 15.3, 1.62},
                new double[]{4, 2, 985, 35, 177, 705, 25000, 4.5, 4.5, 12.7, 1.68},
                new double[]{4, 2, 985, 35, 177, 705, 25000, 4.5, 4.5, 12.2, 1.58},
                new double[]{4, 1, 985, 35, 166, 705, 25000, 4.5, 4.6, 14.8, 1.72},
                new double[]{4, 2.5, 985, 35, 166, 765, 25000, 4.5, 4.5, 15.0, 1.66},
                new double[]{4, 1.5, 985, 35, 166, 705, 25000, 4.5, 4.7, 12.4, 1.72},
                new double[]{4, 2, 985, 35, 166, 705, 25000, 4.5, 4.7, 11.9, 1.62},
                new double[]{1, 0, 1300, 50, 266, 4200, 0, 2, 1, 18.8, 1}
        };

        // Criteria directions (true - max, false - min)
        boolean[] criteriaDirections = new boolean[]{false, false, false, true, true, false, true, true, true, false, true};

        int numberOfAlternatives = alternatives.length;
        int numberOfCriteria = criteriaDirections.length;

        // Define weight space
        double[][] lhs = new double[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // w1 >=0
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // w2 >=0
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, // w3 >=0
                {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, // w4 >=0
                {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, // w5 >=0
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, // w6 >=0
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, // w7 >=0
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, // w8 >=0
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, // w9 >=0
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, // w0 >=0
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // w11 >=0
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // sum_{i = 1,...,11} w_i = 1
        };
        String[] dir = new String[]{">=", ">=", ">=", ">=", ">=", ">=", ">=", ">=", ">=", ">=", ">=", "="};
        double[] rhs = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        ConstraintsSystem constraints = new ConstraintsSystem(lhs, dir, rhs);

        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(constraints);

        // Setup Chebyshev center as start point
        runner.setAnyStartPoint();

        // Generate 1000 samples
        double[][] sampledWeights = runner.chain(
                new HitAndRun(new Random(1)), // seed is set for reproducible results
                new NCubedThinningFunction(1.0),
                1000);

        // Find the minimum and maximum values per criterion
        double[] minimums = new double[numberOfCriteria];
        double[] maximums = new double[numberOfCriteria];
        System.arraycopy(criteriaValues[0], 0, minimums, 0, numberOfCriteria);
        System.arraycopy(criteriaValues[0], 0, maximums, 0, numberOfCriteria);
        for (int i = 1; i < numberOfAlternatives; i++) {
            for (int j = 0; j < numberOfCriteria; j++) {
                if (criteriaValues[i][j] < minimums[j]) {
                    minimums[j] = criteriaValues[i][j];
                }

                if (criteriaValues[i][j] > maximums[j]) {
                    maximums[j] = criteriaValues[i][j];
                }
            }
        }

        // Sample criteria values
        Random random = new Random(1); // seed is set for reproducible results
        double[][][] sampledCriteriaValues = new double[1000][numberOfAlternatives][numberOfCriteria];

        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < numberOfAlternatives; j++) {
                for (int k = 0; k < numberOfCriteria; k++) {
                    double interval = (maximums[k] - minimums[k]) / 10.0;
                    sampledCriteriaValues[i][j][k] = (criteriaValues[j][k] - interval) + random.nextDouble() * 2.0 * interval;
                }
            }
        }

        // Calculate acceptability indices
        double[][] rankAcceptabilityIndex = new double[numberOfAlternatives][numberOfAlternatives];
        for (int s = 0; s < sampledCriteriaValues.length; s++) {
            // find best and worst (for scaling)
            double[] worst = new double[numberOfCriteria];
            double[] best = new double[numberOfCriteria];
            System.arraycopy(sampledCriteriaValues[s][0], 0, worst, 0, numberOfCriteria);
            System.arraycopy(sampledCriteriaValues[s][0], 0, best, 0, numberOfCriteria);
            for (int i = 1; i < numberOfAlternatives; i++) {
                for (int k = 0; k < numberOfCriteria; k++) {
                    if (criteriaDirections[k]) {
                        if (sampledCriteriaValues[s][i][k] < worst[k]) {
                            worst[k] = sampledCriteriaValues[s][i][k];
                        }

                        if (sampledCriteriaValues[s][i][k] > best[k]) {
                            best[k] = sampledCriteriaValues[s][i][k];
                        }
                    } else {
                        if (sampledCriteriaValues[s][i][k] < best[k]) {
                            best[k] = sampledCriteriaValues[s][i][k];
                        }

                        if (sampledCriteriaValues[s][i][k] > worst[k]) {
                            worst[k] = sampledCriteriaValues[s][i][k];
                        }
                    }
                }
            }

            for (int j = 0; j < sampledWeights.length; j++) {
                double[] comprehensiveValues = new double[numberOfAlternatives];
                for (int i = 0; i < numberOfAlternatives; i++) {
                    comprehensiveValues[i] = u(sampledCriteriaValues[s][i], sampledWeights[j], worst, best);
                }

                for (int i = 0; i < numberOfAlternatives; i++) {
                    int rank = 1;

                    for (int k = 0; k < numberOfAlternatives; k++) {
                        if (i != k) {
                            if (comprehensiveValues[i] < comprehensiveValues[k]) {
                                rank++;
                            }
                        }
                    }

                    rankAcceptabilityIndex[i][rank - 1] += 1.0 / (double) (sampledWeights.length * sampledCriteriaValues.length);
                }
            }
        }

        // Print header
        System.out.print("Table of rank acceptability indices [in %]:\nrank\t");
        for (int j = 1; j <= numberOfAlternatives; j++) {
            System.out.print(j + "\t");
        }
        System.out.println();

        // Print rank acceptability indices
        for (int i = 0; i < numberOfAlternatives; i++) {
            System.out.print(alternatives[i] + "\t");

            for (int j = 0; j < numberOfAlternatives; j++) {
                System.out.print((int) (rankAcceptabilityIndex[i][j] * 100.0) + "\t");
            }

            System.out.println();
        }
    }

    private static double u(double[] criteriaValues, double[] weightsVector, double[] worst, double[] best) {
        double value = 0.0;

        for (int i = 0; i < criteriaValues.length; i++) {
            value += (criteriaValues[i] * weightsVector[i] - worst[i]) / (best[i] - worst[i]);
        }

        return value;
    }
}
