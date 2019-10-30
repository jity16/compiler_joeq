### 基于 JoeQ 的数据流分析

计64	嵇天颖	2016010308

---

**目录**

[TOC]



---

#### 任务1

##### 要求

完成 `submit.MySolver`。它位于 `src/submit/MySolver.java`。使用已经提供好的 `flow.ConstantProp` 和 `flow.Liveness` 作为在这一 Solver 上运行的分析算法来测试其正确性；期望的输出分别位于 `test/Test*.cp.out` 和 `test/Test*.lv.out`。



##### 实现

`ConstantProp`是前项数据流分析，`Liveness`是逆向数据流分析，我们分别实现它们。

解决前向和逆向数据流分析的算法都是通过迭代，用交汇运算处理前驱或者后继`Quad`来更新`IN`和`OUT`的值，结束条件是是否存在`OUT`（前向）或者`IN`（逆向）的值发生改变。

* 用`changed`来标志是否完成迭代

~~~java
public void visitCFG(ControlFlowGraph cfg) {
        // this needs to come first.
        analysis.preprocess(cfg);
        boolean changed = true;
        if(this.analysis.isForward()){
            while(changed){
                changed = forwardCFG(cfg);  //forward dataflow analysis
            }
        }
        else{
            while(changed){
                changed = backwardCFG(cfg); //backward dataflow analysis
            }
        }
        // this needs to come last.
        analysis.postprocess(cfg);
    }
~~~



* 前向数据流分析
  * 对控制流图的每一个`Quad`，获取全部的前驱
  * 如果当前的`Quad`是出口`Exit`的前驱，将其加入出口的前驱节点中
  * 调用`processQuad`进行`OUT`的运算
  * 检查是否有`OUT`的更新
* 逆向数据流分析
  * 与前向数据流分析思路基本一致，只是要不断更新的是入口`Entry`的后继，获取每一个`Quad`的后继
  * 调用`processQuad`进行`IN`的运算
  * 检查是否有`In`的更新



##### 结果

* 测试`Flow ConstantProp Test`和`Flow ConstantProp TestTwo`均与正确输出一致，测试通过
* 测试`Flow Liveness Test`和`Flow Liveness TestTwo`均与正确输出一致，测试通过



**参考**

参考了`joeq`分析框架源码中`joeq/Compil3r/Quad/DataflowFramework.java`实现的`visitCFG`接口，不得不说，官方源码真的太有用了，之前无从入手，照着写就成功了，在数据流算法中的迭代过程参照了它的实现和思路。





---



#### 任务2

##### 要求

完成 `submit.ReachingDefs`。它位于 `src/submit/ReachingDefs.java`。在 `submit.MySolver` 上运行时它应输出和 `test/Test*.rd.out` 相同的正确结果。



##### 实现

* 参照框架`Flow.Liveness`实现`setToTop`, `setToBottom`, `copy`, `equals`, `hashCode`, `toString`, `genDef`, `killDef`, `isForward`等诸多函数接口
* 与`Liveness`不同的是：
  * `Reaching Definition`分析是前向数据流分析
  * 与`Liveness`类似，我们在`preprocess`中设置集合`s`,但`ReachingDef`中集合存储的是`Quad`的`id`信息，用`Integer`类型处理
  * 设置了一个`Defmap`来实现`Registers`和`Quad id`的映射
  * 

