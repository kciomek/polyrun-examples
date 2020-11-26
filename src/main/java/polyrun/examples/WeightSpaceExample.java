package polyrun.examples;

import polyrun.PolytopeRunner;
import polyrun.constraints.Constraint;
import polyrun.constraints.ConstraintsSystem;
import polyrun.constraints.SimpleConstraint;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.NCubedThinningFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightSpaceExample {

    public static void main(String[] args) throws Exception {
        // This example contains calculation of pairwise wining indices and rank acceptability indices
        // when weight space is defined a priori and known marginal value functions. The case is presented
        // in https://doi.org/10.1016/j.dss.2017.10.010

        // Define actions
        String[] actions = new String[]{
                "GREENWAY", "RAIL-BANKING", "TRANSPORT", "OLD STATION", "NO ACTION"
        };

        double[] dm1Weights = new double[]{0.11666667, 0.13333333, 0.08333333, 0.16666667, 0.16000000, 0.02000000, 0.08000000, 0.20000000, 0.04000000};
        double[] dm2Weights = new double[]{0.1724138, 0.0862069, 0.1379310, 0.1034483, 0.0937500, 0.0468750, 0.1562500, 0.1250000, 0.0781250};

        // Criteria values (Table 1 in the paper)
//        Object[][] criteriaValues = new Object[][]{
//                new Integer[] { 165000, 0, 0, 40000, 0 },
//                new String[] { "Good", "Very good", "Very good", "Low", "Very good" },
//                new Integer[] { 12, 1, 1, 5, 0 },
//                new String[] { "Very positive", "Irrelevant", "Irrelevant", "Very positive", "Negative" },
//                new Integer[] { 830000, 170000, 170000, 240000, 0 },
//                new Integer[] { 4, 0, 3, 5, 0 },
//                new String[] { "High", "None", "Medium", "High", "None" },
//                new Integer[] { 75000, 0, 249200, 19400, 0 },
//                new Integer[] { 78, 0, 33, 32, 0 },
//        };

        // Marginal values of the actions (Table 2 in the paper)
        double[][] marginalValues = new double[][]{
                new double[]{1, 0.6, 0, 1, 0, 0.8, 1, 0.340909090909091, 1},
                new double[]{0, 1, 0.916666666666667, 0.2, 0.590361445783133, 0, 0, 0, 0},
                new double[]{0, 1, 0.916666666666667, 0.2, 0.590361445783133, 0.6, 0.7, 1, 0.608333333333333},
                new double[]{0.3125, 0, 0.5, 1, 0.460843373493976, 1, 1, 0.0881818181818182, 0.6},
                new double[]{0, 1, 1, 0, 1, 0, 0, 0, 0}
        };

        int numberOfActions = actions.length;
        int numberOfCriteria = marginalValues[0].length;

        // Collect constraints on the weight space
        List<Constraint> constraintsList = new ArrayList<Constraint>();

        // Normalization - sum_{i = 1,...,numberOfCriteria} w_i = 1
        constraintsList.add(new SimpleConstraint(
                ones(numberOfCriteria), "=", 1.0
        ));

        // Boundaries
        for (int i = 0; i < numberOfCriteria; i++) {
            double[] lhs = new double[numberOfCriteria];
            lhs[i] = 1.0;

            constraintsList.add(new SimpleConstraint(lhs, ">=", Math.min(dm1Weights[i], dm2Weights[i])));
            constraintsList.add(new SimpleConstraint(lhs, "<=", Math.max(dm1Weights[i], dm2Weights[i])));
        }

        // Define weight space
        ConstraintsSystem constraints = new ConstraintsSystem(constraintsList);

        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(constraints);

        // Setup Chebyshev center as start point
        runner.setAnyStartPoint();

        // Generate 1000 samples
        final int numberOfSamples = 1000;
        double[][] sampledWeights = runner.chain(
                new HitAndRun(new Random(11)), // seed is set for reproducible results
                new NCubedThinningFunction(1.0),
                numberOfSamples);

        // Calculate winning indices
        double[][] pairwiseWiningIndex = new double[numberOfActions][numberOfActions];
        double[][] rankAcceptabilityIndex = new double[numberOfActions][numberOfActions];

        for (double[] weights : sampledWeights) {
            double[] comprehensiveValues = new double[numberOfActions];

            // Calculate comprehensive values for every action
            for (int i = 0; i < numberOfActions; i++) {
                for (int j = 0; j < numberOfCriteria; j++) {
                    comprehensiveValues[i] += weights[j] * marginalValues[i][j];
                }
            }

            for (int i = 0; i < numberOfActions; i++) {
                int rank = 0;

                for (int j =  0; j < numberOfActions; j++) {
                    if (i == j)
                        continue;

                    if (comprehensiveValues[i] < comprehensiveValues[j]) {
                        pairwiseWiningIndex[j][i] += 1.0;
                        rank++;
                    }
                }

                rankAcceptabilityIndex[i][rank] += 1.0;
            }
        }

        for (int i = 0; i < numberOfActions; i++) {
            for (int j = 0; j < numberOfActions; j++) {
                pairwiseWiningIndex[i][j] = pairwiseWiningIndex[i][j] / (double) numberOfSamples;
                rankAcceptabilityIndex[i][j] = rankAcceptabilityIndex[i][j] / (double) numberOfSamples;
            }
        }

        // Find the longest action name
        int headerAlignment = 0;
        for (int i = 0; i < numberOfActions; i++) {
            headerAlignment = Math.max(headerAlignment, actions[i].length());
        }

        // Print header
        System.out.print("Table of pairwise wining indices:\n" + align(headerAlignment, "") + " ");
        for (int j = 0; j < numberOfActions; j++) {
            System.out.print(align(headerAlignment, actions[j]) + " ");
        }
        System.out.println();

        // Print rank acceptability indices
        for (int i = 0; i < numberOfActions; i++) {
            System.out.print(align(headerAlignment, actions[i]) + " ");

            for (int j = 0; j < numberOfActions; j++) {
                System.out.print(align(headerAlignment, "" + BigDecimal.valueOf(pairwiseWiningIndex[i][j])
                        .setScale(2, RoundingMode.HALF_UP)) + " ");
            }

            System.out.println();
        }

        // Print header
        System.out.print("\nTable of rank acceptability indices:\n" + align(headerAlignment, "rank") + "\t");
        for (int j = 1; j <= numberOfActions; j++) {
            System.out.print(j + "\t\t");
        }
        System.out.println();

        // Print rank acceptability indices
        for (int i = 0; i < numberOfActions; i++) {
            System.out.print(align(headerAlignment, actions[i]) + "\t");

            for (int j = 0; j < numberOfActions; j++) {
                System.out.print(BigDecimal.valueOf(rankAcceptabilityIndex[i][j])
                        .setScale(2, RoundingMode.HALF_UP) + "\t");
            }

            System.out.println();
        }
    }

    private static double[] ones(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("number");
        }

        double[] vector = new double[number];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = 1.0;
        }

        return vector;
    }

    private static String align(int length, String text) {
        return String.format("%" + length + "s", text);
    }
}
