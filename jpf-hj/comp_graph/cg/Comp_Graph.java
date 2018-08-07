package cg;

import static cg.Edges.*;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.bytecode.ArrayElementInstruction;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceFieldInstruction;
import gov.nasa.jpf.vm.bytecode.StaticFieldInstruction;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;

public class Comp_Graph {

    public void finalize() {
      System.out.println("NumReports: " + numReports);
    }

    private static HashMap threads;
    private static AtomicInteger threadCounter;
    private static int numReports = 0;

    static int GRAPH_ITER = 0;
    static DirectedAcyclicGraph<Node, DefaultEdge> currentGraph = null;

    public static boolean analyzeFinishBlock(DirectedAcyclicGraph<Node, DefaultEdge> graph, String finishID, boolean otf) {
        List<activityNode> tasks = getTasksFromFinishBlock(finishID, graph);
        boolean race = false;
        for (int i = 0; i < tasks.size(); i++) {
	    Node task1 = tasks.get(i);
            for (int j = i + 1; j < tasks.size(); j++) {
                Node task2 = tasks.get(j);
                if (!happensBefore(task1, task2, graph) && !happensBefore(task2, task1, graph)){
                    if (detectDataRace(task1, task2)) race = true;
                }
            }
        } 
        return race;
    } 

    private static boolean happensBefore(Node task1, Node task2, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
    //** a very naive approach depth first search for the node
    //for implementation efficancy a node happens before itself
        if (task1 == task2) return true;
        for (DefaultEdge edge : graph.outgoingEdgesOf(task1)) {
            Node child = graph.getEdgeTarget(edge);
            if (happensBefore(child, task2, graph)) return true;
	}
        return false;
    }

    private static boolean detectDataRace(Node node1, Node node2) {
        //if either one of them is not an activity node there are no reads or writes to compare
        System.out.println("Considering " + node1 + " and " + node2);
        if (!(node1 instanceof activityNode) || !(node2 instanceof activityNode)) return false;
	if (node1.ti.equals(node2.ti)) return false; //if on same thread NOTE: optimization not in paper
        activityNode task1 = (activityNode) node1;
        activityNode task2 = (activityNode) node2;
        Set<Pair<Elements,Elements>> race_elements = new HashSet<Pair<Elements,Elements>>();
        Set<Pair<Elements,Elements>> iso_race_elements = new HashSet<Pair<Elements,Elements>>();
        Set<Pair<ArrayElements,ArrayElements>> array_race_elements = new HashSet<Pair<ArrayElements,ArrayElements>>();
        Set<Pair<ArrayElements,ArrayElements>> iso_array_race_elements = new HashSet<Pair<ArrayElements,ArrayElements>>();

        race_elements.addAll(intersection(task1.var_read, task2.var_write));
        race_elements.addAll(intersection(task1.var_read, task2.isolated_write));
        race_elements.addAll(intersection(task1.var_write, task2.var_read));
        race_elements.addAll(intersection(task1.var_write, task2.var_write));
        race_elements.addAll(intersection(task1.var_write, task2.isolated_read));
        race_elements.addAll(intersection(task1.var_write, task2.isolated_write));
        race_elements.addAll(intersection(task1.isolated_read, task2.var_write));
        iso_race_elements.addAll(intersection(task1.isolated_read, task2.isolated_write));
        race_elements.addAll(intersection(task1.isolated_write, task2.var_write));
        race_elements.addAll(intersection(task1.isolated_write, task2.var_read));
        race_elements.addAll(intersection(task1.isolated_write, task2.var_read));
        iso_race_elements.addAll(intersection(task1.isolated_write, task2.isolated_write));
        iso_race_elements.addAll(intersection(task1.isolated_write, task2.isolated_read));
        array_race_elements.addAll(intersection(task1.array_read, task2.array_write));
        array_race_elements.addAll(intersection(task1.array_read, task2.array_write_isolated));
        array_race_elements.addAll(intersection(task1.array_write, task2.array_read));
        array_race_elements.addAll(intersection(task1.array_write, task2.array_write));
        array_race_elements.addAll(intersection(task1.array_write, task2.array_read_isolated));
        array_race_elements.addAll(intersection(task1.array_write, task2.array_write_isolated));
        array_race_elements.addAll(intersection(task1.array_read_isolated, task2.array_write));
        iso_array_race_elements.addAll(intersection(task1.array_read_isolated, task2.array_write_isolated));
        array_race_elements.addAll(intersection(task1.array_write_isolated, task2.array_read));
        array_race_elements.addAll(intersection(task1.array_write_isolated, task2.array_write));
        iso_array_race_elements.addAll(intersection(task1.array_write_isolated, task2.array_read_isolated));
        iso_array_race_elements.addAll(intersection(task1.array_write_isolated, task2.array_write_isolated));
        


        for (Pair<Elements,Elements> race : race_elements) {
            System.out.println("Non-deterministic access between:\n\t" + race.first + "\n\t" + race.second);
        }
        for (Pair<ArrayElements,ArrayElements> race : array_race_elements) {
            System.out.println("Non-deterministic access between:\n\t" + race.first + "\n\t" + race.second);
        }
        for (Pair<Elements,Elements> race : iso_race_elements) {
            System.out.println("Intended race between:\n\t" + race.first + "\n\t" + race.second);
        }
        for (Pair<ArrayElements,ArrayElements> race : iso_array_race_elements) {
            System.out.println("Intended race between:\n\t" + race.first + "\n\t" + race.second);
        }
        return race_elements.size() + array_race_elements.size() != 0;
    }

