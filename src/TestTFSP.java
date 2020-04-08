import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * This class contains the code developed to perform the experimental valuation of the TFSP algorithms.
 * The results are shown in Section 7.4 of the paper.
 * For the Rademacher complexity, it uses the precomputed upper bounds on the maximum deviation that are stored in two files
 * in the data folder.
 */
public class TestTFSP {

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df1 = new DecimalFormat("#.####");

    /**
     * Creates a pseudo-artificial dataset from a real dataset. It extracts a random sample from a real dataset
     * that has the same size of the real one.
     *
     * @param dataset    the name of the dataset
     * @param sampleFile the name of the output file that will contain the pseudo-artificial dataset
     * @param seed       the seed of the random generator used to extract the sample (fixed for reproducibility)
     */
    public static void createDataset(String dataset, String sampleFile, int seed) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> dat = new ArrayList<>();
        try {
            FileInputStream fin = new FileInputStream(new File(dataset));
            br = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = br.readLine()) != null) {
                dat.add(line);
            }
            br.close();
            int datasetSize = dat.size();
            FileOutputStream fout = new FileOutputStream(new File((sampleFile)));
            bw = new BufferedWriter(new OutputStreamWriter(fout));
            Random r = new Random(seed);
            for (int i = 0; i < datasetSize; i++) {
                int j = r.nextInt(datasetSize);
                bw.write(dat.get(j) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null && bw != null) {
                try {
                    br.close();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Computes the percentage of times the FSPs mined from the 4 pseudo-artificial datasets contain false
     * positives and miss some TFSP from the corresponding ground truth
     *
     * @param dataset the name of the dataset
     * @param j       the index of the theta considered
     */
    static void getPercentageFPFN(String dataset, int j) {
        BufferedReader br = null;
        HashSet<String> gt = new HashSet<>();
        try {
            String gtFSP = "data/TFSP/samples/minedFiles/" + dataset + "_GT" + (j + 1) + ".txt";
            FileInputStream fin = new FileInputStream(new File(gtFSP));
            br = new BufferedReader(new InputStreamReader(fin));
            String line = br.readLine();
            while (line != null) {
                gt.add(line.split(" #SUP: ")[0]);
                line = br.readLine();
            }
            br.close();
            int totFp = 0;
            int totFn = 0;
            for (int k = 0; k < 4; k++) {
                HashSet<String> fsp = new HashSet<>();
                int fp = 0;
                int fn = 0;
                String sampleFSP = "data/TFSP/samples/minedFiles/" + dataset + "_S" + (k + 1) + "_THETA" + (j + 1) + ".txt";
                fin = new FileInputStream(new File(sampleFSP));
                br = new BufferedReader(new InputStreamReader(fin));
                line = br.readLine();
                while (line != null) {
                    fsp.add(line.split(" #SUP: ")[0]);
                    line = br.readLine();
                }
                br.close();

                for (String s : gt) {
                    if (!fsp.contains(s)) fn++;
                }
                for (String s : fsp) {
                    if (!gt.contains(s)) fp++;
                }
                if (fn > 0) totFn++;
                if (fp > 0) totFp++;
            }
            System.out.print("TFSP: " + gt.size() + " - ");
            System.out.print("Times FPs: " + (totFp / 4. * 100) + "% - ");
            System.out.println("Times FNs: " + (totFn / 4. * 100) + "%");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Computes the percentage of times the TFSPs mined from the 4 pseudo-artificial datasets contain false
     * positives and the average fraction TFSP from the samples/TFSP from the ground truth,
     * that is the fraction of TFSPs of the ground truth reported in output
     *
     * @param dataset the name of the dataset
     * @param j       the index of the theta considered
     * @param vc      true if the maxDev has been computed with the VC-dimension
     */
    static void getPercentageFP(String dataset, int j, boolean vc) {
        BufferedReader br = null;
        HashSet<String> gt = new HashSet<>();
        try {
            FileInputStream fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_GT" + (j + 1) + ".txt"));
            br = new BufferedReader(new InputStreamReader(fin));
            String line = br.readLine();
            while (line != null) {
                gt.add(line.split(" #SUP: ")[0]);
                line = br.readLine();
            }
            br.close();
            int totFp = 0;
            double percTFSP = 0.;
            for (int k = 0; k < 4; k++) {
                HashSet<String> fsp = new HashSet<>();
                int fp = 0;
                if (vc)
                    fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_S" + (k + 1) + "_P" + (j + 1) + "_VC.txt"));
                else
                    fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_S" + (k + 1) + "_P" + (j + 1) + "_RAD.txt"));
                br = new BufferedReader(new InputStreamReader(fin));
                line = br.readLine();
                while (line != null) {
                    fsp.add(line.split(" #SUP: ")[0]);
                    line = br.readLine();
                }
                br.close();
                fin.close();
                for (String s : fsp) {
                    if (!gt.contains(s)) fp++;
                }
                if (fp > 0) totFp++;
                percTFSP += (fsp.size() - fp) / (gt.size() * 1.);

            }
            if (vc) {
                System.out.print("TFSP: " + gt.size());
                System.out.print(" - Times FPs VC: " + (totFp / 4. * 100.) + "%");
                System.out.print(" - FSP/TFSP VC: " + df2.format(percTFSP / 4.));
            } else {
                System.out.print(" - Times FPs RAD: " + (totFp / 4. * 100.) + "%");
                System.out.print(" - FSP/TFSP RAD: " + df2.format(percTFSP / 4.));
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Computes the percentage of times the TFSPs mined from the 4 pseudo-artificial datasets miss some TFSPs from the
     * the ground truth and the average fraction TFSP from the ground truth/TFSP from the samples,
     * that is the fraction of FSPs in output the are TFSPs in the ground truth
     *
     * @param dataset the name of the dataset
     * @param j       the index of the theta considered
     * @param vc      true if the maxDev has been computed with the VC-dimension
     */
    static void getPercentageALL(String dataset, int j, boolean vc) {
        BufferedReader br = null;
        HashSet<String> gt = new HashSet<>();
        try {

            FileInputStream fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_GT" + (j + 1) + ".txt"));
            br = new BufferedReader(new InputStreamReader(fin));
            String line = br.readLine();
            while (line != null) {
                gt.add(line.split(" #SUP: ")[0]);
                line = br.readLine();
            }
            br.close();
            int totALL = 0;
            double percFP = 0.;
            for (int k = 0; k < 4; k++) {
                HashSet<String> fsp = new HashSet<>();
                int fn = 0;
                if (vc)
                    fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_S" + (k + 1) + "_M" + (j + 1) + "_VC.txt"));
                else
                    fin = new FileInputStream(new File("data/TFSP/samples/minedFiles/" + dataset + "_S" + (k + 1) + "_M" + (j + 1) + "_RAD.txt"));
                br = new BufferedReader(new InputStreamReader(fin));
                line = br.readLine();
                while (line != null) {
                    fsp.add(line.split(" #SUP: ")[0]);
                    line = br.readLine();
                }
                br.close();
                fin.close();
                for (String s : gt) {
                    if (!fsp.contains(s)) fn++;
                }
                if (fn == 0) totALL++;
                percFP += ((gt.size() - fn) / (fsp.size() * 1.));
            }

            if (vc) {
                System.out.print("TFSP: " + gt.size());
                System.out.print(" - Times FNs VC: " + ((4 - totALL) / 4. * 100.) + "%");
                System.out.print(" - TFSP/FSP VC: " + df2.format(percFP / 4.));
            } else {
                System.out.print(" - Times FNs RAD: " + ((4 - totALL) / 4. * 100.) + "%");
                System.out.print(" - TFSP/FSP RAD: " + df2.format(percFP / 4.));
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        String[] datasets = {"BIBLE", "BMS1", "BMS2", "KOSARAK", "LEVIATHAN", "MSNBC"};
        double[] maxDevVC = new double[4 * datasets.length];
        double[] maxDevRad = new double[4 * datasets.length];
        double[] maxDevRadBound = new double[4 * datasets.length];
        BufferedReader br = null;
        try {
            // Read the upper bound on the maximum deviation computed using an approximation of the Rademacher complexity
            FileReader fr = new FileReader("data/TFSP/radeApprox.txt");
            br = new BufferedReader(fr);
            String line;
            int h = 0;
            while ((line = br.readLine()) != null) {
                maxDevRad[h++] = Double.parseDouble(line);
            }
            br.close();
            fr.close();
            // Read the upper bound on the maximum deviation computed using an upper bound on the Rademacher complexity
            fr = new FileReader("data/TFSP/radeBound.txt");
            br = new BufferedReader(fr);
            h = 0;
            while ((line = br.readLine()) != null) {
                maxDevRadBound[h++] = Double.parseDouble(line);
            }
            br.close();
            fr.close();
            double[] thetas = {0.1, 0.05, 0.025, 0.0225, 0.025, 0.0225, 0.06, 0.04, 0.15, 0.1, 0.02, 0.015};
            double delta = 0.1;
            int[] s = {0, 2, 3, 4};
            df1.setRoundingMode(RoundingMode.HALF_UP);
            df2.setRoundingMode(RoundingMode.HALF_UP);
            // Generate 4 pseudo-artificial datasets for each real dataset fixing the seed for the random generator
            for (int i = 0; i < datasets.length; i++) {
                String dataset = "data/" + datasets[i] + ".txt";
                for (int k = 0; k < 4; k++) {
                    String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                    createDataset(dataset, sample, s[k]);
                }
            }
            // Table 3 of the paper
            System.out.println("TABLE 3:");
            for (int i = 0; i < datasets.length; i++) {
                System.out.println(datasets[i] + ": ");
                String dataset = "data/" + datasets[i] + ".txt";
                for (int j = 0; j < 2; j++) {
                    double theta = thetas[2 * i + j];
                    String gtFSP = "data/TFSP/samples/minedFiles/" + datasets[i] + "_GT" + (j + 1) + ".txt";
                    System.out.print("Theta: " + theta + " - ");
                    Algorithms.mining(dataset, gtFSP, theta);
                    for (int k = 0; k < 4; k++) {
                        String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                        String sampleFSP = "data/TFSP/samples/minedFiles/" + datasets[i] + "_S" + (k + 1) + "_THETA" + (j + 1) + ".txt";
                        Algorithms.mining(sample, sampleFSP, theta);

                    }
                    getPercentageFPFN(datasets[i], j);
                }
            }
            // Table 4 of the paper
            System.out.println("*************************");
            System.out.println("TABLE 4:");
            for (int i = 0; i < datasets.length; i++) {
                System.out.println(datasets[i] + ": ");
                double avgVC = 0;
                double avgRAD = 0;
                double avgRADBound = 0;
                double maxVC = 0;
                double maxRAD = 0;
                double maxRADBound = 0;
                double stdVC = 0;
                double stdRAD = 0;
                double stdRADBound = 0;
                for (int k = 0; k < 4; k++) {
                    String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                    maxDevVC[i * 4 + k] = Algorithms.computeMaxDevVC(sample, delta);
                    if (maxDevVC[i * 4 + k] > maxVC) maxVC = maxDevVC[i * 4 + k];
                    avgVC += maxDevVC[i * 4 + k];
                    if (maxDevRad[i * 4 + k] > maxRAD) maxRAD = maxDevRad[i * 4 + k];
                    avgRAD += maxDevRad[i * 4 + k];
                    if (maxDevRadBound[i * 4 + k] > maxRADBound) maxRADBound = maxDevRadBound[i * 4 + k];
                    avgRADBound += maxDevRadBound[i * 4 + k];
                }
                avgVC /= 4.;
                avgRAD /= 4.;
                avgRADBound /= 4.;
                for (int k = 0; k < 4; k++) {
                    stdVC += Math.pow(maxDevVC[i * 4 + k] - avgVC, 2.);
                    stdRAD += Math.pow(maxDevRad[i * 4 + k] - avgRAD, 2.);
                    stdRADBound += Math.pow(maxDevRadBound[i * 4 + k] - avgRADBound, 2.);
                }
                stdVC /= 4.;
                stdRAD /= 4.;
                stdRADBound /= 4.;
                stdVC = Math.sqrt(stdVC);
                stdRAD = Math.sqrt(stdRAD);
                stdRADBound = Math.sqrt(stdRADBound);
                System.out.print("avgVC: " + df1.format(avgVC) + " - maxVC: " + df1.format(maxVC) + " - stdVC: " + df1.format(stdVC));
                System.out.print(" - avgRADBound: " + df1.format(avgRADBound) + " - maxRADBound: " + df1.format(maxRADBound) + " - stdRADBound: " + df1.format(stdRADBound));
                System.out.println(" - avgRAD: " + df1.format(avgRAD) + " - maxRAD: " + df1.format(maxRAD) + " - stdRAD: " + df1.format(stdRAD));
            }
            //Table 5 of the paper
            System.out.println("*************************");
            System.out.println("TABLE 5:");
            for (int i = 0; i < datasets.length; i++) {
                for (int j = 0; j < 2; j++) {
                    double theta = thetas[2 * i + j];
                    System.out.println(datasets[i] + " (theta: " + theta + ")");
                    for (int k = 0; k < 4; k++) {

                        String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                        String sampleFSPVC = "data/TFSP/samples/minedFiles/" + datasets[i] + "_S" + (k + 1) + "_P" + (j + 1) + "_VC.txt";
                        String sampleFSPRAD = "data/TFSP/samples/minedFiles/" + datasets[i] + "_S" + (k + 1) + "_P" + (j + 1) + "_RAD.txt";
                        Algorithms.mining(sample, sampleFSPVC, theta + maxDevVC[i * 4 + k]);
                        Algorithms.mining(sample, sampleFSPRAD, theta + maxDevRad[i * 4 + k]);

                    }
                    getPercentageFP(datasets[i], j, true); //VC
                    getPercentageFP(datasets[i], j, false); //RAD
                }
            }
            //Table 6 of the paper
            System.out.println("*************************");
            System.out.println("TABLE 6:");
            for (int i = 0; i < datasets.length; i++) {
                for (int j = 0; j < 2; j++) {
                    double theta = thetas[2 * i + j];
                    System.out.println(datasets[i] + " (theta: " + theta + ")");
                    for (int k = 0; k < 4; k++) {
                        String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                        String sampleFSPVC = "data/TFSP/samples/minedFiles/" + datasets[i] + "_S" + (k + 1) + "_M" + (j + 1) + "_VC.txt";
                        String sampleFSPRAD = "data/TFSP/samples/minedFiles/" + datasets[i] + "_S" + (k + 1) + "_M" + (j + 1) + "_RAD.txt";
                        Algorithms.mining(sample, sampleFSPVC, theta - maxDevVC[i * 4 + k]);
                        Algorithms.mining(sample, sampleFSPRAD, theta - maxDevRad[i * 4 + k]);
                    }
                    getPercentageALL(datasets[i], j, true);
                    getPercentageALL(datasets[i], j, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}