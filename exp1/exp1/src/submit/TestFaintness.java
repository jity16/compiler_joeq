package submit;

class TestFaintness {
    /**
     * In this method all variables are faint because the final value is never used.
     * Sample out is at src/test/Faintness.out
     */
    void test1() {
        int x = 2;
        int y = x + 2;
        int z = x + y;
        return;
    }

    /**
     * Write your test cases here. Create as many methods as you want.
     * Run the test from root dir using
     * ./run.sh flow.Flow submit.MySolver submit.Faintness submit.TestFaintness
     */
    /*
      void test2() {
      }
      ...
    */

    int test2() {
        int x = 2;      //x is faint
        int y = x + 2;  //y is not faint, because y is used in return
        int z = x + y;  //z is faint
        return y;
    }

    int test3(){
        int x = 1;      //x is not faint, because x is used in return
        int y = 2;      //y is faint
        int i = 0;      //i is not faint, because i is used in loop condition test
        while(i < 5){
            i ++;
        }
        return x;
    }

    int test4(int a, int b , int x, int y){
        //a , b is faint
        x = 0;          //x is not faint, because x is used in return
        y = x + 1;      //y is faint
        int z = x + y;  //z is faint
        return x;
    }
}
