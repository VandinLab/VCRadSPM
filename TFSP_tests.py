#This script allows to execute the TFSP tests which results are reported in Section 7.4 of the paper.

import os

#compile the java source code
cmd = "javac src/*.java"
os.system(cmd)

#create the samples
cmd = "java -cp ./src CreateSamples"
os.system(cmd)

#compute upper bounds to the maximum deviation using an upper bound on the
#Rademacher complexity
cmd = "python3 script_radeBound.py"
os.system(cmd)

#compute upper bounds to the maximum deviation using an approximation to the
#Rademacher complexity
cmd = "python3 script_radeApprox.py"
os.system(cmd)

#execute the TFSP tests 
cmd = "java -cp ./src TestTFSP"
os.system(cmd)