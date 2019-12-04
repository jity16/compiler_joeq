package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import flow.Flow;

public class RedundantNullCheck implements Flow.Analysis{
    public static class MyDataflowObject implements Flow.DataflowObject {
        private Set<String> set;
        public static Set<String> universalSet;
        public MyDataflowObject(){ set = new TreeSet<String>(universalSet); }

        public void setToTop() { set = new TreeSet<String>(universalSet); }
        public void setToBottom() { set = new TreeSet<String>(); }

        public void meetWith (Flow.DataflowObject o) {
            MyDataflowObject a = (MyDataflowObject)o;
            set.retainAll(a.set);  //intersection
        }
        public void copy (Flow.DataflowObject o) {
            MyDataflowObject a = (MyDataflowObject) o;
            set = new TreeSet<String>(a.set);
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MyDataflowObject){
                MyDataflowObject a = (MyDataflowObject) o;
                return set.equals(a.set);
            }
            return false;
        }

        @Override
        public int hashCode(){
            return set.hashCode();
        }

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[REG0, REG1, REG2, ...]", where each REG is
         * the identifier of a register, and the list of REGs must be sorted.
         * See src/test/TestFaintness.out for example output of the analysis.
         * The output format of your reaching definitions analysis must
         * match this exactly.
         */
        @Override
        public String toString() { return set.toString(); }
        public void genVar(String v) { set.add(v); }
        public void killVar(String v) { set.remove(v); }
        public boolean contains(String v){
            return set.contains(v);
        }
    }

    private MyDataflowObject[] in, out;
    private MyDataflowObject entry, exit;


    public void preprocess(ControlFlowGraph cfg) {
        System.out.println(cfg.getMethod().getName().toString());
        /* Generate initial conditions. */
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int x = qit.next().getID();
            if (x > max) max = x;
        }
        max += 1;
        in = new MyDataflowObject[max];
        out = new MyDataflowObject[max];
        qit = new QuadIterator(cfg);

        Set<String> s = new TreeSet<String>();
        MyDataflowObject.universalSet = s;

        /* Arguments are always there. */
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }

        while (qit.hasNext()) {
            Quad q = qit.next();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }

        entry = new MyDataflowObject();
        exit = new MyDataflowObject();
        transferfn.val = new MyDataflowObject();
        for (int i=0; i<in.length; i++) {
            in[i] = new MyDataflowObject();
            out[i] = new MyDataflowObject();
        }

//        System.out.println("Initialization completed.");
    }

    //To Do ?????
    public void postprocess(ControlFlowGraph cfg) {

    }

    public boolean isForward() {
        return true;
    }

    public Flow.DataflowObject getEntry() {
        Flow.DataflowObject result = newTempVar();
        result.copy(entry);
        return result;
    }

    public void setEntry(Flow.DataflowObject value) {
        entry.copy(value);
    }

    public Flow.DataflowObject getExit() {
        Flow.DataflowObject result = newTempVar();
        result.copy(exit);
        return result;
    }

    public void setExit(Flow.DataflowObject value) {
        exit.copy(value);
    }

    public Flow.DataflowObject getIn(Quad q) {
        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]);
        return result;
    }

    public Flow.DataflowObject getOut(Quad q) {
        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]);
        return result;
    }

    public void setIn(Quad q, Flow.DataflowObject value) {
        in[q.getID()].copy(value);
    }

    public void setOut(Quad q, Flow.DataflowObject value) {
        out[q.getID()].copy(value);
    }

    public Flow.DataflowObject newTempVar() {
        return new MyDataflowObject();
    }


    /* Actually perform the transfer operation on the relevant
     * quad. */

    private TransferFunction transferfn = new TransferFunction ();
    public void processQuad(Quad q) {
        transferfn.val.copy(in[q.getID()]);
        transferfn.visitQuad(q);
        out[q.getID()].copy(transferfn.val);
    }

    /* The QuadVisitor that actually does the computation */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        MyDataflowObject val;
        @Override
        public void visitQuad(Quad q) {
            //TO DO ??????
            for (RegisterOperand use : q.getDefinedRegisters()) {
                val.genVar(use.getRegister().toString());
            }
            for (RegisterOperand def : q.getUsedRegisters()) {
                val.killVar(def.getRegister().toString());
            }
        }
    }
}
