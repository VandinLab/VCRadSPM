public class CreateSamples {

    /**
     * This class creates the 4 pseudo-artificial datasets for each real dataset for the TFSP evaluation.
     * It is used in the script to compute the upper bound and the approximation on the Rademacher complexity
     * for the TFSP problem
     */
    public static void main(String[] args) {
        String[] datasets = {"BIBLE", "BMS1", "BMS2", "KOSARAK", "LEVIATHAN", "MSNBC"};
        int[] s = {0, 2, 3, 4};
        for (int i = 0; i < datasets.length; i++) {
            String dataset = "data/" + datasets[i] + ".txt";
            for (int k = 0; k < 4; k++) {
                String sample = "data/TFSP/samples/" + datasets[i] + "_S" + (k + 1) + ".txt";
                TestTFSP.createDataset(dataset, sample, s[k]);
            }
        }
    }
}