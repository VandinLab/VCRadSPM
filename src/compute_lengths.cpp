#include <iostream>
#include <cstring>
#include <vector>
#include <stdlib.h>
#include <algorithm>
#include <time.h>
#include <sstream>

using namespace std;

/*
  compute average item-length and maximum item-length of the transactions of a given dataset
*/


int main(int argc, char* argv[]) {
    const short n_input_param = 1;
    if (argc != n_input_param+1) {
        cout << "Error in command line. Specify 1 parameter: dataset name\n";
        return 0;
    }
    const char * dataset_name = argv[1];
    double transaction_average_length;
    int max = 0;

    unsigned int dataset_size = 0;
    FILE *in_file = fopen(dataset_name, "r");
	if (in_file == NULL) {
        perror("Error opening input file");
        exit(errno);
    }

    char line[100000];
    while (fgets(line, sizeof(line), in_file)){
        const char *line1 = line;
        char *token;
        vector<int> transaction;
        while ((token = strsep((char **)&line1," ")) != NULL) {
            if (*token != '\0') {
                int n = strtol(token, NULL, 10);
                if ((n == -1) || (n == -2)) continue;
                transaction.push_back(n);
            }
        }//end while loop on line
        transaction_average_length += (double)transaction.size();
        if (transaction.size()>max) max = transaction.size();
        ++dataset_size;
    }//end of while loop on the file
    transaction_average_length /= (double)dataset_size;
    fclose(in_file);

    cout << transaction_average_length << " " << max;
    return 0;
}
