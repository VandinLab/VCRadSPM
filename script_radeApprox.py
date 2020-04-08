import subprocess
import math
import random
import glob

def randomAssignment(line,fw1,fw2):
    label = random.randint(0, 1)
    if (label == 1):
        fw1.write(line)
    else:
        fw2.write(line)

for dataset in glob.glob("data/TFSP/samples/*_S*"):
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
datasets = ["BIBLE", "BMS1", "BMS2","KOSARAK","LEVIATHAN", "MSNBC"]
fw = open("data/TFSP/radeApprox.txt", "w+")
delta = 0.1

for dataset in datasets:
    for j in range(1,5):
        dataset_sx = "data/TFSP/samples/" + dataset + "_S" + str(j) + "_sx"
        dataset_dx = "data/TFSP/samples/" + dataset + "_S" + str(j) + "_dx"
        find = False
        kappa = 10.0
        approx_rade = 0.0
        dataset_size = 0.0
        while(find == False):
            output_file_sx = dataset_sx + "_" + str(kappa) + ".txt"
            subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_sx + ".txt " + output_file_sx + " " +str(kappa)+"%", shell=True)
            output_file_dx = dataset_dx + "_" + str(kappa) + ".txt"
            subprocess.check_output("java -Xmx50G -jar src/spmf.jar run PrefixSpan " + dataset_dx + ".txt " + output_file_dx + " " +str(kappa)+"%", shell=True)
            original_dataset = "data/TFSP/samples/" + dataset + "_S" + str(j) + ".txt"
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
        fw.write(str(approx_gap) + "\n")
fw.close()
