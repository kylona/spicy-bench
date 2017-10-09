package extensions;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.VM;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateGraphListener extends ListenerAdapter {

    /**
     * The following array stores the packages you wish to ignore when graphing
     * the state space!
     */
    private static final String[] invalidText = {"edu/rice", "hj/util/", "hj/lang/"};
    private static final String[] systemLibrary = {"java/util/", "java/runtime/", "java/lang/", "null", "hj/runtime/wsh/"};

    static final int RECTANGLE = 1;
    static final int ELLIPSE = 2;
    static final int ROUND_RECTANGLE = 3;
    static final int RECTANGLE_WITH_TEXT = 4;
    static final int ELLIPSE_WITH_TEXT = 5;
    static final int ROUND_RECTANGLE_WITH_TEXT = 6;
    private static boolean transition_numbers = false;
    private static boolean show_source = false;

    private HashMap<Integer, String> transitions;
    private BufferedWriter graph = null;
    private int edge_id = 0;
    private String out_filename = "-hj-ss.dot";
    ArrayList<String> gdfEdges = new ArrayList();

    private StateInformation prev_state = null;
    private static final String INVALID_SELECTION_TEXT = "INVALID_SOURCE_SELECTION";

    public StateGraphListener(Config conf, JPF jpf) {
        VM vm = jpf.getVM();
        String delims = "[ .]+";
        String[] parts = vm.getSUTName().split(delims);
        out_filename = parts[parts.length - 1] + out_filename;
        vm.recordSteps(true);
        transitions = new HashMap();
    }

    @Override
    public void searchStarted(Search search) {
        try {
            beginGraph();
        } catch (IOException ex) {
            Logger.getLogger(StateGraphListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void searchFinished(Search search) {
        try {
            endGraph();
        } catch (IOException ex) {
            Logger.getLogger(StateGraphListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        int id = search.getStateId();
        boolean has_next = search.hasNextState();
        boolean is_new = search.isNewState();
        try {
            if (this.prev_state != null) {
                addEdge(this.prev_state.id, id, search);
            } else {
                this.prev_state = new StateInformation();
            }
            addNode(this.prev_state, search);
            this.prev_state.reset(id, has_next, is_new);
        } catch (IOException localIOException) {
        }
    }

    @Override
    public void stateRestored(Search search) {
        this.prev_state.reset(search.getStateId(), false, false);
    }

    @Override
    public void stateProcessed(Search search) {
    }

    @Override
    public void stateBacktracked(Search search) {
        try {
            addNode(this.prev_state, search);
            this.prev_state.reset(search.getStateId(), false, false);
        } catch (IOException localIOException) {
        }
    }

    @Override
    public void searchConstraintHit(Search search) {
    }

    private String getErrorMsg(Search search) {
        List errs = search.getErrors();
        if (errs.isEmpty()) {
            return null;
        }
        return ((Error) errs.get(0)).getDescription();
    }

    @Override
    public void propertyViolated(Search search) {
        this.prev_state.error = getErrorMsg(search);
    }

    private void beginGraph() throws IOException {

        this.graph = new BufferedWriter(new FileWriter(this.out_filename));
        this.graph.write("digraph hj_state_space {");
        this.graph.newLine();
    }

    private void endGraph()
            throws IOException {
        if (this.prev_state != null) {
            addNode(this.prev_state, null);
        }

        this.graph.write("}");
        this.graph.newLine();

        this.graph.close();
    }

    private String makeDotLabel(Search state, int my_id) {
        Transition trans = state.getTransition();

        Step last_trans_step = trans.getLastStep();
        if (last_trans_step == null) {
            return "?";
        }

        StringBuilder result = new StringBuilder();

        if (transition_numbers) {
            result.append(my_id);
            result.append("\\n");
        }

        int thread = trans.getThreadIndex();

        result.append("Thd");
        result.append(thread);
        result.append(':');
        result.append(getFinalLineText(trans));

        if (show_source) {
            String source_line = last_trans_step.getLineString();
            if ((source_line != null) && (!source_line.equals(""))) {
                result.append("\\n");

                StringBuilder sb = new StringBuilder(source_line);

                replaceString(sb, "\n", "");
                replaceString(sb, "]", "\\]");
                replaceString(sb, "\"", "\\\"");
                result.append(sb.toString());
            }
        }

        return result.toString();
    }

    private String getFinalLineText(Transition trans) {
        ArrayList<String> steps = new ArrayList<>();
        for (int i = 0; i < trans.getStepCount(); i++) {
            steps.add(trans.getStep(i).toString());
        }
        for (int i = steps.size() - 1; i >= 0; i--) {
            String testValue;
            try {
                testValue = steps.get(i);
            } catch (NullPointerException e) {
                testValue = INVALID_SELECTION_TEXT;
            }
            boolean passed = true;
            for (String s : systemLibrary) {
                if (testValue.contains(s)) {
                    passed = false;
                }
            }
            for (String s : invalidText) {
                if (testValue.contains(s)) {
                    passed = false;
                }
            }
            if (passed) {
                return testValue;
            }
        }
        for (int i = steps.size() - 1; i >= 0; i--) {

            String testValue = steps.get(i);
            boolean passed = true;
            for (String s : systemLibrary) {
                if (testValue.contains(s)) {
                    passed = false;
                }
            }
            if (passed) {
                return testValue;
            }
        }
        return "INVALID SOURCE ONLY ";
    }

    private StringBuilder replaceString(StringBuilder original, String from, String to) {
        int indexOfReplaced = 0;
        int lastIndexOfReplaced = 0;
        while (indexOfReplaced != -1) {
            indexOfReplaced = original.indexOf(from, lastIndexOfReplaced);
            if (indexOfReplaced != -1) {
                original.replace(indexOfReplaced, indexOfReplaced + 1, to);
                lastIndexOfReplaced = indexOfReplaced + to.length();
            }
        }
        return original;
    }

    private void addNode(StateInformation state, Search search)
            throws IOException {
        if (state.is_new) {
            this.graph.write(makeChoiceNode(state, search));

            this.graph.write("\",shape=");
            if (state.error != null) {
                this.graph.write("diamond,color=red");
            } else if (state.has_next) {
                this.graph.write("circle,color=black");
            } else {
                this.graph.write("egg,color=green");
            }
            this.graph.write("];");

            this.graph.newLine();
        }
    }

    private String makeChoiceNode(StateInformation state, Search search) {
        //System.out.println("Adding a node: "+state.id);
        StringBuilder output = new StringBuilder();
        output.append("  st");
        output.append(state.id);
        output.append(" [label=\"");

        //Now we need to get the choice generator.
        String choice = " CGN: " + state.id + "\n";
        if (search != null) {
            choice += search.getTransition().getChoiceGenerator().getId();

        }
        output.append(choice);
        return output.toString();
    }

    private void addEdge(int old_id, int new_id, Search state)
            throws IOException {
        int my_id = this.edge_id++;
        this.graph.write(new StringBuilder().append("  st").append(old_id).append(" -> tr").append(my_id).append(";").toString());
        this.graph.newLine();
        this.graph.write(new StringBuilder().append("  tr").append(my_id).append(" [label=\"").append(makeDotLabel(state, my_id)).append("\",shape=box]").toString());

        this.graph.newLine();
        this.graph.write(outgoingEdge(state, my_id, new_id));
        this.graph.newLine();
        this.graph.newLine();
    }

    private String outgoingEdge(Search state, int my_id, int new_id) {
        StringBuilder output = new StringBuilder();
        output.append("  tr").append(my_id).append(" -> st").append(new_id);
        String color = null;
        if (state.getTransition().getChoiceGenerator().getNextChoice().toString().contains("TERMINATE")) {
            color = "red";
        }
        if (color != null) {
            output.append(" [ color= \"").append(color).append(" \" ]");
        }
        return output.append(";").toString();
    }

    void filterArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                String arg = args[i];
                if ("-transition-numbers".equals(arg)) {
                    transition_numbers = true;
                    args[i] = null;
                } else if ("-show-source".equals(arg)) {
                    show_source = true;
                    args[i] = null;
                } else if ("-gdf".equals(arg)) {

                    this.out_filename = "hj-state-space.gdf";
                    args[i] = null;
                } else if ("-labelvisible".equals(arg)) {
                    args[i] = null;
                } else if ("-help".equals(args[i])) {
                }
            }
        }
    }

    private static class StateInformation {

        int id = -1;
        boolean has_next = true;
        String error = null;
        boolean is_new = false;

        public void reset(int id, boolean has_next, boolean is_new) {
            this.id = id;
            this.has_next = has_next;
            this.error = null;
            this.is_new = is_new;
        }
    }
}
