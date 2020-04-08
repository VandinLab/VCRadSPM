import java.io.*;
import java.util.*;

/**
 * This class contains the code developed to perform the experimental valuation of the sampling algorithm.
 * The results are shown in Section 7.3 of the paper.
 */
public class TestSampling {

    /**
     * Creates an enlarged dataset from a real dataset. It replicates each transaction of the real dataset
     * a fixed number of times and it stores the new dataset in a file.
     *
     * @param fileIn     the name of the real dataset
     * @param fileOut    the name of the output file that will contain the new enlarged dataset
     * @param replic     the replication factor of the transactions
     */
    public static int replicate(String fileIn, String fileOut, int replic) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        ArrayList<String> dataset;
        ArrayList<Integer> newDataset = null;
        try {
            FileInputStream fin = new FileInputStream(new File(fileIn));
            br = new BufferedReader(new InputStreamReader(fin));
            dataset = new ArrayList<>();
            newDataset = new ArrayList<>();
            String line;
            int t = 0;
            while ((line = br.readLine()) != null) {
                dataset.add(line);
                for (int i = 0; i < replic; i++) newDataset.add(t);
                t++;
            }
            Random r = new Random(0);
            Collections.shuffle(newDataset, r);
            FileOutputStream fout = new FileOutputStream(new File((fileOut)));
            bw = new BufferedWriter(new OutputStreamWriter(fout));
            for (int i : newDataset) bw.write(dataset.get(i) + "\n");
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
        return newDataset.size();
    }

