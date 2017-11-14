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

    public static boolean analyzeFinishBlock(DirectedAcyclicGraph<Node, DefaultEdge> graph, String finishID, boolean otf) {
        List<activityNode> tasks = getTasksFromFinishBlock(finishID, graph);

        for (int i = 0; i < tasks.size() - 1; i++) {
            if (tasks.get(i).var_read != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).var_write != null) {
                        if (DetectDataRace(tasks.get(i).var_read, tasks.get(j).var_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).isolated_write != null) {
                        if (DetectDataRace(tasks.get(i).var_read, tasks.get(j).isolated_write)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).var_write != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).var_read != null) {
                        if (DetectDataRace(tasks.get(i).var_write, tasks.get(j).var_read)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).var_write != null) {
                        if (DetectDataRace(tasks.get(i).var_write, tasks.get(j).var_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).isolated_read != null) {
                        if (DetectDataRace(tasks.get(i).var_write, tasks.get(j).isolated_read)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).isolated_write != null) {
                        if (DetectDataRace(tasks.get(i).var_write, tasks.get(j).isolated_write)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).isolated_read != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).var_write != null) {
                        if (DetectDataRace(tasks.get(i).isolated_read, tasks.get(j).var_write)) {
                            return true;
                        }
                    }
                    //intended race with isolated write
                    if (tasks.get(j).isolated_write != null) {
                        if (DetectDataRaceIsolated(tasks.get(i).isolated_read, tasks.get(j).isolated_write)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).isolated_write != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).var_write != null) {
                        if (DetectDataRace(tasks.get(i).isolated_write, tasks.get(j).var_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).var_read != null) {
                        if (DetectDataRace(tasks.get(i).isolated_write, tasks.get(j).var_read)) {
                            return true;
                        }
                    }
                    //intended race with isolated write
                    if (tasks.get(j).isolated_write != null) {
                        if (DetectDataRaceIsolated(tasks.get(i).isolated_write, tasks.get(j).isolated_write)) {
                            return true;
                        }
                    }
                    //intended race with isolated read
                    if (tasks.get(j).isolated_read != null) {
                        if (DetectDataRaceIsolated(tasks.get(i).isolated_write, tasks.get(j).isolated_read)) {
                            return true;
                        }
                    }
                }
            }

            if (tasks.get(i).array_read != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).array_write != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_read, tasks.get(j).array_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).array_write_isolated != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_read, tasks.get(j).array_write_isolated)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).array_write != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).array_read != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write, tasks.get(j).array_read)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).array_write != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write, tasks.get(j).array_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).array_read_isolated != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write, tasks.get(j).array_read_isolated)) {
                            return true;
                        }

                    }
                    if (tasks.get(j).array_write_isolated != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write, tasks.get(j).array_write_isolated)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).array_read_isolated != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }
                    if (tasks.get(j).array_write != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_read_isolated, tasks.get(j).array_write)) {
                            return true;
                        }
                    }
                    //intended race with isolated write array
                    if (tasks.get(j).array_write_isolated != null) {
                        if (DetectDataRaceArrayIsolated(tasks.get(i).array_read_isolated, tasks.get(j).array_write_isolated)) {
                            return true;
                        }
                    }
                }
            }
            if (tasks.get(i).array_write_isolated != null) {
                for (int j = i + 1; j < tasks.size(); j++) {
                    if ((tasks.get(i).ti != null && tasks.get(j).ti != null && tasks.get(j).ti.equals(tasks.get(i).ti))
                            || checkSerialNodes(tasks.get(i), tasks.get(j), graph)) {
                        continue;
                    }

                    if (tasks.get(j).array_write != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write_isolated, tasks.get(j).array_write)) {
                            return true;
                        }
                    }
                    if (tasks.get(j).array_read != null) {
                        if (DetectDataRaceArray(tasks.get(i).array_write_isolated, tasks.get(j).array_read)) {
                            return true;
                        }
                    }
                    //intended race with isolated write array
                    if (tasks.get(j).array_write_isolated != null) {
                        if (DetectDataRaceArrayIsolated(tasks.get(i).array_write_isolated, tasks.get(j).array_write_isolated)) {
                            return true;
                        }
                    }
                    //intended race with isolated read array
                    if (tasks.get(j).array_read_isolated != null) {
                        if (DetectDataRaceArrayIsolated(tasks.get(i).array_write_isolated, tasks.get(j).array_read_isolated)) {
                            return true;
                        }
                    }
                }
            }
        }
        Node fin = searchGraph(finishID, graph);
        if (otf) {
            createMasterNode((finishNode) fin, graph);
        }
        //createMasterNode((finishNode) fin, graph);
        return false;
    }

    public static boolean DetectDataRaceArray(List<ArrayElements> var1, List<ArrayElements> var2) {
        for (ArrayElements ae1 : var1) {
            for (ArrayElements ae2 : var2) {
                if (ae1.ei.toString().equals(ae2.ei.toString()) && (ae1.idx == ae2.idx)) {
                    System.out.print("race for array element ");
                    System.out.print(ae1.ei);
                    System.out.print("[");
                    System.out.print(ae1.idx);
                    System.out.println("]");
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean DetectDataRace(List<Elements> var1, List<Elements> var2) {

        for (Elements ae1 : var1) {
            for (Elements ae2 : var2) {
                if (ae1.ei.toString().equals(ae2.ei.toString()) && ae1.fi.toString().equals(ae2.fi.toString())) {
                    System.out.print("race for field ");
                    System.out.print(ae1.ei);
                    System.out.print('.');
                    System.out.println(ae1.fi.getName());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean DetectDataRaceIsolated(List<Elements> var1, List<Elements> var2) {

        for (Elements ae1 : var1) {
            for (Elements ae2 : var2) {
              System.out.println("AE2 outside Loop: " + ae2.ei);
                if (ae1.ei.toString().equals(ae2.ei.toString()) && ae1.fi.toString().equals(ae2.fi.toString())) {
                    numReports++;
                    System.out.print("(" + numReports + ")Intended race for field ");
                    System.out.print(ae1.ei);
                    System.out.print('.');
                    System.out.println(ae1.fi.getName());
                    //return true;
                }
            }
        }
        return false;
    }

    public static boolean DetectDataRaceArrayIsolated(List<ArrayElements> var1, List<ArrayElements> var2) {
        for (ArrayElements ae1 : var1) {
            for (ArrayElements ae2 : var2) {
                if (ae1.ei.toString().equals(ae2.ei.toString()) && (ae1.idx == ae2.idx)) {
                    System.out.print("Intended race for array element ");
                    System.out.print(ae1.ei);
                    System.out.print("[");
                    System.out.print(ae1.idx);
                    System.out.println("]");
                    //return true;
                }
            }
        }
        return false;
    }

    static void createGraph(DirectedAcyclicGraph<Node, DefaultEdge> graph, String targetDirectory, VM vm, String addToName) {
        IntegerNameProvider<Node> p1 = new IntegerNameProvider<Node>();
        ComponentAttributeProvider<Node> p2 = new ComponentAttributeProvider<Node>() {
            @Override
            public Map<String, String> getComponentAttributes(Node arg0) {
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
            exporter.export(new FileWriter(targetDirectory + vm.getSUTName() + "-" + ++GRAPH_ITER + "-" + addToName + ".dot"), graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkSerialNodes(Node n1, Node n2, DirectedAcyclicGraph<Node, DefaultEdge> graph) {

        for (DefaultEdge edge : graph.outgoingEdgesOf(n1)) {
            Node n = graph.getEdgeTarget(edge);
            if (n2.equals(n)) {
                return true;
            }
            if (check(n1, n2, graph.outgoingEdgesOf(n), graph)) {
                return true;
            }
        }

        return false;
    }

    private static boolean check(Node n1, Node n2, Set<DefaultEdge> outGoingEdges, DirectedAcyclicGraph<Node, DefaultEdge> graph) {

        for (DefaultEdge edge : outGoingEdges) {
            Node n = graph.getEdgeTarget(edge);
            if (n2.equals(n)) {
                return true;
            }
            if (check(n1, n2, graph.outgoingEdgesOf(n), graph)) {
                return true;
            }
        }
        return false;
    }

    //public static void registerHeapAccess(activityNode n, Elements var, Boolean insnIsRead, Boolean Isolated){
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

    //public static void registerHeapAccessArray(activityNode n1, ArrayElements ae, Boolean insnIsRead, Boolean Isolated){
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

    public static void copy(List<Elements> source, List<Elements> Target) {
        if (Target == null) {
            Target = new LinkedList<Elements>();
        }
        Target.addAll(source);
    }

    public static void copyArray(List<ArrayElements> source, List<ArrayElements> Target) {
        if (Target == null) {
            Target = new LinkedList<ArrayElements>();
        }
        Target.addAll(source);
    }

    public static void createIsolatedEdges(List<isolatedNode> nodes, DirectedAcyclicGraph<Node, DefaultEdge> graph) {
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = 1; j < nodes.size(); j++) {
                addIsolatedEdge(nodes.get(i), nodes.get(j), graph);
            }
        }
    }

    static List<activityNode> getTasksFromFinishBlock(String finID, DirectedAcyclicGraph<Node, DefaultEdge> graph) {

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

    static boolean isValidFieldInstruction(Instruction instructionToExecute, VM vm) {
        String class_name = extractClassName(instructionToExecute);
        String insn = instructionToExecute.toString();

        if ((instructionToExecute instanceof InstanceFieldInstruction
                && !insn.contains("java") && !insn.contains("hj") && !insn.contains("edu") && !class_name.contains("$"))
                || (instructionToExecute instanceof StaticFieldInstruction
                && insn.toString().split(" ")[1].startsWith(vm.getSUTName()))) {
            return true;
        }
        return false;
    }
}
