package submit;

// some useful things to import. add any additional imports you need.
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import flow.Flow;



/**
 * Skeleton class for implementing a reaching definition analysis
 * using the Flow.Analysis interface.
 */
public class ReachingDefs implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the ReachingDefs analysis.
     * You are free to change this class or move it to another file.
     */
    public static class MyDataflowObject implements Flow.DataflowObject {
        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * These need to be filled in.
         */
        private Set<Integer> set;
        public static Set<Integer> universalSet;
        public MyDataflowObject(){ set = new TreeSet<Integer>();}


        public void setToTop() { set = new TreeSet<Integer>(); }
        public void setToBottom() { set = new TreeSet<Integer>(universalSet); }

        public void meetWith (Flow.DataflowObject o) {
            MyDataflowObject a = (MyDataflowObject) o;
            set.addAll(a.set);
        }

        public void copy (Flow.DataflowObject o) {
            MyDataflowObject a = (MyDataflowObject) o;
            set = new TreeSet<Integer>(a.set);
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
        public  int hashCode(){
            return set.hashCode();
        }

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[ID0, ID1, ID2, ...]", where each ID is
         * the identifier of a quad defining some register, and the
         * list of IDs must be sorted.  See src/test/Test.rd.out
         * for example output of the analysis.  The output format of
         * your reaching definitions analysis must match this exactly.
         */
        @Override
        public String toString() { return set.toString();}

        public void genDef(int v){set.add(v);}
        public void killDef(int v){set.remove(v);}
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private MyDataflowObject[] in, out;
    private MyDataflowObject entry, exit;
    private Map<String,Set<Integer>> Defmap;

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
    public void preprocess(ControlFlowGraph cfg) {
        // this line must come first.
        System.out.println("Method: "+cfg.getMethod().getName().toString());

        // get the amount of space we need to allocate for the in/out arrays.
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int id = qit.next().getID();
            if (id > max)
                max = id;
        }
        max += 1;

        // allocate the in and out arrays.
        in = new MyDataflowObject[max];
        out = new MyDataflowObject[max];

        // initialize the contents of in and out.
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            int id = qit.next().getID();
            in[id] = new MyDataflowObject();
            out[id] = new MyDataflowObject();
        }



        /************************************************
         * Your remaining initialization code goes here *
         ************************************************/
        Set<Integer> s = new TreeSet<Integer>();
        MyDataflowObject.universalSet = s;
        Defmap = new TreeMap<String, Set<Integer>>();

        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
            int id = q.getID();
            s.add(id);
            for(RegisterOperand def : q.getDefinedRegisters()){
                String v = def.getRegister().toString();
                if (!Defmap.containsKey(v)) {
                    TreeSet newset = new TreeSet<Integer>();
                    newset.add(id);
                    Defmap.put(v, newset);
                }else {
                    Defmap.get(v).add(id);
                }
            }
        }

        // initialize the entry and exit points.
        entry = new MyDataflowObject();
        exit = new MyDataflowObject();

        System.out.println("Initialization completed.");
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
    public void postprocess (ControlFlowGraph cfg) {
        System.out.println("entry: " + entry.toString());
        for (int i=0; i<in.length; i++) {
            if (in[i] != null) {
                System.out.println(i + " in:  " + in[i].toString());
                System.out.println(i + " out: " + out[i].toString());
            }
        }
        System.out.println("exit: " + exit.toString());
    }

    /**
     * Other methods from the Flow.Analysis interface.
     * See Flow.java for the meaning of these methods.
     * These need to be filled in.
     */
    public boolean isForward () { return true; }
    public Flow.DataflowObject getEntry() {
        Flow.DataflowObject result = newTempVar();
        result.copy(entry);
        return result;
    }
    public Flow.DataflowObject getExit() {
        Flow.DataflowObject result = newTempVar();
        result.copy(exit);
        return result;
    }
    public void setEntry(Flow.DataflowObject value) {
        entry.copy(value);
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
    public Flow.DataflowObject newTempVar() {return new MyDataflowObject();}

    public void processQuad(Quad q) {
        MyDataflowObject val=new MyDataflowObject();
        val.copy(in[q.getID()]);
        for (RegisterOperand def : q.getDefinedRegisters()) {
            for(int id : Defmap.get(def.getRegister().toString())){
                val.killDef(id);
            }
            val.genDef(q.getID());
        }
        out[q.getID()].copy(val);
    }
}
