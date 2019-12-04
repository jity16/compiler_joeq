package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import flow.Flow;

/**
 * Skeleton class for implementing a faint variable analysis
 * using the Flow.Analysis interface.
 */
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

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
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

        //initial value : out[entry] = null
        entry.setToBottom();

    }

    /**
     * This method is called after the fixpoint is reached.
     * It must print out the dataflow objects associated with
     * the entry, exit, and all interior points of the CFG.
     * Unless you modify in, out, entry, or exit you shouldn't
     * need to change this method.
     *
     * @param cfg  Unused.
     */
    public void postprocess(ControlFlowGraph cfg) {
        Set<Integer> nullset = new TreeSet<Integer>();
        QuadIterator qit = new QuadIterator(cfg,true);
        while(qit.hasNext()){
            Quad q = qit.next();
            int id = q.getID();
            Operator op = q.getOperator();
            if(op instanceof Operator.NullCheck){
                boolean isRebundant = true;
                for(RegisterOperand use : q.getUsedRegisters()){
                    if(!in[id].contains(use.getRegister().toString())){
                        isRebundant = false;
                    }
                }
                if(isRebundant){
                    nullset.add(id);
                }
            }
        }
        for(Integer id : nullset){
            System.out.print(" "+id);
        }
        System.out.println();
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
            Operator op = q.getOperator();
            if(op instanceof Operator.NullCheck) {
                for (RegisterOperand use : q.getDefinedRegisters()) {
                    val.genVar(use.getRegister().toString());
                }
            }else{
                for (RegisterOperand def : q.getUsedRegisters()) {
                    val.killVar(def.getRegister().toString());
                }
            }
        }
    }
}
