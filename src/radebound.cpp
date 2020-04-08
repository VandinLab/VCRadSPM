#include <iostream>
#include <string>
#include <math.h>
#include <unordered_map>
#include <unordered_set>
#include <iterator>
#include <vector>
#include <set>
#include <limits>
#include <algorithm>
#include <nlopt.h>
#include <float.h>
#include <stdlib.h>
#include <chrono>
#include <sstream>
#include <time.h>
#include <bits/stdc++.h>

using namespace std;
vector<pair<int,int>> items_ordering;
int offset;

/*
    Computing the binomial coefficient Bin(n,k)
*/
long double binomialCoeff(int n, int k){
    long double C[n + 1][k + 1];
    int i, j;

    for (i = 0; i <= n; i++){
        for (j = 0; j <= min(i, k); j++){
            if (j == 0 || j == i)
                C[i][j] = 1.0;
            else
                C[i][j] = C[i - 1][j - 1] + C[i - 1][j];
        }
    }

    return C[n][k];
}

/*
    Comparison between two items
    param a: pair of integers representing an item and its frequency
    param b: pair of integers representing an item and its frequency
    return: true if the frequency of the first item lower than the frequency of the second one or if the first item comes before of the second one in the alphabetical order (with equal frequencies),
            false otherwise
*/
bool mycompare(pair <int,int> a, pair <int,int> b){
	return (items_ordering.at(a.first+offset).second < items_ordering.at(b.first+offset).second ||
	 (items_ordering.at(a.first+offset).second == items_ordering.at(b.first+offset).second && a.first < b.first));
}

/*
    Optimization function (for more details look at the NLopt C++ reference)
    param x: point of the function (optimization parameters)
    param grad: not used
    param data_opt: additional data for computing the objective function
    return: the value of the objective function
*/
double opt_fun(unsigned, const double *x, double *grad, void *data_opt) {
	vector<pair<long double, long double> > *data_exp_sum = (vector<pair<long double, long double> > *) data_opt;

	auto iter = (*data_exp_sum).begin();
	long double s_square = powl(x[0], 2.0);
	long double sum = 1.0;

	while (iter != (*data_exp_sum).end()) {
		long double exponent = s_square * iter->first;
    	sum += ( (1.0 + iter->second) * exp(exponent));
		++iter;
	}

	return (double)( log(sum) / x[0] );
}

/*
    Computing the upper bound to maximum deviation
    Input parameters: name of the dataset, confidence parameter delta, threshold for the item-length
*/

