package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.Iterator;

/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);

        /***********************
         * Your code goes here *
         ***********************/
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

    private boolean forwardCFG(ControlFlowGraph cfg){
        boolean changed = false;

        QuadIterator qi = new QuadIterator(cfg,true);
        Flow.DataflowObject Exit = analysis.newTempVar();
        while(qi.hasNext()){
            /*get predecessors of current quad*/
            Quad currentQuad = qi.next();

            /*variable to hold the new in*/
            Flow.DataflowObject currentQuadIn = analysis.newTempVar();
            currentQuadIn.setToTop();

            /* meet with the OUTs of all predecessors to compute IN */
            Iterator<Quad> predit = qi.predecessors();
            if(predit == null){
                currentQuadIn.meetWith((analysis.getEntry()));
            }else {
                while (predit.hasNext()) {
                    Quad pred = predit.next();
                    if (pred == null) {
                        currentQuadIn.meetWith(analysis.getEntry());
                    } else {
                        currentQuadIn.meetWith(analysis.getOut(pred));
                    }
                }
            }

            analysis.setIn(currentQuad,currentQuadIn);
            /*old OUT use for comparison*/
            Flow.DataflowObject oldOut = analysis.getOut(currentQuad);
            /*use interface processQuad to deal with in/out after setting*/
            analysis.processQuad(currentQuad);
            Flow.DataflowObject newOut = analysis.getOut(currentQuad);
            /*find whether out has been changed*/
            if(!oldOut.equals(newOut)){
                changed = true;
            }

            /*find all exit block*/
            Iterator<Quad> succit = qi.successors();
            while(succit.hasNext()){
                Quad succ = succit.next();
                if(succ == null){
                    Exit.meetWith(analysis.getOut(currentQuad));
                    break;
                }
            }
        }
        analysis.setExit(Exit);
        return changed;
    }

    private boolean backwardCFG(ControlFlowGraph cfg){
        boolean changed = false;
        return changed;
    }
}

