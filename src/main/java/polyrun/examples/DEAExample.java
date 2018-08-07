package polyrun.examples;


import polyrun.PolytopeRunner;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.NCubedThinningFunction;

import java.text.DecimalFormat;
import java.util.Random;

public class DEAExample {

    public static void main(String[] args) throws Exception {
        // Example for data set from paper https://doi.org/10.1016/j.omega.2016.03.003

        String[] DMUName = new String[]{"WAW", "KRK", "KAT", "WRO", "POZ", "LCJ", "GDN", "SZZ", "BZG", "RZE", "IEG"};
        double[][] inputs = new double[][]{
                new double[]{10.5, 36, 129.4, 7.0}, // WAW
                new double[]{3.1, 19, 31.6, 7.9}, // KRK
                new double[]{3.6, 32, 57.6, 10.5}, // KAT
                new double[]{1.5, 12, 18.0, 3.0}, // WRO
                new double[]{1.5, 10, 24.0, 4.0}, // POZ
                new double[]{0.6, 12, 24.0, 3.9}, // LCJ
                new double[]{1.0, 15, 42.9, 2.5}, // GDN
                new double[]{0.7, 10, 25.7, 1.9}, // SZZ
                new double[]{0.3, 6, 3.4, 1.2}, // BZG
                new double[]{0.6, 6, 11.3, 2.7}, // RZE
                new double[]{0.1, 10, 63.4, 3.0} // IEG
        };

        double[][] outputs = new double[][]{
                new double[]{9.5, 129.7}, // WAW
                new double[]{2.9, 31.3}, // KRK
                new double[]{2.4, 21.1}, // KAT
                new double[]{1.5, 18.8}, // WRO
                new double[]{1.3, 16.2}, // POZ
                new double[]{0.3, 4.2}, // LCJ
                new double[]{2.0, 23.6}, // GDN
                new double[]{0.3, 4.2}, // SZZ
                new double[]{0.3, 4.2}, // BZG
                new double[]{0.3, 3.5}, // RZE
                new double[]{0.005, 0.61} // IEG
        };

        int numberOfDMUs = DMUName.length;
        int numberOfInputs = inputs[0].length;
        int numberOfOutputs = outputs[0].length;

        // Define basic weight space for u and v
        double[][] lhs = new double[][]{
                // basic weight space
                {1, 0, 0, 0, 0, 0}, // v1 >=0
                {0, 1, 0, 0, 0, 0}, // v2 >=0
                {0, 0, 1, 0, 0, 0}, // v3 >=0
                {0, 0, 0, 1, 0, 0}, // v4 >=0
                {0, 0, 0, 0, 1, 0}, // u1 >=0
                {0, 0, 0, 0, 0, 1}, // u2 >=0
                {1, 1, 1, 1, 0, 0}, // v1 + v2 + v3 + v4 = 1
                {0, 0, 0, 0, 1, 1}, // u1 + u2 = 1
        };

        String[] dir = new String[]{">=", ">=", ">=", ">=", ">=", ">=", "=", "="};
        double[] rhs = new double[]{0, 0, 0, 0, 0, 0, 1, 1};
        ConstraintsSystem constraints = new ConstraintsSystem(lhs, dir, rhs);

        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(constraints);

        // Setup Chebyshev center as start point
        runner.setAnyStartPoint();

        // Generate 10000 samples
        double[][] samples = runner.chain(
                new HitAndRun(new Random(0)),
                new NCubedThinningFunction(1.0),
                10000);

        int numberOfIntervals = 10;
        double intervalSize = 1.0 / (double) numberOfIntervals;

        int[][] acceptedSamplesPerInterval = new int[numberOfDMUs][numberOfIntervals];

        for (int i = 0; i < samples.length; i++) {
            double[] v = new double[numberOfInputs];
            double[] u = new double[numberOfOutputs];

            System.arraycopy(samples[i], 0, v, 0, numberOfInputs);
            System.arraycopy(samples[i], numberOfInputs, u, 0, numberOfOutputs);
            double[] efficiency = new double[numberOfDMUs];
            for (int j = 0; j < numberOfDMUs; j++) {
                efficiency[j] = calculateEfficiency(inputs[j], outputs[j], v, u);
            }

            double maximalEfficiency = max(efficiency);

            for (int j = 0; j < numberOfDMUs; j++) {
                acceptedSamplesPerInterval[j][efficiency[j] == 0.0 ? 0 : (int) Math.ceil(efficiency[j] / maximalEfficiency / intervalSize) - 1]++;
            }
        }

        DecimalFormat format = new DecimalFormat("#.###");

        for (int i = 0; i < numberOfDMUs; i++) {
            System.out.println("DMU: " + DMUName[i]);
            for (int j = 0; j < numberOfIntervals; j++) {
                System.out.println("    " + (j == 0 ? "[" : "(") + format.format(intervalSize * j) + ";" + format.format(intervalSize * (j + 1)) + "]: " +
                        format.format(acceptedSamplesPerInterval[i][j] / (double) samples.length));
            }
            System.out.println();
        }
    }

    private static double max(double[] array) {
        double max = array[0];

        for (int i = 1; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }

        return max;
    }

    private static double calculateEfficiency(double[] input, double[] output, double[] v, double[] u) {
        // E = (u^T  * output) / (v^T * input)

        double nom = 0.0;
        double denom = 0.0;

        for (int i = 0; i < output.length; i++) {
            nom += output[i] * u[i];
        }

        for (int i = 0; i < input.length; i++) {
            denom += input[i] * v[i];
        }

        return nom / denom;
    }
}
