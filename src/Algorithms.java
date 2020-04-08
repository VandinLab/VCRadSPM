import java.io.*;
import java.util.*;

/**
 * This class contains some of the algorithms used in the evaluation of the methods introduced in the
 * paper "Mining Sequential Patterns with VC-Dimension and Rademacher Complexity".
 */
public class Algorithms {

    /**
     * Private class that represents a pair composed by a string that is a sequential pattern,
     * and an integer that is the sequential pattern's item-length.
     */
    private static class PairSP implements Comparable<PairSP> {
        private String sp;
        private int length;

        PairSP(String sp, int length) {
            this.sp = sp;
            this.length = length;
        }

        int getLength() {
            return length;
        }

        String getSequence() {
            return sp;
        }

        public int compareTo(PairSP p2) {
            return length - p2.getLength();
        }
    }

    /**
     * Computes the item-length of a sequential pattern
     *
     * @param sp a string that represents the sequential pattern
     * @return the item-length of sp
     */
    private static int computeItemLength(String sp) {
        int length = 0;
        String[] splitLine = sp.split(" -1 ");
        for (int i = 0; i < splitLine.length - 1; i++) {
            String[] splitItemset = splitLine[i].split(" ");
            length += splitItemset.length;
        }
        return length;
    }

    /**
     * Computes an upper bound to the maximum deviation for the true frequent sequential patterns algorithm using the VC-dimension
     *
     * @param dataset the file of the dataset
     * @param delta   the confidence parameter
     * @return an upper bound to the maximum deviation
     */
    public static double computeMaxDevVC(String dataset, double delta) {
        // compute an upper bound to the SBound and the size of the dataset D
        int sBound = 0;
        ArrayList<PairSP> orderedSet;
        HashSet<String> set;
        BufferedReader br = null;
        double maxDev = -1;
        try {
            FileInputStream fin = new FileInputStream(new File(dataset));
            br = new BufferedReader(new InputStreamReader(fin));
            orderedSet = new ArrayList<>();
            set = new HashSet<>();
            String line;
            int datasetSize = 0;
            while ((line = br.readLine()) != null) {
                datasetSize++;
                if (!set.contains(line)) {
                    int length = computeItemLength(line);
                    if (length > sBound) {
                        set.add(line);
                        int i = 0;
                        while (i < orderedSet.size() && orderedSet.get(i).getLength() > length) i++;
                        orderedSet.add(i, new PairSP(line, length));
                        if (orderedSet.get(orderedSet.size() - 1).getLength() > sBound) sBound++;
                        else {
                            String removable = orderedSet.remove(orderedSet.size() - 1).getSequence();
                            set.remove(removable);
                        }
                    }
                }
            }
            // compute the upper bound to the maximum deviation using the SBound
            maxDev = Math.sqrt(1 / (2. * datasetSize) * (sBound + Math.log(1 / delta)));
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
        return maxDev;
    }

    /**
     * Computes the sample size for the sampling algorithm using the VC-dimension and the size of the input dataset
     *
     * @param dataset the file of the dataset
     * @param eps     the error parameter
     * @param delta   the confidence parameter
     * @return an array out[] of two inttegers, where out[0] is the dataset size and out[1] is the sample size
     */
    public static int[] computeSampleSize(String dataset, double eps, double delta) {
        // compute an upper bound to the SBound
        int sBound = 0;
        int sampleSize = 0;
        int datasetSize = 0;
        ArrayList<PairSP> orderedSet;
        HashSet<String> set;
        BufferedReader br = null;
        try {
            FileInputStream fin = new FileInputStream(new File(dataset));
            br = new BufferedReader(new InputStreamReader(fin));
            orderedSet = new ArrayList<>();
            set = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                datasetSize++;
                if (!set.contains(line)) {
                    int length = computeItemLength(line);
                    if (length > sBound) {
                        set.add(line);
                        int i = 0;
                        while (i < orderedSet.size() && orderedSet.get(i).getLength() > length) i++;
                        orderedSet.add(i, new PairSP(line, length));
                        if (orderedSet.get(orderedSet.size() - 1).getLength() > sBound) sBound++;
                        else {
                            String removable = orderedSet.remove(orderedSet.size() - 1).getSequence();
                            set.remove(removable);
                        }
                    }
                }
            }
            // compute the sample size
            sampleSize = (int) Math.ceil(2. / Math.pow(eps, 2.) * (sBound + Math.log(1. / delta)));
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
        return new int[]{datasetSize,sampleSize};
    }

    /**
     * Create a random sample from the input dataset
     *
     * @param datasetFile   the file of the dataset
     * @param datasetSize   the size of the input dataset
     * @param sampleFile    the file to save the sample
     * @param sampleSize    the size of the sample
     * @param seed          the seed for the random generator
     */
    public static void createSample(String datasetFile, int datasetSize, String sampleFile, int sampleSize, long seed) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        int[] sample;
        try {
            FileInputStream fin = new FileInputStream(new File(datasetFile));
            br = new BufferedReader(new InputStreamReader(fin));
            FileOutputStream fout = new FileOutputStream(new File((sampleFile)));
            bw = new BufferedWriter(new OutputStreamWriter(fout));
            sample = new int[sampleSize];
            Random r = new Random(seed);
            for (int i = 0; i < sampleSize; i++) sample[i] = r.nextInt(datasetSize);
            Arrays.sort(sample);
            String line = br.readLine();
            int t = 0;
            for (int i = 0; i < sampleSize; i++) {
                while (t < sample[i]) {
                    line = br.readLine();
                    t++;
                }
                bw.write(line + "\n");
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
     * Mines the FSP from a dataset using the PrefixSpan algorithm
     *
     * @param dataset the file of the dataset
     * @param fileFSP the name of the output file that will contain the FSP
     * @param theta   the minimum frequency threshold
     * @return the number of FSP extracted from the dataset
     */
    public static int mining(String dataset, String fileFSP, double theta) {
        AlgoPrefixSpan alg = null;
        try {
            alg = new AlgoPrefixSpan();
            alg.runAlgorithm(dataset, theta, fileFSP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  alg.patternCount;
    }
}