int main(int argc, char* argv[]) {
	const short n_input_param = 3;
	unsigned int dataset_size = 0u;
	if (argc != n_input_param+1) {
        cout << "Error in command line. Specify 3 parameters: dataset, delta, and item-length threshold.\n";
        return 0;
    }
	const char * dataset_name = argv[1];
	double delta = std::stod(argv[2]);
	unsigned int replication_factor = 1;
	int item_length_threshold = stoi(argv[3]);

	FILE *in_file = fopen(dataset_name, "r");
	if (in_file == NULL) {
		perror("Error opening input file");
		exit(errno);
	}

	vector<map<int,int>> dataset;
	map<int,int> transactions_itemlength;
	map<int,int> items; //item-frequency map
	int max_item_value = numeric_limits<int>::min(), min_item_value = numeric_limits<int>::max();
	int max_transactions_itemlength = 0;
    char line[100000];

	while (fgets(line, sizeof(line), in_file)) {
		++dataset_size;
		const char *line1 = line;
		char *token;
		vector<int> transaction;
		map<int,int> item_multiplicity_transaction;

		while ((token = strsep((char **)&line1," ")) != NULL) {
			if (*token != '\0') {
				int n = strtol(token, NULL, 10);
				if ((n == -1) || (n == -2)) continue;
				transaction.push_back(n);
				map<int,int>::iterator it = item_multiplicity_transaction.find(n);
				if (it != item_multiplicity_transaction.end()) it->second++;
				else item_multiplicity_transaction.insert(make_pair(n, 1));
			}
		}// end of a line containing a transaction

		if (transaction.size()>max_transactions_itemlength){
			max_transactions_itemlength = transaction.size();
		}

		dataset.push_back(item_multiplicity_transaction);

		map<int,int>::iterator itl = transactions_itemlength.find(transaction.size());
		if (itl != transactions_itemlength.end()) itl->second++;
		else transactions_itemlength.insert(make_pair(transaction.size(), 1));

		map<int,int>::iterator it;
		for (auto &e: item_multiplicity_transaction) {
			if (e.first > max_item_value) max_item_value = e.first;
			if (e.first < min_item_value) min_item_value = e.first;
			it = items.find(e.first);
			if (it != items.end()) it->second++;
			else items.insert(make_pair(e.first, 1));
		}
	} // end of the dataset of transactions
	fclose(in_file);

	vector<long double> binomials(max_transactions_itemlength+1,0.0);

	// computation of the needed quantities
	int number_of_items = max_item_value - min_item_value + 1;
	offset = -min_item_value; // if min>0 offset=-min. if min<0 offset=|min|=-min
	vector<pair<int,int>> temp (number_of_items);
	for (auto &e: items)
		temp.insert(temp.begin()+(e.first + offset), e);
	for (int i=0; i<number_of_items; i++)
		items_ordering.push_back(temp.at(i));
	vector< map< pair<int,int>, int > > quantities; // <<k_a_tau,m_a_tau>,g>
	for(int i = 0; i < items_ordering.size(); i++){
		quantities.push_back(map< pair<int,int>, int >());
	}

	for (auto &transaction: dataset){
  		vector<pair<int,int>> trans_vect = vector<pair<int,int>>(transaction.begin(), transaction.end());

		sort(trans_vect.begin(), trans_vect.end(), mycompare);

		int k = 0; //k_a_tau
		int max_item_value = 0;
		for(int i = trans_vect.size()-1; i>=0 ; i--){
			k+=trans_vect.at(i).second;
			int index = trans_vect.at(i).first + offset;
			map< pair<int,int>, int >::iterator it = quantities.at(index).find(make_pair(k,trans_vect.at(i).second));
			if (it != quantities.at(index).end()) it->second++;
			else quantities.at(index).insert(make_pair(make_pair(k,trans_vect.at(i).second), 1));
		}// end on the transaction
	}// end for loop on the dataset

	vector<pair<long double, long double> > data_opt; // <factor that is multiplied to s^2 in the function, how many times the same exponential must be counted>
	data_opt.clear();
	long double factor = 2.0 * powl(dataset_size, 2.0) * replication_factor;

	for (auto &e: items){
		int index = e.first + offset;
		map< pair<int,int>, int > item_quantities = quantities.at(index);
		long double sum = 0.0;

		for (auto &f: item_quantities){
			int kk = (f.first).first; // k_a_tau where 'a' and 'tau' are respectively a generic item and transaction
			int m = (f.first).second; // multiplicity m
			long double g = (long double) f.second; // g_a_k_tau

			if (kk == m){
				sum += (  ((long double)(m) - 1.0) * g  );
			}
			else{
				if (kk >= item_length_threshold){
					if (binomials[kk-1] < 0.5){
						for (int i=1; i<= (item_length_threshold-2);i++){
							// Bin(n,k) <= (n*e/k)^k
							if (kk*(i+1) >= 500000)
								binomials[kk-1] += (powl( ( (((long double)(kk-1))*exp(1.0)) / ((long double)(i)) ), (long double)(i) ));
							else
								binomials[kk-1] += (long double)binomialCoeff(kk-1,i);
						} // end for loop up to item-length threshold
					} // and if binomials
					sum += (binomials[kk-1]*g);
				}//end if kk
				else
					sum += ( (  ((long double)m) * (powl(2,kk - m) - 1.0) + powl(2,kk - m)*(powl(2,m) -1.0 - ((long double)m)))  * g );
			}// end else (kk != m)
		}//end for loop on item_quantities
		pair<long double, long double> to_add(((long double) e.second) / factor, sum);
		data_opt.push_back(to_add);
	}// end for on items

	long double n_transactions = 0.0; //number of transactions with item-length greater or equal than the item-length threshold
	for (auto &e: transactions_itemlength) {
		if(e.first >= item_length_threshold){
			n_transactions +=  ((long double) e.second);
		}
	}
	pair<long double, long double> to_addvb(n_transactions/factor, (powl(2,n_transactions) - 1.0)-1.0);
	data_opt.push_back(to_addvb);

	// optimization (for details look at the NLopt C++ reference)
	nlopt_opt opt_prob = nlopt_create(NLOPT_LN_COBYLA, 1);
	nlopt_set_min_objective(opt_prob, opt_fun, &data_opt);
	double lb[1] = {1.0};
	nlopt_set_lower_bounds(opt_prob, lb);
	const double xtol_abs = 1e-10;
	nlopt_set_xtol_abs(opt_prob, &xtol_abs);
	nlopt_set_ftol_abs(opt_prob, 1e-10);
	double x[1] = {1000.0};
	double rade_upper_bound;
	nlopt_result result = nlopt_optimize(opt_prob, x, &rade_upper_bound);
	nlopt_destroy(opt_prob);
	// end optimization

	if (result >= 0) {
		// constant term
		long double const_term = sqrt((2.0 * log (2.0 / delta)) / ((long double) (dataset_size * replication_factor)));
		// upper bound to the maximum deviation
		long double upper_bound = 2 * rade_upper_bound + const_term;
		cout << dataset_name <<  " " << upper_bound;
	}
	else {
		cout << "Optimization failed! return code: " << result << "\n";
	}
	return 0;
}
