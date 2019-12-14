### 实验二：基于 JoeQ 的程序优化

计64	嵇天颖	2016010308

---

**目录**

[TOC]





----

#### 任务一

##### 任务要求

实现 `submit.FindRedundantNullChecks`，其功能应为分析并打印输入的所有类名所对应类的各方法的冗余 `NULL_CHECK Quads`



##### 设计思路

1. `Null Check`数据流分析框架：

   ~~~C++
   Direction: 					Forward
   Meeting Operation: 	Intersection
   Initial Condition:  Out[Entry] = empty set
   Transfer Function:  f(x) = gen and (x - kill)
   ~~~

2. 类`RedundantNullCheck`

   * 与`ReachingDefs`分析实现类似，继承自`Flow.Analysis`，按照需求实现框架函数

   * 传递函数`Transfer Function`:

     ~~~java
     if(op instanceof Operator.NullCheck) { // x and gen
       for (RegisterOperand use : q.getUsedRegisters()) {
         val.genVar(use.getRegister().toString());
       }
     }else{																// x - kill
       for (RegisterOperand def : q.getDefinedRegisters()) {
         val.killVar(def.getRegister().toString());
       }
     }
     ~~~

   * 输出处理`postprocess`:
     * 设置全局`nullset`用于存储`NullCheck`冗余的`Quad`的`id`
     * 用`QuadIterator`遍历程序流图，根据前面分析的结果将冗余`Quad`的编号加入`nullset`中
     * 遍历`nullset`输出

3. 类`FindRedundantNullCheck`

   按照注释所提示的实现，获取类名，进行`RedundantNullCheck`分析

  ~~~java
  FlowSolver solver = new FlowSolver();
  for(String name : args){
    jq_Class clazz = (jq_Class) Helper.load(name);
    RedundantNullCheck nullCheck = new RedundantNullCheck();
    solver.registerAnalysis(nullCheck);
    Helper.runPass(clazz,solver);
  }
  ~~~



##### 实验结果

* 测试`NullTest.java`和`SkipList.java`，测试结果与预期一致





---

#### 任务二

##### 任务要求

实现 `submit.Optimize.optimize(List optimizeClasses, boolean nullCheckOnly)`。其功能应为对根据类名 `Helper.load` 得到的 `clazz` 进行优化；在 `nullChecksOnly` 为 `true` 时，仅移除冗余的 `NULL_CHECK`，否则也进行其它优化



##### 设计思路

* 为`RedundantNullCheck`设置`doRemove`参数，用来分离两个任务

* 当`doRemove`被设置为`true` 时，我们在`postprocess` 中进行处理

  * 利用`QuadIterator`遍历时发现`Null Check`块，并移除冗余块

    ~~~java
    if(isRebundant){
      nullset.add(id);
      if(doRemove){
        qit.remove();
      }
    }
    ~~~



##### 实验结果

**测试1**

* 在未进行`Remove`操作时，我们运行`SkipList`，得到运行时信息

  ~~~java
  Result of interpretation: Returned: null (null checks: 5393 quad count: 20953)
  ~~~

* 进行移除冗余块操作时，再次运行，我们的到运行信息：

  ~~~java
  Result of interpretation: Returned: null (null checks: 4104 quad count: 19664)
  ~~~

* 我们发现优化效果比较明显，执行`Null Check`的指令次数减少

**测试2**

* 在未进行`Remove`操作时，我们运行`QuickSort`，得到运行时信息

  ~~~java
  Result of interpretation: Returned: null (null checks: 1800 quad count: 7017)
  ~~~

* 进行移除冗余块操作时，再次运行，我们的到运行信息

  ~~~java
  Result of interpretation: Returned: null (null checks: 1724 quad count: 6941)
  ~~~

* 我们发现优化效果比较明显，执行`Null Check`的指令次数减少

---



#### Bonus任务

在查看`joeq`框架源码时，我发现它除了