/**
 * This class allows to execute the TFSP algorithm introduced in Section 6 of the paper
 * "Mining Sequential Patterns with VC-Dimension and Rademacher Complexity" in a dataset
 * and with parameters provided in input by the user, using the maximum deviation computed
 * with the upper bound (s-bound) on the empirical VC-dimension
 */
public class TFSP {
    public static void main(String[] args) {
        String dataset = "data/"+args[0];
        String outputFile = "data/TFSP/"+args[0].split("\\.txt")[0];
        double theta = Double.parseDouble(args[1]);
        double delta = Double.parseDouble(args[2]);
        boolean FPF = Boolean.parseBoolean(args[3]);
        long timeMaxDev;
        long timeMiningDataset;
        long start = System.currentTimeMillis();
        double maxDev = Algorithms.computeMaxDevVC(dataset, delta);
        timeMaxDev = System.currentTimeMillis() - start;
        double newTheta;
        if (FPF) {
            newTheta = theta + maxDev;
            outputFile += "_TFSP_FPF_Approx.txt";
        } else {
            newTheta = theta - maxDev;
            if (newTheta <= 0) {
                System.out.println("ERROR: corrected frequency threshold smaller than 0!");
                return;
            }
            outputFile += "_TFSP_Approx.txt";
        }
        System.out.println("Input Dataset: " + dataset);
        System.out.println("Theta: " + theta);
        System.out.println("Delta: " + delta);
        System.out.println("FPF: " + FPF);
        System.out.println("Upper Bound on Max Dev: " + maxDev);
        System.out.println("Corrected Theta: " + newTheta);
        System.out.println("Output File: " + outputFile);
        start = System.currentTimeMillis();
        int numFSP = Algorithms.mining(dataset, outputFile, newTheta);
        timeMiningDataset = System.currentTimeMillis() - start;
        System.out.println("TFSP Founds: " + numFSP);
        System.out.println("Time Upper Bound on Maximum Deviation Computation: " + timeMaxDev + "ms");
        System.out.println("Time Mining Dataset: " + timeMiningDataset + "ms");
    }
}