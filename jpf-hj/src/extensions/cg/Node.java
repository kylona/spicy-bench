package extensions.cg;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.LinkedList;
import java.util.List;

public class Node {
    static int nodeCount = 0;
    public int nodeId;
    ThreadInfo ti = null;
    String id = null;
    Node next = null;
    private boolean isAsync = false;
    private boolean isJoin = false;
    private boolean isIsolated = false;
    public boolean hasOutgoingIsolationEdge = false;
    public boolean hasIncomingIsolationEdge = false;
    private int index = -1;
    private boolean readyForJoin = false;

    public void setReadyForJoin(boolean input) {
        this.readyForJoin = input;
    }

    public boolean isReadyForJoin() {
        return this.readyForJoin;
    }

    public boolean hasOutgoingIsolationEdge() {
        return hasOutgoingIsolationEdge;
    }

    public void setOutgoingIsolationEdge(boolean input) {
        this.hasOutgoingIsolationEdge = input;
    }

    public boolean hasIncomingIsolationEdge() {
        return hasIncomingIsolationEdge;
    }

    public void setIncomingIsolationEdge(boolean input) {
        this.hasIncomingIsolationEdge = input;
    }
    public void setIndex(int input)  {
        this.index = input;
    }

    public int getIndex() {
        return index;
    }

    private int joinEdgesEvaluated = 0;

    public int getJoinEdgesEvaluated()
    {
        return this.joinEdgesEvaluated;
    }

    public void incJoinEdgesEvaluated()
    {
        this.joinEdgesEvaluated++;
    }

    private String display_name = null;


    public String getDisplay_name() {
        //return display_name;
        return String.valueOf(nodeId);
    }

    public String toString() {
        return String.valueOf(nodeId);
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public Node(String name, ThreadInfo t) {
        ti = t;
        id = name;
        nodeId = nodeCount++;
    }

    public Node(String name) {
        id = name;
        nodeId = nodeCount++;
    }

    public void setThreadInfo(ThreadInfo t) {
        ti = t;
    }

    public void setNextNode(Node n) {
        next = n;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean newAsync) {
        isAsync = newAsync;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public void setJoin(boolean newJoin) {
        isJoin = newJoin;
    }

    public boolean isIsolated() {
        return isIsolated;
    }

    public void setIsolated(boolean newIsolated) {
        isIsolated = newIsolated;
    }


}

abstract class DataAccess {
    boolean isolated = false;
    public boolean isIsolated() {
        return isolated;
    }
    public void setIsolated(boolean isolated) {
    this.isolated = isolated;
    }

    abstract public boolean conflictsWith(DataAccess da);
}
class ArrayElements extends DataAccess {

    ElementInfo ei;
    int idx;
    String filePos;

    public ElementInfo getElementInfo() {
        return ei;
    }

    public void setElementInfo(ElementInfo ei) {
        this.ei = ei;
    }

    public int getIndex() {
        return idx;
    }

    public void setIndex(int index) {
        this.idx = index;
    }

    public ArrayElements(ElementInfo ei, int idx, String filePos) {
        this.ei = ei;
        this.idx = idx;
	this.filePos = filePos;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof ArrayElements)) return false;
        ArrayElements ae = (ArrayElements) o;
        if (!this.ei.toString().equals(ae.ei.toString())) return false;
        if (this.idx != ae.idx) return false;
        return true;
    }

    public String toString() {
        return "array ref on index " + this.idx + " @ " + this.filePos.toString();
    }
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean conflictsWith(DataAccess da) {
        return this.equals(da); 
    }
}

class Elements extends DataAccess {

    ElementInfo ei;
    FieldInfo fi;
    String filePos;

    public ElementInfo getElementInfo() {
        return ei;
    }

    public void setElementInfo(ElementInfo ei) {
        this.ei = ei;
    }

    public FieldInfo getFieldInfo() {
        return fi;
    }

    public void setIndex(FieldInfo fi) {
        this.fi = fi;
    }

    public Elements(ElementInfo ei, FieldInfo fi, String filePos) {
        this.ei = ei;
        this.fi = fi;
	this.filePos = filePos;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Elements)) return false;
        Elements e = (Elements) o;
        if (!this.ei.toString().equals(e.ei.toString())) return false;
        if (!this.fi.toString().equals(e.fi.toString())) return false;
        return true;
    }
    public String toString() {
        return this.fi.getName() + " @ " + this.filePos.toString();
    }
    public int hashCode() {
        return this.toString().hashCode();

    }
    public boolean conflictsWith(DataAccess da) {
        return this.equals(da); 
    }
}

class finishNode extends Node {

    public finishNode(String name, ThreadInfo t) {
        super(name, t);
    }
}

class activityNode extends Node {

    List<Elements> var_read = null;
    List<Elements> var_write = null;
    List<Elements> isolated_read = null;
    List<Elements> isolated_write = null;

    List<ArrayElements> array_write = null;
    List<ArrayElements> array_read = null;
    List<ArrayElements> array_write_isolated = null;
    List<ArrayElements> array_read_isolated = null;

    public activityNode(String name) {
        super(name);
    }

    public activityNode(String name, ThreadInfo ti) {
        super(name, ti);
    }

    public void createReadList() {
        var_read = new LinkedList<Elements>();
    }

    public void createWriteList() {
        var_write = new LinkedList<Elements>();
    }

    public void createIsolatedReadList() {
        isolated_read = new LinkedList<Elements>();
    }

    public void createIsolatedWriteList() {
        isolated_write = new LinkedList<Elements>();
    }

    public void createArrayReadList() {
        array_read = new LinkedList<ArrayElements>();
    }

    public void createArrayWriteList() {
        array_write = new LinkedList<ArrayElements>();
    }

    public void createArrayIsolatedReadList() {
        array_read_isolated = new LinkedList<ArrayElements>();
    }

    public void createArrayIsolatedWriteList() {
        array_write_isolated = new LinkedList<ArrayElements>();
    }

    public String getDisplay_name() {
        StringBuilder sb = new StringBuilder();
        if (var_write == null) return super.getDisplay_name();
        for (DataAccess write : var_write) {
            sb.append(write.toString() + "\n");
        }
        //return sb.toString();
        return String.valueOf(nodeId);
    }
}

class isolatedNode extends activityNode {

    public isolatedNode(String name, ThreadInfo t) {
        super(name, t);
    }
}

class futureNode extends activityNode {

    public futureNode(String name) {
        super(name);
    }

    public futureNode(String name, ThreadInfo ti) {
        super(name, ti);
    }
}

class phaserNode extends Node {

    public phaserNode(String name) {
        super(name);
    }

    public phaserNode(String name, ThreadInfo ti) {
        super(name, ti);
    }
}
