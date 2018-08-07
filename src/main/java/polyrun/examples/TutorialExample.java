package polyrun.examples;

import polyrun.PolytopeRunner;
import polyrun.SampleConsumer;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampling.HitAndRun;
import polyrun.sampling.OutOfBoundsBehaviour;
import polyrun.sampling.SphereWalk;
import polyrun.thinning.NCubedThinningFunction;

public class TutorialExample {

    public static void main(String[] args) throws Exception {
        // Define sampling space
        final double[][] lhs = new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1},
                {1, 1, 1},
                {3, 0.5, -0.75}
        };

        final String[] dir = new String[]{">=", ">=", ">=", "=", ">="};
        final double[] rhs = new double[]{0, 0, 0, 1, 0};
        ConstraintsSystem constraints = new ConstraintsSystem(lhs, dir, rhs);

        // Initialize polytope runner
        PolytopeRunner runner = new PolytopeRunner(constraints);

        // Setup start point (by slack maximization)
        runner.setAnyStartPoint();

        // Generate 1000 samples
        double[][] samples = runner.chain(new HitAndRun(),
                new NCubedThinningFunction(1.0),
                1000);

        // Set current point to [0.3, 0.1, 0.6]
        runner.setStartPoint(new double[]{0.3, 0.1, 0.6});

        // Generate 20 samples from the neighborhood of point [0.3, 0.1, 0.6]
        double[][] neighborhood = runner.neighborhood(new SphereWalk(0.15, OutOfBoundsBehaviour.Crop), 20);

        // Print the samples to standard output
        System.out.println("Samples from neighborhood of [0.3, 0.1, 0.6]:");
        final DoubleFormatter formatter = new DoubleFormatter();
        for (double[] sample : neighborhood) {
            System.out.println(formatter.format(sample));
        }

        // Generate 1000 samples from the polytope and print them to standard output
        System.out.println("Samples uniformly picked from the polytope:");
        runner.chain(new HitAndRun(),
                new NCubedThinningFunction(1.0),
                1000,
                new SampleConsumer() {
                    public void consume(double[] sample) {
                        //print sample
                        System.out.println(formatter.format(sample));
                    }
                });
    }
}
