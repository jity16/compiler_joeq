**GC**

* 手动管理`main.cpp`

  ~~~c++
  //compile
  g++ main.cpp -o main.out -std=c++11 -O2
  //run
  ./main.out datasets/datasets/wiki0.01.txt
  ~~~

  

* 引用计数

  * Version1： `rc.cpp`

    ~~~c++
    //compile
    g++ rc.cpp -o main.out -std=c++11 -O2
    //run
    ./rc.out datasets/datasets/wiki0.01.txt
    ~~~

  * Version2：`cop.cpp`

    ~~~c++
    g++ rcop.cpp -o rcop.out -std=c++11 -O2
    time ./rcop.out datasets/datasets/wiki0.01.txt 
    ~~~



**Z3**

* 实现了任务一

  * `Z3/main.cpp`

    