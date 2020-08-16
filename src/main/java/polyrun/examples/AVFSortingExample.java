package polyrun.examples;


import polyrun.PolytopeRunner;
import polyrun.SampleConsumer;
import polyrun.constraints.Constraint;
import polyrun.constraints.ConstraintsSystem;
import polyrun.constraints.SimpleConstraint;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.MNThinningFunction;

import java.util.*;

public class AVFSortingExample {

    public static void main(String[] args) throws Exception {
        // The example considers consistency preferential construct CAE_1 defined in Section 3.1
        // in the paper https://doi.org/10.1016/j.ejor.2016.10.019.
        //
        // This data set consists of 48 reference alternatives with known class assignment and 5 alternatives that
        // should be examined (assigned).

        final double[][] alternatives = new double[][]{
                //reference alternatives
                new double[]{3, 2, 3, 0, 45, 80, 3, 1},
                new double[]{3, 3, 3, 0, 60, 100, 3, 1},
                new double[]{3, 3, 3, 0, 72000, 40, 2, 1},
                new double[]{3, 5, 3, 0, 45, 41, 5, 1},
                new double[]{3, 5, 3, 0, 60, 47, 5, 1},
                new double[]{3, 5, 3, 0, 30, 39, 5, 1},
                new double[]{3, 5, 3, 0, 30, 42, 5, 1},
                new double[]{3, 3, 3, 0, 10, 150, 4, 1},
                new double[]{1, 1, 3, 0, 1800, 25, 6, 1},
                new double[]{3, 1, 3, 0, 900, 25, 1, 3},
                new double[]{3, 1, 3, 0, 900, 25, 1, 1},
                new double[]{1, 1, 1, 0, 15300, 25, 6, 1},
                new double[]{3, 5, 3, 0, 28800, 25, 7, 2},
                new double[]{3, 5, 3, 0, 28800, 25, 7, 1},
                new double[]{3, 5, 3, 0, 7200, 25, 7, 1},
                new double[]{4, 5, 3, 0, 7200, 25, 7, 1},
                new double[]{3, 5, 3, 1, 28800, 37, 8, 1},
                new double[]{3, 5, 3, 1, 7200, 27, 8, 1},
                new double[]{3, 5, 3, 1, 480, 30, 1, 1},
                new double[]{3, 5, 3, 1, 21600, 30, 8, 2},
                new double[]{3, 5, 1, 0, 86400, 25, 8, 1},
                new double[]{1, 1, 1, 0, 10800, 170, 4, 1},
                new double[]{1, 2, 3, 0, 180, 198, 4, 2},
                new double[]{1, 2, 3, 0, 5, 100, 4, 1},
                new double[]{1, 2, 3, 0, 7200, 90, 2, 1},
                new double[]{1, 2, 1, 0, 14400, 160, 2, 3},
                new double[]{1, 2, 1, 0, 14400, 160, 2, 1},
                new double[]{3, 5, 3, 0, 1800, 80, 2, 2},
                new double[]{1, 5, 3, 0, 480, 100, 4, 1},
                new double[]{3, 3, 3, 0, 7200, 70, 2, 1},
                new double[]{3, 5, 3, 0, 28800, 70, 2, 1},
                new double[]{1, 1, 1, 0, 60, 100, 3, 1},
                new double[]{4, 5, 3, 0, 4500, 25, 8, 3},
                new double[]{4, 5, 3, 0, 2700, 60, 2, 1},
                new double[]{3, 5, 3, 0, 10800, 160, 2, 1},
                new double[]{4, 5, 3, 0, 600, 40, 2, 1},
                new double[]{3, 5, 3, 0, 600, 40, 2, 1},
                new double[]{3, 5, 3, 1, 900, 80, 2, 1},
                new double[]{3, 5, 3, 0, 600, 100, 4, 2},
                new double[]{3, 5, 3, 1, 1200, 100, 2, 1},
                new double[]{3, 5, 3, 0, 28800, 25, 6, 2},
                new double[]{4, 5, 3, 0, 60, 55, 5, 1},
                new double[]{3, 5, 3, 1, 600, 40, 2, 1},
                new double[]{3, 5, 3, 1, 1200, 80, 2, 3},
                new double[]{3, 5, 3, 1, 900, 95, 2, 1},
                new double[]{3, 5, 3, 1, 600, 25, 7, 1},
                new double[]{1, 1, 3, 0, 60, 100, 4, 1},
                new double[]{3, 1, 3, 0, 86400, 25, 8, 1},

                // alternatives to assign
                new double[]{3, 5, 3, 0, 55, 42, 5, 1},
                new double[]{3, 2, 1, 1, 2600, 85, 2, 2},
                new double[]{2, 2, 3, 0, 600, 90, 2, 3},
                new double[]{3, 3, 3, 0, 70, 65, 5, 1},
                new double[]{1, 1, 1, 0, 480, 100, 3, 1}
        };

        // Remember indices of alternatives to assign
        final int[] alternativesToAssign = new int[]{48, 49, 50, 51, 52};

        // Criteria directions (true - max, false - min)
        boolean[] criteria = new boolean[]{true, true, true, true, false, false, true, false};

        // Number of classes
        final int numberOfClasses = 5; // 1 - worst, 5 - best

        // Assignments (for all reference alternatives but 33-rd, see Section 3.1 in the paper)
        // Each row contains (1-based) index of alternative and (1-based) index of its desired class
        int[][] assignments = new int[][]{
                new int[]{1, 3},
                new int[]{2, 4},
                new int[]{3, 4},
                new int[]{4, 5},
                new int[]{5, 5},
                new int[]{6, 5},
                new int[]{7, 5},
                new int[]{8, 5},
                new int[]{9, 2},
                new int[]{10, 2},
                new int[]{11, 2},
                new int[]{12, 1},
                new int[]{13, 5},
                new int[]{14, 5},
                new int[]{15, 5},
                new int[]{16, 5},
                new int[]{17, 5},
                new int[]{18, 5},
                new int[]{19, 5},
                new int[]{20, 5},
                new int[]{21, 2},
                new int[]{22, 1},
                new int[]{23, 2},
                new int[]{24, 2},
                new int[]{25, 2},
                new int[]{26, 2},
                new int[]{27, 2},
                new int[]{28, 4},
                new int[]{29, 2},
                new int[]{30, 3},
                new int[]{31, 3},
                new int[]{32, 2},
                new int[]{34, 3},
                new int[]{35, 3},
                new int[]{36, 4},
                new int[]{37, 4},
                new int[]{38, 4},
                new int[]{39, 4},
                new int[]{40, 4},
                new int[]{41, 3},
                new int[]{42, 4},
                new int[]{43, 4},
                new int[]{44, 4},
                new int[]{45, 4},
                new int[]{46, 5},
                new int[]{47, 2},
                new int[]{48, 5}
        };

        // Small positive value
        double epsilon = 1e-4;

        // Map criterion index -> (Map criterion value -> )
        Map<Integer, SortedMap<Double, List<Integer>>> criterionMapping = new HashMap<Integer, SortedMap<Double, List<Integer>>>();
        int numberOfVariables = 0;
        for (int i = 0; i < criteria.length; i++) {
            criterionMapping.put(i, new TreeMap<Double, List<Integer>>());

            for (int j = 0; j < alternatives.length; j++) {
                Double value = alternatives[j][i];
                if (criterionMapping.get(i).containsKey(value)) {
                    criterionMapping.get(i).get(value).add(j);
                } else {
                    criterionMapping.get(i).put(value, new ArrayList<Integer>(Collections.singletonList(j)));
                }
            }

            numberOfVariables += criterionMapping.get(i).size() - 1;
        }

        // Add thresholds between classes
        numberOfVariables += numberOfClasses - 1;

        // Create a list of constraints
        List<Constraint> constraints = new ArrayList<Constraint>();

        // Add constraints for monotonicity and normalization
        int currentFirstIndex = 0;
        int[] bestValuesIndices = new int[criteria.length];
        double[] lhs;

        for (int i = 0; i < criteria.length; i++) {
            for (int j = 0; j < criterionMapping.get(i).size() - 1; j++) {
                lhs = new double[numberOfVariables];

                if (j > 0) {
                    lhs[currentFirstIndex + j - 1] = -1;
                }

                lhs[currentFirstIndex + j] = 1;

                constraints.add(new SimpleConstraint(lhs, ">=", epsilon));
            }

            currentFirstIndex += criterionMapping.get(i).size() - 1;
            bestValuesIndices[i] = currentFirstIndex - 1;
        }

        lhs = new double[numberOfVariables];
        for (int i : bestValuesIndices) {
            lhs[i] = 1.0;
        }
        constraints.add(new SimpleConstraint(lhs, "=", 1.0));

        // Add constraints to provide monotonicity of thresholds
        for (int i = 0; i < numberOfClasses - 1; i++) {
            lhs = new double[numberOfVariables];

            if (i > 0) {
                lhs[currentFirstIndex + i - 1] = -1;
            }

            lhs[currentFirstIndex + i] = 1;
            constraints.add(new SimpleConstraint(lhs, ">=", epsilon));
        }

        lhs = new double[numberOfVariables];
        lhs[numberOfVariables - 1] = 1.0;
        constraints.add(new SimpleConstraint(lhs, "<=", 1.0 - epsilon));

        // Build mapping alternative -> (criterion -> variable index)
        final Integer[][] valuesIndices = new Integer[alternatives.length][criteria.length];
        int currentIndex = 0;
        for (int j = 0; j < criteria.length; j++) {
            SortedMap<Double, List<Integer>> mapping = criterionMapping.get(j);

            List<Double> orderedKeys = new ArrayList<Double>(mapping.keySet());
            if (!criteria[j]) {
                Collections.reverse(orderedKeys);
            }

            for (int k = 1; k < orderedKeys.size(); k++) { // k = 1, because the worst level is skipped (marginal value = 0 by definition)
                for (int i : mapping.get(orderedKeys.get(k))) {
                    valuesIndices[i][j] = currentIndex;
                }

                currentIndex++;
            }

        }

        // Add all assignments of reference alternatives as constraints to the model
        for (int[] assignment : assignments) {
            int alternative = assignment[0] - 1;
            int desiredClass = assignment[1] - 1;

            if (desiredClass > 0) {
                lhs = new double[numberOfVariables];

                for (Integer variableIndex : valuesIndices[alternative]) {
                    if (variableIndex != null) {
                        lhs[variableIndex] = 1.0;
                    }
                }

                lhs[lhs.length - numberOfClasses + desiredClass] = -1.0;

                constraints.add(new SimpleConstraint(lhs, ">=", 0));
            }

            if (desiredClass < numberOfClasses - 1) {
                lhs = new double[numberOfVariables];

                for (Integer variableIndex : valuesIndices[alternative]) {
                    if (variableIndex != null) {
                        lhs[variableIndex] = 1.0;
                    }
                }

                lhs[lhs.length - numberOfClasses + desiredClass + 1] = -1.0;

                constraints.add(new SimpleConstraint(lhs, "<=", -epsilon));
            }
        }

        // Prepare storage for class indices
        final double[][] classIndices = new double[alternativesToAssign.length][numberOfClasses];

        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(new ConstraintsSystem(constraints));

        // Setup Chebyshev center as a start point
        runner.setAnyStartPoint();

        // Generate 100 samples and calculate
        final int numberOfSamples = 100;
        runner.chain(
                new HitAndRun(new Random(1)), // seed is set for reproducible results
                new MNThinningFunction(0.5),
                numberOfSamples,
                new SampleConsumer() {
                    public void consume(double[] sample) {
                        for (int i = 0; i < alternativesToAssign.length; i++) {
                            double value = 0.0;

                            for (Integer variableIndex : valuesIndices[alternativesToAssign[i]]) {
                                if (variableIndex != null) {
                                    value += sample[variableIndex];
                                }
                            }

                            int assignment = 0;

                            for (int t = sample.length - numberOfClasses + 1; t < sample.length; t++) {
                                if (value > sample[t]) {
                                    assignment++;
                                }
                            }

                            classIndices[i][assignment] += 1.0 / numberOfSamples;
                        }
                    }
                });

        // Print header
        System.out.print("Stochastic assignments for non-reference alternatives\nclass\t");
        for (int j = 1; j <= numberOfClasses; j++) {
            System.out.print(j + "\t");
        }
        System.out.println();

        // Print rank acceptability indices (see part CAE_1 of Table 8 in the paper)
        DoubleFormatter formatter = new DoubleFormatter();
        for (int i = 0; i < alternativesToAssign.length; i++) {
            System.out.print("a_" + (alternativesToAssign[i] + 1) + "\t");

            for (int j = 0; j < numberOfClasses; j++) {
                System.out.print(formatter.format(classIndices[i][j]) + "\t");
            }

            System.out.println();
        }
    }
}
