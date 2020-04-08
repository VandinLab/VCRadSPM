import subprocess
import math
import random
import sys
import os

def randomAssignment(line,fw1,fw2):
    label = random.randint(0, 1)
    if (label == 1):
        fw1.write(line)
    else:
        fw2.write(line)

dataset = sys.argv[1]
theta = float(sys.argv[2])
delta = float(sys.argv[3])
fr = open(dataset, 'r')
splitting = dataset.split(".")
fw1 = open(splitting[0]+"_sx"+".txt", 'w') #+1
fw2 = open(splitting[0]+"_dx"+".txt", 'w') #-1
line = fr.readline()
randomAssignment(line,fw1,fw2)
while line:
    line = fr.readline()
    randomAssignment(line,fw1,fw2)
fr.close()
fw1.close()
fw2.close()

subprocess.getoutput("g++ -o src/approx_rade src/approx_rade.cpp")
fw = open("data/TFSP/radeApprox_singleDataset.txt", "w+")

dataset_sx = splitting[0]+"_sx"
dataset_dx = splitting[0]+"_dx"
find = False
kappa = 10.0
approx_rade = 0.0
dataset_size = 0.0
while(find == False):
    output_file_sx = dataset_sx + "_" + str(kappa) + ".txt"
    subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_sx + ".txt " + output_file_sx + " " +str(kappa)+"%", shell=True)
    output_file_dx = dataset_dx + "_" + str(kappa) + ".txt"
    subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_dx + ".txt " + output_file_dx + " " +str(kappa)+"%", shell=True)
    original_dataset = dataset
    #approximation of the Rademacher complexity: original dataset of transactions, sub-sample associated with label -1, file containing frequent patterns of sub-sample associated
    #with label +1, file containing frequent patterns of sub-sample associated with -1
    output_approx_rade = subprocess.check_output("./src/approx_rade " + original_dataset + " " + dataset_dx + ".txt " + output_file_sx + " " + output_file_dx, shell=True)
    splitting = (output_approx_rade.decode('utf-8')).split(" ");
    approx_rade = float(splitting[0])
    dataset_size = float(splitting[1])
    if (approx_rade == 0.0):
        kappa /= 2.0
    elif (approx_rade >= (kappa/100.0)):
        find = True
    else:
        kappa = (approx_rade - 1.0/100000000.0)*100.0
constant_term = math.sqrt((2.0*math.log(2.0/delta))/dataset_size)
approx_gap = 2.0*approx_rade + constant_term
fw.write(str(approx_gap)+ "\n")
fw.close()
splitting = dataset.split("/")
#mining with theta - approx_gap
new_theta = (theta - approx_gap)*100.0
if(new_theta >= 0.0):
    output_file = "data/TFSP/" + (splitting[1]).split(".")[0] + "_RB_FN_guarantees.txt"
    subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset + " " + output_file + " " +str(new_theta)+"%", shell=True)
else:
    print("Theta - approx_gap is negative.")
#mining with theta + approx_gap
new_theta = (theta + approx_gap)*100.0
output_file = "data/TFSP/" + (splitting[1]).split(".")[0] + "_RB_FP_guarantees.txt"
subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset + " " + output_file + " " +str(new_theta)+"%", shell=True)
