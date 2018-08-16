package hj.lang;

import edu.rice.hj.api.HjOperator;

/**
 *
 * @author kristophermiles
 */
public abstract class accumulator
        extends Object
        implements ValueType {

    public abstract void send(Number paramNumber);

    public abstract void send(int paramInt);

    public abstract void send(double paramDouble);

    public abstract void send(java.lang.Object paramObject);

    public abstract void send(int[] paramArrayOfInt);

    public abstract void send(double[] paramArrayOfDouble);

    public abstract void send(java.lang.Object[] paramArrayOfObject);

    public abstract void send(Number paramNumber, int paramInt);

    public abstract void send(int paramInt1, int paramInt2);

    public abstract void send(double paramDouble, int paramInt);

    public abstract Number result();

    public abstract Number result(int paramInt);

    public abstract int intResult();

    public abstract int intResult(int paramInt);

    public abstract double doubleResult();

    public abstract double doubleResult(int paramInt);

    public abstract java.lang.Object objResult();

    public abstract java.lang.Object objResult(int paramInt);

    public abstract int[] intResultArr();

    public abstract int[] intResultArr(int paramInt);

    public abstract double[] doubleResultArr();

    public abstract double[] doubleResultArr(int paramInt);

    public abstract java.lang.Object[] objResultArr();

    public abstract java.lang.Object[] objResultArr(int paramInt);

    public abstract Class getType();

    public abstract HjOperator getOperator();

    public abstract int getRound();

    public abstract int getArrsize();

    public static enum Operator {

        SUM, PROD, MIN, MAX, ANY;

        private Operator() {
        }
    }

    public void put(Number val) {
        send(val);
    }

    public void put(int val) {
        send(val);
    }

    public void put(double val) {
        send(val);
    }

    public void put(java.lang.Object obj) {
        send(obj);
    }

    public void put(int[] arr) {
        send(arr);
    }

    public void put(double[] arr) {
        send(arr);
    }

    public void put(java.lang.Object[] arr) {
        send(arr);
    }

    public void put(Number val, int workerId) {
        send(val, workerId);
    }

    public void put(int val, int workerId) {
        send(val, workerId);
    }

    public void put(double val, int workerId) {
        send(val, workerId);
    }

    public Number get() {
        return result();
    }

    public Number get(int offset) {
        return result(offset);
    }

    public int intGet() {
        return intResult();
    }

    public int intGet(int offset) {
        return intResult(offset);
    }

    public double doubleGet() {
        return doubleResult();
    }

    public double doubleGet(int offset) {
        return doubleResult(offset);
    }

    public java.lang.Object objGet() {
        return objResult();
    }

    public java.lang.Object objGet(int offset) {
        return objResult(offset);
    }

    public int[] intGetArr() {
        return intResultArr();
    }

    public int[] intGetArr(int offset) {
        return intResultArr(offset);
    }

    public double[] doubleGetArr() {
        return doubleResultArr();
    }

    public double[] doubleGetArr(int offset) {
        return doubleResultArr(offset);
    }

    public java.lang.Object[] objGetArr() {
        return objResultArr();
    }

    public java.lang.Object[] objGetArr(int offset) {
        return objResultArr(offset);
    }
}