    private static <T> List<Pair<T,T>> intersection(List<T> list1, List<T> list2) {
    List<Pair<T,T>> result = new ArrayList<Pair<T,T>>();
    if (list1 == null || list2 ==  null) return result;
    for (T first : list1) {
        for (T second : list2) {
            if (first != null && first.equals(second)) {
                result.add(new Pair(first,second));
            }
        }
    }
    return result;

}

    public static void registerHeapAccess(Node n1, Elements var, Boolean insnIsRead, Boolean Isolated) {

        activityNode n = (activityNode) n1;
        if (insnIsRead) {
            if (Isolated) {
                if (n.isolated_read == null) {
                    n.createIsolatedReadList();
                }
                n.isolated_read.add(var);
            } else {
                if (n.var_read == null) {
                    n.createReadList();
                }
                n.var_read.add(var);
            }
        } else {
            if (Isolated) {
                if (n.isolated_write == null) {
                    n.createIsolatedWriteList();
                }
                n.isolated_write.add(var);
            } else {
                if (n.var_write == null) {
                    n.createWriteList();
                }
                n.var_write.add(var);
            }
        }
    }

    public static void registerHeapAccessArray(Node n1, ArrayElements ae, Boolean insnIsRead, Boolean Isolated) {

        activityNode n = (activityNode) n1;
        if (insnIsRead) {
            if (Isolated) {
                if (n.array_read_isolated == null) {
                    n.createArrayIsolatedReadList();
                }
                n.array_read_isolated.add(ae);
            } else {
                if (n.array_read == null) {
                    n.createArrayReadList();
                }
                n.array_read.add(ae);
            }
        } else {
            if (Isolated) {
                if (n.array_write_isolated == null) {
                    n.createArrayIsolatedWriteList();
                }
                n.array_write_isolated.add(ae);
            } else {
                if (n.array_write == null) {
                    n.createArrayWriteList();
                }
                n.array_write.add(ae);
            }
        }
    }

    public static void createMasterNode(finishNode fin, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        Set<Node> removeNodes = new HashSet<Node>();
        Queue<DefaultEdge> edges = new LinkedList<DefaultEdge>();
        edges.addAll(graph.outgoingEdgesOf(fin));
        Node fin_end = null;
        while (!edges.isEmpty()) {
            DefaultEdge e = edges.remove();
            Node n = graph.getEdgeTarget(e);
            if (!n.id.equals(fin.id + "-end")) {
                removeNodes.add(n);
                edges.addAll(graph.outgoingEdgesOf(n));
            } else {
                fin_end = n;
            }
        }

        activityNode master = new activityNode("Master");
        for (Node n : removeNodes) {
            if (n instanceof activityNode) {
                copyHeapData((activityNode) n, master);
            }
            graph.removeVertex(n);
        }
        graph.addVertex(master);
        addContinuationEdge(fin, master, graph);
        addContinuationEdge(master, fin_end, graph);
    }

