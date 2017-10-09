package hj.lang;

/**
 * @author Kris
 */
import java.util.Stack;
import java.util.Iterator;

public class MultipleExceptions extends Exception {

    public final Stack exceptions;

    public MultipleExceptions(Stack s) {
        Stack exceptionList = new Stack();
        Iterator it = exceptionList.iterator();
        while (it.hasNext()) {
            Throwable t = (Throwable) it.next();
            if ((t instanceof MultipleExceptions)) {
                MultipleExceptions me = (MultipleExceptions) t;
                exceptionList.addAll(me.exceptions);
            } else {
                exceptionList.add(t);
            }
        }
        this.exceptions = exceptionList;
    }

    @Override
    public String toString() {
        return this.exceptions.toString();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        Iterator it = this.exceptions.iterator();
        while (it.hasNext()) {
            ((Throwable) it.next()).printStackTrace();
        }
    }

}
