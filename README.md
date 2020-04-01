# VCRadSPM: Mining Sequential Patterns with VC-Dimension and Rademacher Complexity
## Authors: Diego Santoro (diego.santoro@dei.unipd.it), Andrea Tonon (andrea.tonon@dei.unipd.it), and Fabio Vandin (fabio.vandin@unipd.it)
This repository contains the implementation of the algorithms introduced in the paper *Mining Sequential Patterns with VC-Dimension and Rademacher Complexity* and the code that has been used to test their performance.

The code to compute the bound on the VC-dimension and to perform the evaluation has been developed in Java and executed using version 1.8.0_201. The code to compute the bound and the approximation to the Rademacher complexity has been developed in C++. 
To mine sequential patterns, we used the PrefixSpan implementation provided by the [SPMF library](https://www.philippe-fournier-viger.com/spmf/). We used [NLopt](https://nlopt.readthedocs.io/en/latest/) as non-linear optimization solver.

## Package Description
The package contains the following folders:
* src/: contains the source code 
* data/: contains the datasets used in the evaluation
* data/sampling/: contains the output files of the sampling algorithm
* data/sampling/datasets/: contains the enlarged datasets built from the real datasets created for the evaluation of the sampling algorithm
* data/TFSP/: contains the output files of the TFSP algorithms
* data/TFSP/samples/: contains the pseudo-artificial datasets built from the real datasets created for the evaluation of the TFSP algorithms

## Download and Install Nlopt
Download it from https://github.com/stevengj/nlopt/archive/v2.6.1.tar.gz.
Install it by running the following code in the nlopt directory (for detailed information, look at https://nlopt.readthedocs.io/en/latest/NLopt_Installation/):
```
mkdir build
cd build
cmake ..
make
sudo make install
```
## Compile
These are the instructions to compile the code:

Java code:
```
javac src/*.java
```
The C++ source code is compiled inside the python scripts to execute the algorithms.

## Reproducibility
We provided the source code and the scripts to replicate the results shown in Section 7 of the paper. 

### Sampling Algorithm
Usage:
```
java -XmxRG -cp ./src TestSampling
```
-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 10G).

All the results are stored in the data/sampling/ folder while the enlarged datasets used for the evaluation are stored in the data/sampling/datasets/ folder. The program writes to the standard output all the results shown in Section 7.3 of the paper. 

### TFSP Algorithm
Since the computation of the approximation to the Rademacher complexity is time consuming, we provided two different ways to replicate the TFSP results:
	
* It is possible to run the TFSP evaluation computing the upper bounds on the maximum deviation with the empirical VC-dimension on the fly, and using precomputed upper bounds on the maximum deviation for the Rademacher complexity bounds and approximations. The precomputed values for the Rademacher approaches are stored in the [radeBound](data/TFSP/radeBound.txt) and [radeApprox](data/TFSP/radeApprox.txt) files, respectively.

Usage: 
```
java -XmxRG -cp ./src TestTFSP 
```
-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 10G).

* It is possible to run the TFSP evaluation computing the upper bounds on the maximum deviation with all the approaches on the fly.

Usage: 
```
python3 TFSP_tests.py
```

All the results are stored in the data/TFSP/ folder while the pseudo-artificial datasets used for the evaluation are stored in the data/TFSP/samples/ folder. The program writes to the standard output all the results shown in Section 7.4 of the paper.

## Execute
We also provide the source code to execute our algorithms with other datasets and with user-defined parameters.

### Sampling Algorithm
Usage: 
```
java -XmxRG -cp ./src Sampling dataset theta epsilon delta FPF
```
-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 10G). 

Arguments:
* dataset: the name of the file containing the sequential dataset. The dataset must be in the format described in the SPMF Library documentation and it must be in the data folder (e.g., MSNBC.txt)
* theta: the minimum frequency threshold in (0,1] (e.g., 0.04)
* epsilon: the epsilon for the approximation in (0,1) (e.g., 0.05)
* delta: confidence parameter in (0,1) (e.g., 0.1)
* FPF: a boolean to choose the algorithm (true to obtain a FPF ε-approximation, false to obtain an ε-approximation)

Example of usage: 
```	
java -Xmx10G -cp ./src Sampling MSNBC.txt 0.04 0.05 0.1 false
```
The frequent sequential patterns mined from the sample are stored in a file, dataset_Approx.txt or dataset_FPF_Approx.txt, in the data/sampling/ folder, where dataset is the name of the file provided in input. 

### TFSP Algorithm using the empirical VC-dimension
Usage: 
```
java -XmxRG -cp ./src TFSP dataset theta delta FPF    
```
-XmxRG allows to specify the maximum memory allocation pool for the Java Virtual Machine (JVM). R must be replaced with an integer that represents the maximum memory in GB (e.g., 10G). 

Arguments:
* dataset: the name of the file containing the sequential dataset. The dataset must be in the format described in the SPMF Library documentation and it must be in the data folder (e.g., MSNBC.txt)
* theta: the minimum frequency threshold in (0,1] (e.g., 0.04)
* delta: confidence parameter in (0,1) (e.g., 0.1)
* FPF: a boolean to choose the algorithm (true to obtain a FPF μ-approximation, false to obtain an μ-approximation)

Example of usage: 
```
java -Xmx10G -cp ./src TFSP MSNBC.txt 0.04 0.1 false
```
The true frequent sequential patterns mined from the dataset are stored in a file, dataset_TFSP_Approx.txt or dataset_TFSP_FPF_Approx.txt, in the data/TFSP/ folder, where dataset is the name of the file provided in input. 

### TFSP Algorithm using an upper bound on the empirical Rademacher complexity
Usage: 
```
python3 script_radeBound_singleDataset.py dataset theta delta beta_1 beta_2
```

Arguments:
* dataset: the name of the file containing the sequential dataset. The dataset must be in the format described in the SPMF Library documentation and it must be in the data folder (e.g., MSNBC.txt)
* theta: the minimum frequency threshold in (0,1] (e.g., 0.04)
* delta: confidence parameter in (0,1) (e.g., 0.1)
* beta_1: positive integer number (e.g., 20)
* beta_2: positive integer number (e.g., 120)

Example of usage: 
```
python3 script_radeBound_singleDataset.py MSNBC.txt 0.04 0.1 20 120
```
The value of the upper bound to the maximum deviation is stored in radeBound_singleDataset.txt in the data/TFSP/ folder. The true frequent sequential patterns mined from the dataset are stored in dataset_RB_FN_guarantees.txt and dataset_RB_FP_guarantees.txt, in the data/TFSP/ folder, where dataset is the name of the file provided in input.

### TFSP Algorithm using an approximation to the empirical Rademacher complexity
Usage: 
```
python3 script_radeApprox_singleDataset.py dataset theta delta
```

Arguments:
* dataset: the name of the file containing the sequential dataset. The dataset must be in the format described in the SPMF Library documentation and it must be in the data folder (e.g., MSNBC.txt)
* theta: the minimum frequency threshold in (0,1] (e.g., 0.04)
* delta: confidence parameter in (0,1) (e.g., 0.1)

Example of usage: 
```
python3 script_radeApprox_singleDataset.py MSNBC.txt 0.04 0.1
```
The value of the upper bound to the maximum deviation is stored in radeApprox_singleDataset.txt in the data/TFSP/ folder. The true frequent sequential patterns mined from the dataset are stored in dataset_RA_FN_guarantees.txt and dataset_RA_FP_guarantees.txt, in the data/TFSP/ folder, where dataset is the name of the file provided in input.

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details