    public static void copyHeapData(activityNode source, activityNode Target) {
        if (source.var_read != null) {
            copy(source.var_read, Target.var_read);
        }
        if (source.var_write != null) {
            copy(source.var_write, Target.var_write);
        }
        if (source.isolated_read != null) {
            copy(source.isolated_read, Target.isolated_read);
        }
        if (source.isolated_write != null) {
            copy(source.isolated_write, Target.isolated_write);
        }
        if (source.array_read != null) {
            copyArray(source.array_read, Target.array_read);
        }
        if (source.array_read_isolated != null) {
            copyArray(source.array_read_isolated, Target.array_read_isolated);
        }
        if (source.array_write != null) {
            copyArray(source.array_write, Target.array_write);
        }
        if (source.array_write_isolated != null) {
            copyArray(source.array_write_isolated, Target.array_write_isolated);
        }
    }

    public static void copy(List<Elements> source, List<Elements> target) {
        if (target == null) {
            target = new LinkedList<Elements>();
        }
        target.addAll(source);
    }

    public static void copyArray(List<ArrayElements> source, List<ArrayElements> target) {
        if (target == null) {
            target = new LinkedList<ArrayElements>();
        }
        target.addAll(source);
    }

    static List<activityNode> getTasksFromFinishBlock(String finID, DirectedAcyclicGraph<Node, DefaultEdge> graph) {

	//Returns in topographic order only the nodes in graph that are within the
        // given finish block
        List<activityNode> tasks = new ArrayList<activityNode>();
        Queue<Node> nodes = new LinkedList<Node>();

        Node start = searchGraph(finID, graph);
        Node end = searchGraph(finID + "-end", graph);

        nodes.add(start);
        while (!nodes.isEmpty()) {
            for (DefaultEdge v1OutEdge : graph.outgoingEdgesOf(nodes.poll())) {
                Node n = graph.getEdgeTarget(v1OutEdge);
                if (!n.equals(end)) {
                    if (n instanceof activityNode && !tasks.contains(n)) {
                        tasks.add((activityNode) n);
                    }
                    nodes.add(n);
                }
            }
        }
        return tasks;
    }

    static int createGraph(DirectedAcyclicGraph<Node, DefaultEdge> graph, String targetDirectory, VM vm) {
        IntegerNameProvider<Node> p1 = new IntegerNameProvider<Node>();
        ComponentAttributeProvider<Node> p2 = new ComponentAttributeProvider<Node>() {
            @Override
            public Map<String, String> getComponentAttributes(
                    Node arg0) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("label", arg0.getDisplay_name());
                return map;
            }
        };

        ComponentAttributeProvider<DefaultEdge> p3 = new ComponentAttributeProvider<DefaultEdge>() {
            @Override
            public Map<String, String> getComponentAttributes(DefaultEdge arg0) {
                Map<String, String> map = new HashMap<String, String>();

                if (arg0.getAttributes().equals(Edges.SpawnEdgeAttributes())) {
                    map.put("color", "green");
                } else if (arg0.getAttributes().equals(Edges.JoinEdgeAttributes())) {
                    map.put("color", "red");
                } else if (arg0.getAttributes().equals(Edges.FutureEdgeAttributes())) {
                    map.put("color", "blue");
                } else if (arg0.getAttributes().equals(Edges.IsolatedEdgeAttributes())) {
                    map.put("color", "pink");
                } else if (arg0.getAttributes().equals(Edges.ContinuationEdgeAttributes())) {
                    map.put("color", "black");
                } else if (arg0.getAttributes().equals(Edges.SignalEdgeAttributes())) {
                    map.put("color", "cyan");
                } else if (arg0.getAttributes().equals(Edges.WaitEdgeAttributes())) {
                    map.put("color", "pink");
                } else if (arg0.getAttributes().equals(Edges.PhaseEdgeAttributes())) {
                    map.put("color", "cyan");
                } else {
                    map.put("color", "black");
                }

                return map;
            }
        };

