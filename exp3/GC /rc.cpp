#include <cstdio>
#include <vector>
#include <string>
#include <regex>
#include <map>
#include <ctime>
#include <chrono>
#include <iostream>
using namespace std;
using namespace std::chrono;

steady_clock::duration mallocTime;
steady_clock::duration deleteTime;
steady_clock::duration referMallocTime;
steady_clock::duration referDeleteTime;
long long referCount = 0;
char * newchar(int n){
    steady_clock::time_point startTime = steady_clock::now();//clock();
    char *s = new char[n + 1];
    steady_clock::time_point endTime = steady_clock::now();
    mallocTime += (endTime - startTime);
    return s;
}
void mydelete(char * s){
    steady_clock::time_point startTime = steady_clock::now();//clock();   
    delete[] s;
    steady_clock::time_point endTime = steady_clock::now();
    deleteTime += (endTime - startTime);
}
int * newint(int n){
    steady_clock::time_point startTime = steady_clock::now();//clock();
    int *r = new int(n);
    steady_clock::time_point endTime = steady_clock::now();
    referMallocTime += (endTime - startTime);
    return r;
}
void referDelete(int *r){
    steady_clock::time_point startTime = steady_clock::now();//clock();   
    delete[] r;
    steady_clock::time_point endTime = steady_clock::now();
    referDeleteTime += (endTime - startTime); 
}
struct MyString{
    char *s;
    int len;
    int *refer;

    MyString() : s(NULL), len(0), refer(NULL) {}
    MyString(int n): len(n){
        s = newchar(n);
        //refer = new int(1);
        refer = newint(1);
        referCount ++;
    }
    MyString(const char *s){
        len = strlen(s);
        this->s = newchar(len);
        memcpy(this->s,s,len);
        this->s[len] = 0;
        //refer = new int(1);
        refer = newint(1);
        referCount ++;
    }

    ~MyString(){
        if (!refer) return;
        referCount ++;
        if (--(*refer) > 0) return;
        referDelete(refer);
        //delete refer;
        if (s) {
            mydelete(s);
        }
    }

    MyString (const MyString &a) {
        this->s = a.s;
        this->len = a.len;
        this->refer = a.refer;
        (*refer)++; 
        referCount++;
    }

    char * begin() {
        return s;
    }

    char * end() {
        return s+len;
    }
    
    bool operator < (const MyString &a) const {
        return strcmp(s,a.s) < 0;
    }

};

#include <sys/resource.h>
size_t getPeakMemory() {
    struct rusage r_usage;
	getrusage(RUSAGE_SELF, &r_usage);
	return (r_usage.ru_maxrss / 1024L);
}

int main(int argc, char ** argv){
    FILE *f = fopen(argv[1],"r");
    char word[1001];
    vector<MyString> words;
    vector<MyString> tmp;
    while(1 == fscanf(f,"%s",word)){
        MyString myWord(word);
        words.push_back(myWord);
    }
    printf("num origin words: %d\n",(int)words.size());
    regex reg("[^a-zA-Z0-9]+");
    for(MyString word : words){
        string ns = regex_replace(word.s,reg,"");
        if(ns != ""){
            tmp.push_back(MyString(ns.c_str()));
        }
    }
    words.clear();
    for(MyString word : tmp){
        transform(word.begin(),word.end(),word.begin(),::tolower);
        words.push_back(word);
    }
    printf("num filtered words: %d\n",(int)words.size());

    map<MyString,int> wordmap;
    for(MyString word : words){
        wordmap[word] ++;
    }
    printf("num different words:%d\n",(int)wordmap.size());
    double mct = ((double)mallocTime.count())*steady_clock::period::num/steady_clock::period::den;
    double det = ((double)deleteTime.count())*steady_clock::period::num/steady_clock::period::den;
    double rmt = ((double)referMallocTime.count())*steady_clock::period::num/steady_clock::period::den;
    double rdt = ((double)referDeleteTime.count())*steady_clock::period::num/steady_clock::period::den;
    cout <<"mallocTime:"<<mct<<endl;
    cout <<"deleteTime:"<<det<<endl;
    cout <<"referMallocTime:"<<rmt<<endl;
    cout <<"referDeleteTime:"<<rdt<<endl;
    cout <<"total mallocTime:"<<mct+rmt<<endl;
    cout <<"total deleteTime:"<<det+rdt<<endl;
    cout <<"Modify Reference Counting Times:"<<referCount<<endl;

    cout << "Memory used: " << getPeakMemory() << endl;
}