    /**
     * Computes the percentage of times the FSPs mined from the 5 enlarged datasets from the same
     * real dataset are a FPF eps-approximation and computes the maximum and average absolute error
     * between the frequencies of the sequential patterns in the samples and in the enlarged datasets.
     *
     * @param file          the name of the enlarged dataset
     * @param epsilon       the value of epsilon
     * @param datasetSize   the size of the enlarged dataset
     * @param sampleSize    the size of the sample
     * @param n             the number of enlarged datasets from the same real dataset (5)
     * @param theta         the minimum frequency threshold
     */
    public static void checkAppFPF(String file, double epsilon, int datasetSize, int sampleSize, int n, double theta) {
        BufferedReader br = null;
        HashMap<String, Double> the = new HashMap<>();
        HashMap<String, Double> samp;
        String sampleMinned = "data/sampling/" + file + "_SAMPLE_FSP_FPF_";
        String datasetMinned = "data/sampling/" + file + "_FSP.txt";
        try {
            FileInputStream fin = new FileInputStream(new File(datasetMinned));
            br = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" #SUP: ");
                double sup = Double.parseDouble(splitted[1]) / datasetSize;
                the.put(splitted[0], sup);
            }
            br.close();
            double maxTot = 0.;
            double avgMaxTot = 0.;
            int notApp = 0;
            for (int i = 0; i < n; i++) {
                samp = new HashMap<>();
                boolean isApprox = true;
                fin = new FileInputStream(new File(sampleMinned + i + ".txt"));
                br = new BufferedReader(new InputStreamReader(fin));
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" #SUP: ");
                    double sup = Double.parseDouble(splitted[1]) / sampleSize;
                    samp.put(splitted[0], sup);
                }
                br.close();
                double max = 0.;
                for (String seq : samp.keySet()) {
                    double curr;
                    if (!the.containsKey(seq) || (curr = Math.abs(samp.get(seq) - the.get(seq))) > epsilon / 2.) {
                        isApprox = false;
                        notApp++;
                        break;
                    }
                    if (max < curr) max = curr;
                }
                if (isApprox) {
                    for (String seq : the.keySet()) {
                        if (the.get(seq) > theta + epsilon) {
                            if (!samp.containsKey(seq)) {
                                notApp++;
                                break;
                            }
                        }
                    }
                }
                if (maxTot < max) maxTot = max;
                avgMaxTot += max;
            }
            System.out.println("FPF eps-app Probability: " + (100. * (n - notApp) / n));
            System.out.println("Max_Abs_Err: " + maxTot);
            System.out.println("Avg_Abs_Err: " + avgMaxTot / (n * 1.));
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
     * Computes the percentage of times the FSPs mined from the 5 enlarged datasets from the same
     * real dataset are an eps-approximation and computes the maximum and average absolute error
     * between the frequencies of the sequential patterns in the samples and in the enlarged datasets.
     *
     * @param file          the name of the enlarged dataset
     * @param epsilon       the value of epsilon
     * @param datasetSize   the size of the enlarged dataset
     * @param sampleSize    the size of the sample
     * @param n             the number of enlarged datasets from the same real dataset (5)
     */
    public static void checkApp(String file, double epsilon, int datasetSize, int sampleSize, int n) {
        BufferedReader br = null;
        HashMap<String, Double> eps = new HashMap<>();
        HashMap<String, Double> the = new HashMap<>();
        HashMap<String, Double> samp;
        String sampleMinned = "data/sampling/" + file + "_SAMPLE_FSP_";
        String datasetMinned = "data/sampling/" + file + "_FSP.txt";
        String datasetMinnedEps = "data/sampling/" + file + "_FSP_EPS.txt";
        try {
            FileInputStream fin = new FileInputStream(new File(datasetMinned));
            br = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" #SUP: ");
                double sup = Double.parseDouble(splitted[1]) / datasetSize;
                the.put(splitted[0], sup);
            }
            br.close();
            fin = new FileInputStream(new File(datasetMinnedEps));
            br = new BufferedReader(new InputStreamReader(fin));
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" #SUP: ");
                double sup = Double.parseDouble(splitted[1]) / datasetSize;
                eps.put(splitted[0], sup);
            }
            br.close();
            double maxTot = 0.;
            double avgMaxTot = 0.;
            int notApp = 0;
            for (int i = 0; i < n; i++) {
                samp = new HashMap<>();
                boolean isApprox = true;
                fin = new FileInputStream(new File(sampleMinned + i + ".txt"));
                br = new BufferedReader(new InputStreamReader(fin));
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" #SUP: ");
                    double sup = Double.parseDouble(splitted[1]) / sampleSize;
                    samp.put(splitted[0], sup);
                }
                br.close();
                double max = 0.;
                for (String seq : samp.keySet()) {
                    double curr;
                    if (!eps.containsKey(seq) || (curr = Math.abs(samp.get(seq) - eps.get(seq))) > epsilon / 2.) {
                        isApprox = false;
                        notApp++;
                        break;
                    }
                    if (max < curr) max = curr;
                }
                if (isApprox) {
                    for (String seq : the.keySet()) {
                        if (!samp.containsKey(seq)) {
                            notApp++;
                            break;
                        }
                    }
                }
                if (maxTot < max) maxTot = max;
                avgMaxTot += max;

            }
            System.out.println("eps-app Probability: " + (100. * (n - notApp) / n));
            System.out.println("Max_Abs_Err: " + maxTot);
            System.out.println("Avg_Abs_Err: " + avgMaxTot / (n * 1.));
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
        String[] datasets = {"BIBLE", "BMS1", "BMS2", "FIFA", "KOSARAK", "LEVIATHAN", "MSNBC", "SIGN"};
        int[] replication = {200, 100, 100, 200, 100, 1000, 10, 10000};
        double[] theta = {0.1, 0.012, 0.012, 0.25, 0.02, 0.15, 0.02, 0.4};
        for (int index = 0; index < datasets.length; index++) {
            String file = "data/" + datasets[index] + ".txt";
            int rep = replication[index];
            String datasetFile = "data/sampling/datasets/" + datasets[index] + "_" + rep + ".txt";
            String sampleFile = "data/sampling/" + datasets[index] + "_SAMPLE.txt";
            String sampleMinned = "data/sampling/" + datasets[index] + "_SAMPLE_FSP_";
            String sampleMinnedFPF = "data/sampling/" + datasets[index] + "_SAMPLE_FSP_FPF_";
            String datasetMinned = "data/sampling/" + datasets[index] + "_FSP.txt";
            String datasetMinnedEps = "data/sampling/" + datasets[index] + "_FSP_EPS.txt";
            double epsilon = 0.01;
            double delta = 0.1;
            int datasetSize = replicate(file, datasetFile, rep);
            long timeSampleSize = 0;
            long timeSampleCreation = 0;
            long timeMiningSampleFPF = 0;
            long timeMiningSample = 0;
            long timeMiningDataset = 0;
            long start;
            long end;
            int sampleSize = 0;
            int iteration = 5;
            System.out.println("Dataset: " + datasetFile);
            System.out.println("Theta: " + theta[index]);
            for (int i = 0; i < iteration; i++) {
                start = System.currentTimeMillis();
                sampleSize = Algorithms.computeSampleSize(datasetFile, epsilon, delta)[1];
                end = System.currentTimeMillis() - start;
                timeSampleSize += end;
                start = System.currentTimeMillis();
                Algorithms.createSample(datasetFile, datasetSize, sampleFile, sampleSize, i);
                end = System.currentTimeMillis() - start;
                timeSampleCreation += end;
                start = System.currentTimeMillis();
                Algorithms.mining(sampleFile, sampleMinned + i + ".txt", theta[index] - epsilon / 2);
                end = System.currentTimeMillis() - start;
                timeMiningSample += end;
                start = System.currentTimeMillis();
                Algorithms.mining(sampleFile, sampleMinnedFPF + i + ".txt", theta[index] + epsilon / 2);
                end = System.currentTimeMillis() - start;
                timeMiningSampleFPF += end;
                start = System.currentTimeMillis();
                Algorithms.mining(datasetFile, datasetMinned, theta[index]);
                end = System.currentTimeMillis() - start;
                timeMiningDataset += end;
                System.gc();
            }
            System.out.println("|S|/|D|:" + sampleSize/(datasetSize*1.));
            Algorithms.mining(file, datasetMinnedEps, theta[index] - epsilon);
            checkApp(datasets[index], epsilon, datasetSize, sampleSize, iteration);
            checkAppFPF(datasets[index], epsilon, datasetSize, sampleSize, iteration, theta[index]);
            System.out.println("AVG Time Sample Size Computation: " + timeSampleSize / (iteration*1.)+"ms");
            System.out.println("AVG Time Sample Creation: " + timeSampleCreation / (iteration*1.)+"ms");
            System.out.println("AVG Time Mining Sample eps-app: " + timeMiningSample / (iteration*1.)+"ms");
            System.out.println("AVG Time Mining Sample FPF eps-app: " + timeMiningSampleFPF / (iteration*1.)+"ms");
            System.out.println("AVG Time Mining Dataset: " + timeMiningDataset / (iteration*1.)+"ms");
        }
    }
}