        DOTExporter<Node, DefaultEdge> exporter = new DOTExporter<Node, DefaultEdge>(p1, null, null, p2, p3);
        new File(targetDirectory).mkdirs();
        //TransitiveReduction.closeSimpleDirectedGraph(graph);
        try {
            exporter.export(new FileWriter(targetDirectory + vm.getSUTName() + "-" + ++GRAPH_ITER + ".dot"), graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return GRAPH_ITER;
    }

    static Node searchGraph(String search_id, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        Set<Node> nodes = graph.vertexSet();
        for (Node n : nodes) {
            if (n.id.equals(search_id)) {
                return n;
            }
        }
        return null;
    }

    static void printGraph(DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        Iterator<Node> iter = graph.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next().id);
        }
    }

    static String getObjectId(String str) {
        String[] s = str.split("\\.");
        return s[s.length - 1];
    }

    static String extractObjectName(ElementInfo ei) {
        return getObjectId(ei.toString());
    }

    static String extractThreadName(ThreadInfo ti) {
        return getObjectId(ti.getThreadObject().toString());
    }

    static String extractClassName(Instruction insn) {
        return getObjectId(((FieldInstruction) insn).getClassName());
    }

    static String extractMethodName(MethodInfo mi) {
        return getObjectId(mi.toString());
    }

    static String getThreadName(String ti) {
        boolean end_thread = false;
        String output ="";
        if(ti.contains("-end")){
            end_thread = true;
            ti=ti.split("-end")[0];
        }

        if (threads == null) {
            threads = new HashMap();
            threadCounter = new AtomicInteger(0);
        }
        String properThreadName = ti;
        if (threads.containsKey(properThreadName)) {
            output+= ""+threads.get(properThreadName);
        }
        else{
            Integer newThread = threadCounter.incrementAndGet();
            threads.put(properThreadName, newThread);
            output+= "" + newThread;
        }

        if(end_thread){
            output+="-end";
        }
        return output;
    }

    static String extractCalleeName(ThreadInfo ti) {
        String[] str = ti.getThisElementInfo().toString().split("\\.");
        return str[str.length - 1];
    }

    static boolean isIsolatedMethod(String methodName) {
        return methodName.equals("edu.rice.hj.Module2.isolated");
    }

    static boolean isValidArrayInstruction(Instruction insn, ThreadInfo ti) {
        ElementInfo ei = ((ArrayElementInstruction) insn).peekArrayElementInfo(ti);
        if (!ei.getClassInfo().getName().startsWith("[Ljava")
                && !ei.getClassInfo().getName().startsWith("[Ledu")) {
            return true;
        }
        return false;
    }

    static boolean isLibraryInstruction(Instruction insn) {
        String className = ((FieldInstruction) insn).getClassName();
        return className.startsWith("java") || className.startsWith("hj") ||
            (className.startsWith("edu") &&
             !(className.startsWith("edu.rice.hj.api.HjActor") ||
               className.startsWith("edu.rice.hj.api.HjDataDrivenFuture") ||
               className.startsWith("edu.rice.hj.api.HjFinishAccumulator") ||
               className.startsWith("edu.rice.hj.api.HjFuture") ||
               className.startsWith("edu.rice.hj.api.HjLambda") ||
               className.startsWith("edu.rice.hj.api.HjRunnable") ||
               className.startsWith("edu.rice.hj.api.HjSuspendable") ||
               className.startsWith("edu.rice.hj.api.HjSuspendingCallable")));
    }


    static boolean isValidFieldInstruction(Instruction instructionToExecute, VM vm) {
        return instructionToExecute instanceof FieldInstruction &&
            !isLibraryInstruction(instructionToExecute);
    }

    private static class Pair<X, Y> {
	public final X first;
	public final Y second;
        public Pair(X first, Y second) {
	    this.first = first;
	    this.second = second;
	}
        public String toString() {
            return "[" + this.first.toString() + ", " + this.second.toString() + "]";
        }
        public int hashCode() {
            return this.first.hashCode() + 31 * this.second.hashCode();
        }
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof Pair)) return false;
            Pair p = (Pair) o;
            if (!this.first.equals(p.first)) return false;
            if (!this.second.equals(p.second)) return false;
            return true;
        }

    }
}
