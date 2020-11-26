package polyrun.examples;

import polyrun.PolytopeRunner;
import polyrun.constraints.Constraint;
import polyrun.constraints.ConstraintsSystem;
import polyrun.constraints.SimpleConstraint;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.NCubedThinningFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ElectreTRIrCExample {

    public static double EPSILON = 1e-5;

    public static void main(String[] args) throws Exception {
        // This example contains calculation of stochastic results for ELECTRE TRI rC presented
        // https://doi.org/10.1016/j.enbuild.2018.05.032

        final int numberOfCriteria = 7;

        // Table 4 from the manuscript
        double[][] alternatives = new double[][]{
                new double[]{12096.012, 0.0012312, 5.5728, 246240.24, 2951.50, 3657.79, 65.67},
                new double[]{15746.557, 0.0006328, 5.6160, 139537.39, 2670.14, 4392.80, 59.41},
                new double[]{797.040, 0.0000283, 0.4168, 22680.02, 2980.71, 3750.48, 66.32},
                new double[]{3650.403, 0.0003348, 4.0392, 81216.08, 2843.18, 3974.48, 63.26},
                new double[]{1531.441, 0.0000697, 2.3328, 23328.02, 3001.83, 3659.44, 66.79},
                new double[]{2332.802, 0.0003974, 1.5465, 73008.07, 2984.31, 3681.89, 66.40},
                new double[]{855.360, 0.0000861, 0.9050, 12074.41, 2975.77, 3685.51, 66.21},
                new double[]{630.720, 0.0000743, 0.3456, 10821.61, 2908.35, 3921.44, 64.71},
                new double[]{1995.841, 0.0004881, 0.9374, 33264.03, 2853.07, 3893.64, 60.76},
                new double[]{2354.402, 0.0000012, 2.7432, 65880.06, 3093.07, 3357.30, 68.82},
                new double[]{788.400, 0.0000956, 1.2787, 12182.41, 2730.82, 4478.60, 63.48},
                new double[]{311.040, 0.0000347, 0.4039, 6177.60, 2954.19, 3800.44, 65.73}
        };

        boolean[] criteriaDirections = new boolean[]{ // true means maximization
                false, false, false, false, true, false, true
        };

        // Table 5 from the manuscript
        double[][] characteristicProfiles = new double[][]{
                new double[]{12096.012, 0.0003348, 4.0392, 139537.39, 2730.82, 4392.80, 60.76},
                new double[]{2332.802, 0.0000956, 1.2787, 33264.03, 2853.07, 3800.44, 63.48},
                new double[]{630.720, 0.0000283, 0.4039, 10821.61, 2980.71, 3659.44, 66.21}
        };

        // Table 6 in the manuscript
        List<List<Integer>> criteriaOrder = Arrays.asList( // from the least important
                Arrays.asList(0, 5), // l = 1
                Arrays.asList(4, 6), // l = 2
                Arrays.asList(3), // l = 3
                Arrays.asList(1), // l = 4
                Arrays.asList(2) // l = 5
        );
        List<Integer> preferenceIntensity = Arrays.asList(
                0, 1, 0, 1 // i-th value represents preference intensity between i and i+1 group of criteria from *criteriaOrder*
        );

        // Constants from the manuscript
        final int Z = 8;
        final double lambda = 0.7415;

        // Mapping criterion index -> weight index
        int[] weightIndex = getWeightIndicesForCriteria(numberOfCriteria, criteriaOrder);

        // Build constraints
        ConstraintsSystem constraints = buildConstraintsSystem(criteriaOrder, preferenceIntensity, Z);

        // Sample
        double[][] samples = sample(constraints, 1);

        // Calculate stochastic assignments
        double[][] assignments = calculateAssignments(
                alternatives,
                characteristicProfiles,
                criteriaDirections,
                samples,
                weightIndex,
                lambda);

        // Print assignments
        System.out.println("Table of assignments [in %]:");
        for (int i = 0; i < assignments.length; i++) {
            System.out.print("a" + (i + 1) + "\t");

            for (int j = 0; j < assignments[i].length; j++) {
                System.out.print(align(6, "" + BigDecimal.valueOf(100.0 * assignments[i][j])
                        .setScale(1, RoundingMode.HALF_UP)));
            }

            System.out.println();
        }
    }

    private static double[][] calculateAssignments(double[][] alternatives,
                                                   double[][] characteristicProfiles,
                                                   boolean[] criteriaDirections,
                                                   double[][] samples,
                                                   int[] weightIndex,
                                                   double lambda) {
        double[][] assignments = new double[alternatives.length][characteristicProfiles.length];

        for (double[] sample : samples) {
            for (int j = 0; j < alternatives.length; j++) {
                Integer bestClass = null;
                Integer worstClass = null;

                double[] alternativeOutranksProfileConcordanceIndex = new double[characteristicProfiles.length];
                double[] profileOutranksAlternativeConcordanceIndex = new double[characteristicProfiles.length];

                for (int k = 0; k < characteristicProfiles.length; k++) {
                    alternativeOutranksProfileConcordanceIndex[k] = comprehensiveConcordanceIndex(
                            alternatives[j],
                            characteristicProfiles[k],
                            criteriaDirections,
                            sample,
                            weightIndex);
                    profileOutranksAlternativeConcordanceIndex[k] = comprehensiveConcordanceIndex(
                            characteristicProfiles[k],
                            alternatives[j],
                            criteriaDirections,
                            sample,
                            weightIndex);
                }

                for (int k = 0; k < characteristicProfiles.length; k++) {
                    if (k > 0
                            && alternativeOutranksProfileConcordanceIndex[k - 1] >= lambda
                            && profileOutranksAlternativeConcordanceIndex[k - 1] < lambda
                            && alternativeOutranksProfileConcordanceIndex[k] > profileOutranksAlternativeConcordanceIndex[k - 1]) {
                        worstClass = k;
                    }

                    if (bestClass == null
                            && k < characteristicProfiles.length - 1
                            && profileOutranksAlternativeConcordanceIndex[k + 1] >= lambda
                            && alternativeOutranksProfileConcordanceIndex[k + 1] < lambda
                            && profileOutranksAlternativeConcordanceIndex[k] > alternativeOutranksProfileConcordanceIndex[k + 1]) {
                        bestClass = k;
                    }
                }

                if (worstClass == null) {
                    worstClass = 0;
                }

                if (bestClass == null) {
                    bestClass = characteristicProfiles.length - 1;
                }

                for (int k = worstClass; k <= bestClass; k++) {
                    assignments[j][k] += 1.0;
                }
            }

        }

        for (int i = 0; i < alternatives.length; i++) {
            for (int j = 0; j < characteristicProfiles.length; j++) {
                assignments[i][j] = assignments[i][j] / (double) samples.length;
            }
        }

        return assignments;
    }

    private static double[][] sample(ConstraintsSystem constraints, int seed) throws Exception {
        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(constraints);

        // Setup Chebyshev center as start point
        runner.setAnyStartPoint();

        // Generate 1000 samples
        return runner.chain(
                new HitAndRun(new Random(seed)), // seed is set for reproducible results
                new NCubedThinningFunction(1.0),
                1000);
    }

    private static int[] getWeightIndicesForCriteria(int numberOfCriteria, List<List<Integer>> criteriaOrder) {
        int[] weightIndex = new int[numberOfCriteria];
        for (int i = 0; i < criteriaOrder.size(); i++) {
            for (int j = 0; j < criteriaOrder.get(i).size(); j++) {
                weightIndex[criteriaOrder.get(i).get(j)] = i;
            }
        }
        return weightIndex;
    }

    private static ConstraintsSystem buildConstraintsSystem(List<List<Integer>> criteriaOrder, List<Integer> preferenceIntensity, int bestWorstRatio) {
        if (bestWorstRatio < 2) {
            throw new IllegalArgumentException("bestWorstRatio");
        }

        final int numberOfWeights = criteriaOrder.size();

        // Build constraints on the weight space
        List<Constraint> constraintsList = new ArrayList<Constraint>();

        // Normalization of weights
        double[] normalizationLhs = new double[numberOfWeights];
        for (int i = 0; i < numberOfWeights; i++) {
            normalizationLhs[i] = criteriaOrder.get(i).size();
        }
        constraintsList.add(new SimpleConstraint(normalizationLhs, "=", 1.0));

        // Criteria order
        for (int i = 0; i < numberOfWeights - 1; i++) {
            double[] lhs = new double[numberOfWeights];
            lhs[i] = 1.0;
            lhs[i + 1] = -1.0;
            constraintsList.add(new SimpleConstraint(lhs, "<=", -EPSILON));
        }

        // Intensities of preferences
        for (int i = 0; i < preferenceIntensity.size(); i++) {
            for (int j = 0; j < preferenceIntensity.size(); j++) {
                if (i != j && preferenceIntensity.get(i) > preferenceIntensity.get(j)) {
                    // preference intensity between i-th criteria group and (i+1)-th
                    // is stronger than between j-th and (j+1)-th group:
                    // w_(i+1) - w_(i) > w_(j+1) - w_(j)
                    // w_(i+1) - w_(i) - w_(j+1) + w_(j) > 0
                    // w_(i+1) - w_(i) - w_(j+1) + w_(j) >= epsilon

                    double[] lhs = new double[numberOfWeights];
                    lhs[i + 1] = 1.0;
                    lhs[i] = -1.0;
                    lhs[j + 1] += -1.0;
                    lhs[j] += 1.0;

                    constraintsList.add(new SimpleConstraint(lhs, ">=", EPSILON));
                }
            }
        }

        // Fixed ratio (given as parameter *z*) between the highest and the lowest weights
        double[] fixedRatioLhs = new double[numberOfWeights];
        fixedRatioLhs[0] = bestWorstRatio; // the lowest weight
        fixedRatioLhs[numberOfWeights - 1] = -1.0; // the highest weight
        constraintsList.add(new SimpleConstraint(fixedRatioLhs, "=", 0.0));

        return new ConstraintsSystem(constraintsList);
    }

    private static double comprehensiveConcordanceIndex(double[] alternative1,
                                                        double[] alternative2,
                                                        boolean[] maximization,
                                                        double[] weights,
                                                        int[] weightIndex) {
        double value = 0.0;

        for (int i = 0; i < weightIndex.length; i++) {
            value += weights[weightIndex[i]] * marginalConcordanceIndex(alternative1[i], alternative2[i], maximization[i]);
        }

        return value;
    }

    private static double marginalConcordanceIndex(double v1, double v2, boolean maximization) {
        if (maximization) {
            return v1 >= v2 ? 1.0 : 0.0;
        } else {
            return v1 <= v2 ? 1.0 : 0.0;
        }
    }

    private static String align(int length, String text) {
        return String.format("%" + length + "s", text);
    }
}
