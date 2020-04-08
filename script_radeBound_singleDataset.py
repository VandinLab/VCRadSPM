import sys
import subprocess
import math
import os

subprocess.getoutput("g++ -o src/radebound src/radebound.cpp -lnlopt")
subprocess.getoutput("g++ -o src/compute_lengths src/compute_lengths.cpp")

dataset_name = sys.argv[1]
theta = float(sys.argv[2])
delta = sys.argv[3]
beta_1 = int(sys.argv[4])
beta_2 = int(sys.argv[5])
min_upper_bound = 100.0
fw = open("data/TFSP/radeBound_singleDataset.txt", "w+")

lengths = subprocess.check_output("./src/compute_lengths " + dataset_name, shell=True)
lengths_string = lengths.decode('utf-8')
results_lengths = lengths_string.split(" ")
min_eta = int(math.ceil(float(results_lengths[0]))) + beta_1
max_eta = int(min(beta_2,float(results_lengths[1])))
for threshold in range(min_eta,max_eta+1):
        output = subprocess.check_output("./src/radebound " + dataset_name + " " + delta + " " + str(threshold), shell=True)
        output_string = output.decode('utf-8')
        results = output_string.split(" ")
        try:
                upper_bound = float(results[1])
                res = True
        except:
                res = False
                print("The result cannot be converted in float: error in radebound.")
        if (res):
                if (upper_bound <= min_upper_bound or results[1]=="inf"):
                        min_upper_bound = upper_bound
fw.write(str(min_upper_bound)+ "\n")
fw.close()
splitting = dataset_name.split("/")
#mining with theta - min_upper_bound
new_theta = (theta - min_upper_bound)*100.0
if(new_theta >= 0.0):
    output_file = "data/TFSP/" + (splitting[1]).split(".")[0] + "_RB_FN_guarantees.txt"
    subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_name + " " + output_file + " " +str(new_theta)+"%", shell=True)
else:
    print("Theta - upper_bound is negative.")
#mining with theta + min_upper_bound
new_theta = (theta + min_upper_bound)*100.0
output_file = "data/TFSP/" + (splitting[1]).split(".")[0] + "_RB_FP_guarantees.txt"
subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_name + " " + output_file + " " +str(new_theta)+"%", shell=True)
