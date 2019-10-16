### 基于 JoeQ 的数据流分析

计64	嵇天颖	2016010308

---

#### 任务1

**要求**

完成 `submit.MySolver`。它位于 `src/submit/MySolver.java`。使用已经提供好的 `flow.ConstantProp` 和 `flow.Liveness` 作为在这一 Solver 上运行的分析算法来测试其正确性；期望的输出分别位于 `test/Test*.cp.out` 和 `test/Test*.lv.out`。



**实现**

`ConstantProp`是前项数据流分析，`Liveness`是逆向数据流分析，我们分别实现它们。

解决前向和逆向数据流分析的算法都是通过迭代，用交汇运算处理前驱或者后继`Quad`来更新`IN`和`OUT`的值，结束条件是是否存在`OUT`（前向）或者`IN`（逆向）的值发生改变。

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





