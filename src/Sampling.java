/**
 * This class allows to execute the sampling algorithm introduced in Section 5 of the paper
 * "Mining Sequential Patterns with VC-Dimension and Rademacher Complexity" in a SPMF format dataset
 * and with parameters provided in input by the user.
 */
public class Sampling {
    public static void main(String[] args) {
        String dataset = "data/"+args[0];
        String outputFile = "data/sampling/"+args[0].split("\\.txt")[0];
        String sampleFile = outputFile + "_sample.txt";
        double theta = Double.parseDouble(args[1]);
        double epsilon = Double.parseDouble(args[2]);
        double delta = Double.parseDouble(args[3]);
        boolean FPF = Boolean.parseBoolean(args[4]);
        long timeSampleSize;
        long timeSampleCreation;
        long timeMiningSample;
        long start = System.currentTimeMillis();
        int[] sizes = Algorithms.computeSampleSize(dataset, epsilon, delta);
        timeSampleSize = System.currentTimeMillis() - start;
        int datasetSize = sizes[0];
        int sampleSize = sizes[1];
        if (sampleSize >= datasetSize) {
            System.out.println("ERROR: sample size larger than dataset size!");
            return;
        }
        double newTheta;
        if (FPF) {
            newTheta = theta + epsilon / 2.;
            outputFile += "_FPF_Approx.txt";
        } else {
            newTheta = theta - epsilon / 2.;
            if (newTheta <= 0) {
                System.out.println("ERROR: sample frequency threshold smaller than 0!");
                return;
            }
            outputFile += "_Approx.txt";
        }
        System.out.println("Input Dataset: " + dataset);
        System.out.println("Dataset Size |D|: " + datasetSize);
        System.out.println("Theta: " + theta);
        System.out.println("Epsilon: " + epsilon);
        System.out.println("Delta: " + delta);
        System.out.println("FPF: " + FPF);
        System.out.println("Sample Size |S|: " + sampleSize);
        System.out.println("|S|/|D|: " + sampleSize / (datasetSize * 1.));
        System.out.println("Sample Theta: " + newTheta);
        System.out.println("Sample File: " + sampleFile);
        System.out.println("Output File: " + outputFile);
        start = System.currentTimeMillis();
        Algorithms.createSample(dataset, datasetSize, sampleFile, sampleSize, System.currentTimeMillis());
        timeSampleCreation = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        int numFSP = Algorithms.mining(sampleFile, outputFile, newTheta);
        timeMiningSample = System.currentTimeMillis() - start;
        System.out.println("FSP Founds: " + numFSP);
        System.out.println("Time Sample Size Computation: " + timeSampleSize + "ms");
        System.out.println("Time Sample Creation: " + timeSampleCreation + "ms");
        System.out.println("Time Mining Sample: " + timeMiningSample + "ms");
    }
}