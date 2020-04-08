#include <iostream>
#include <string>
#include <math.h>
#include <cstring>
#include <stdio.h>
#include <stdlib.h>
#include <iterator>
#include <vector>
#include <set>
#include <limits>
#include <algorithm>
#include <float.h>
#include <chrono>
#include <sstream>
#include <unordered_map>
#include <bits/stdc++.h>

using namespace std;

/*
  Representation of an Itemset as a set of int
*/

class Itemset
{

  private:
    set<int> itemset;
  public:

  Itemset(){}

  /*
    Adding an item to an itemset
    param item: item to be added
  */
  void addItem(int item){
    itemset.insert(item);
  }

  /*
    Getting the set of items of an itemset
    return: set of items of an itemset
  */
  set<int> getSet(){
    return itemset;
  }

  /*
    Verifying if an itemset is contained in another one
    param x: an itemset
    return: true if x is contained in itemset 'this', false otherwise
  */
  bool contains(Itemset x){
    for (auto e: x.getSet()){
      auto pos = itemset.find(e);
      if (pos == itemset.end()) return false;
    }
    return true;
  }

  /*
    Verifying if an itemset is equal to another one
    param x: an itemset
    return: true if x is equal to itemset 'this', false otherwise
  */
  bool is_equal_to(Itemset x){
    if(itemset.size() != x.getSet().size())
      return false;
    for (auto e: x.getSet()){
      auto pos = itemset.find(e);
      if (pos == itemset.end()) return false;
    }
    return true;
  }
};

/*
   Representation of a sequetial pattern as a vector of Itemsets
*/

class Seq_Pattern
{

  private:
    vector<Itemset> sequence;
  public:

  Seq_Pattern(){}

  /*
    Adding an itemset to a sequential pattern
    param itemset: itemset to be added
  */
  void addItemset(Itemset itemset){
    Itemset my_itemset;
    for (auto e: itemset.getSet()){
      my_itemset.addItem(e);
    }
    sequence.push_back(my_itemset);
  }

  /*
    Verifying if a sequential pattern is contained in another sequential pattern
    param transaction: a sequential pattern
    return: true if sequential pattern 'this' is contained in transaction, false otherwise
  */
  bool is_contaied_in(Seq_Pattern transaction){
    vector<Itemset> t = transaction.getVector();
    if (sequence.size() > t.size()) return false;
    int general_index = 0;
    int last_found = -1;
    for (int i = 0; i<sequence.size(); i++){
        bool found = false;
        Itemset itemset_sequence = sequence[i];
        for (int j=general_index; j<t.size();j++){
            if (t[j].contains(itemset_sequence)){
                general_index = ++j;
                found = true;
                break;
            }
        }// for on transaction
        if (!found) return false;
    }// for on sequence
    return true;
  }

  /*
    Verifying if a sequential pattern is equal to another one
    param seq: a sequential pattern
    return: true is seq is equal to the sequential pattern 'this', false otherwise
  */
  bool is_equal_to(Seq_Pattern seq){
    vector<Itemset> sp = seq.getVector();
    if (sequence.size() != sp.size()) return false;
    for (int i = 0; i<sequence.size(); i++){
        if (!sequence[i].is_equal_to(sp[i]))
        return false;
    }
    return true;
  }

  /*
    Getting a sequential pattern
    return: vector of Itemsets that represents the sequential pattern 'this'
  */
  vector<Itemset> getVector(){
    return sequence;
  }
};

/*
  Parsing the file containing the frequent sequential patterns of the sub-sample associated with label +1
  param file_name: name of the file containing frequent sequential patterns
  return: frequent sequential patterns and their frequency as a vector of pairs of string and int
*/
vector<pair<string,int>> fileAnalyzer_dx(char * file_name){

  FILE *file = fopen((const char *)file_name, "r");
  if (file == NULL) {
    perror("Error opening file");
    exit(errno);
  }

  vector<pair<string,int>> patterns;
  char line[100000];

  while (fgets(line, sizeof(line), file)!=NULL) {
    char *token;
    const char *line1 = line;
    bool last = false;
    string pattern;
    int freq;
    while ((token = strsep((char **)&line1,"#SUP:")) != NULL) {
        if (*token != '\0') {
            if (!last){
                pattern = string(token);
                last = true;
            }
            else{
                freq = stoi((string(token)).substr(1));
            }
        }// if token
    }// while line1 (single line of the file)
    patterns.push_back(make_pair(pattern,freq));
  }// while line (end of the entire file)
  fclose(file);
  return patterns;
}

/*
  Parsing the file containing the frequent sequential patterns of the sub-sample associated with label -1
  param file_name: name of the file containing frequent sequential patterns
  return: frequent sequential patterns and their frequency as an unordered map of string-int
*/
unordered_map<string,int> fileAnalyzer_sx(char * file_name){

  FILE *file = fopen((const char *)file_name, "r");
  if (file == NULL) {
    perror("Error opening file");
    exit(errno);
  }

  unordered_map<string,int> patterns;
  char line[100000];

  while (fgets(line, sizeof(line), file)!=NULL) {
    char *token;
    const char *line1 = line;
    bool last = false;
    string pattern;
    int freq;
    while ((token = strsep((char **)&line1,"#SUP:")) != NULL) {
      if (*token != '\0') {
        if (!last){
          pattern = string(token);
          last = true;
        }
        else{
          freq = stoi((string(token)).substr(1));
        }
      }// if token
    }// while line1 (single line of the file)
    patterns.insert(make_pair(pattern,freq));
  }// while line (end of the entire file)
  fclose(file);
  return patterns;
}

