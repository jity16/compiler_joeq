package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import flow.Flow;

public class RedundantNullCheck implements Flow.Analysis{

    public void preprocess(ControlFlowGraph cfg) {
        
    }

    public void postprocess(ControlFlowGraph cfg) {

    }

    public boolean isForward() {
        return false;
    }

    public Flow.DataflowObject getEntry() {
        return null;
    }

    public void setEntry(Flow.DataflowObject value) {

    }

    public Flow.DataflowObject getExit() {
        return null;
    }

    public void setExit(Flow.DataflowObject value) {

    }

    public Flow.DataflowObject getIn(Quad q) {
        return null;
    }

    public Flow.DataflowObject getOut(Quad q) {
        return null;
    }

    public void setIn(Quad q, Flow.DataflowObject value) {

    }

    public void setOut(Quad q, Flow.DataflowObject value) {

    }

    public Flow.DataflowObject newTempVar() {
        return null;
    }

    public void processQuad(Quad q) {

    }
}
