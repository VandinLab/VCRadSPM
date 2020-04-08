import subprocess
import math

subprocess.getoutput("g++ -o src/radebound src/radebound.cpp -lnlopt")
subprocess.getoutput("g++ -o src/compute_lengths src/compute_lengths.cpp")

datasets = ["BIBLE", "BMS1", "BMS2","KOSARAK","LEVIATHAN", "MSNBC"]
delta = "0.1"
beta_1 = 20
beta_2 = 120
fw = open("data/TFSP/radeBound.txt", "w+")

for dataset in datasets:
        for j in range(1,5):
                dataset_name = "data/TFSP/samples/" + dataset + "_S" + str(j) + ".txt"
                min_upper_bound = 100.0
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
                fw.write(str(min_upper_bound) + "\n")
fw.close()