/*
  Parsing a dataset of bag of transactions
  param file_name: name of the dataset
  return: vector of transactions (that are sequential patterns)
*/
vector<Seq_Pattern> dataset_processing(char * file_name){

  FILE *file = fopen((const char *)file_name, "r");
  if (file == NULL) {
    perror("Error opening file");
    exit(errno);
  }

  vector<Seq_Pattern> dataset;
  char line[100000];
  while (fgets(line, sizeof(line), file)!=NULL) {
    char *token;
    const char *line1 = line;
    Itemset itemset;
    Seq_Pattern sequence;
    while ((token = strsep((char **)&line1," ")) != NULL) {
      if (*token != '\0') {
        string element(token);
        if(element.compare("-1") == 0){
          sequence.addItemset(itemset);
          itemset = Itemset();
        }
        else if (element.compare("-2") != 0){
          itemset.addItem(stoi(element));
        }
      }// if token
    }// while line1 (single line of the file)
    dataset.push_back(sequence);
    sequence = Seq_Pattern();
  }// while line (end of the entire file)
  fclose(file);
  return dataset;
}

/*
  Let p be a sequential pattern and diff(p) be the difference between its frequencies in the two sub-samples associated with labels +1 and -1. This methods returns the maximum diff(p) over the set of
  patterns that are frequent in the two sub-samples.
  param dataset: entire dataset of transactions
  param dataset_sx: sub-samples associated with label -1
  param vectdx: frequent sequential patterns (and their frequencies) of the sub-sample associated with label +1
  param hashsx: frequent sequential patterns (and their frequencies) of the sub-sample associated with label -1
  return: maximum difference of frequencies
*/
long double compute_max_difference(vector<Seq_Pattern> dataset, vector<Seq_Pattern> dataset_sx, vector<pair<string,int>> vectdx, unordered_map<string,int> hashsx){

  long double max_difference = 0.0;
  vector<bool> checked_patterns_dx(vectdx.size(),false);

  //computing gamma_1
  for (int i = 0; i<vectdx.size() ; i++){
    string sp_dx = vectdx[i].first;
    unordered_map<string,int>::const_iterator it = hashsx.find(sp_dx);
    if (it != hashsx.end()){
      checked_patterns_dx[i] = true;
      long double difference = (long double)(vectdx[i].second-it->second);
      if(difference > max_difference) max_difference = difference;
    }
  }

  //loop for taking into account of gamma_2, in order to compute the maximum value between gamma_1 and gamma_2
  for (int i = 0; i<vectdx.size() ; i++){
    //now we have to consider only frequent patterns of the +1 sub-sample that are not frequent in the -1 sub-sample
    if (!checked_patterns_dx[i]){
      stringstream ss(vectdx[i].first);
      string token;
      char delim = ' ';
      Itemset itemset;
      Seq_Pattern seq;
      while (getline(ss, token, delim)) {
        if(token.compare("-1") == 0){
          seq.addItemset(itemset);
          itemset = Itemset();
       	}
        else if (token.compare("-2") != 0){
          itemset.addItem(stoi(token));
       	}
        else if (token.compare("-2") == 0){
          break;
	    }
      }// end loop for the string representing the sequential pattern

      //counting the frequency of seq in the sub-sample associated with libel -1
      int frequency_in_sx = 0;
      for (auto transaction:dataset_sx){
        if (seq.is_contaied_in(transaction))
          frequency_in_sx++;
      }
      long double difference = (long double)(vectdx[i].second-frequency_in_sx);
      if(difference > max_difference) max_difference = difference;
    }// end if
  }// end loop for taking into account of gamma_2

  return max_difference;
}

/*
  Computing an approximation of the Rademacher complexity
  input parameters: name of the entire dataset, name of the sub-sample associated with label -1, name of the file containing the frequent patterns of the sub-sample associated with +1,
  name of the file containing the frequent patterns of the sub-sample associated with -1
*/
int main(int argc, char* argv[]) {
    vector<Seq_Pattern> dataset = dataset_processing(argv[1]); //entire dataset of transactions
    vector<Seq_Pattern> dataset_sx = dataset_processing(argv[2]); //sub-sample associated with label -1
    vector<pair<string,int>> vectdx = fileAnalyzer_dx(argv[3]); //frequent sequential patterns of the sub-sample associated with +1
    unordered_map<string,int> hashsx = fileAnalyzer_sx(argv[4]); //frequent sequential patterns of the sub-sample associated with -1

    long double max_difference = compute_max_difference(dataset,dataset_sx,vectdx,hashsx);
    long double dataset_size = (long double)dataset.size();
    long double approx_rade = max_difference/dataset_size;
    cout << approx_rade << " " << dataset_size;

    return 0;
}
