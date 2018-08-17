package hj.runtime.wsh;

import edu.rice.hj.api.HjFinishAccumulator;
import edu.rice.hj.api.HjOperator;
import hj.lang.FinishAccumulatorException;
import hj.lang.accumulator;

import java.util.concurrent.atomic.AtomicInteger;

public class FinishAccumulator extends accumulator implements HjFinishAccumulator {

    private final HjOperator ope;
    private final Class type;
    private final Activity parentTask;
    private final boolean isLazy;
    private final Number initVal;
    private final int func;
    private final double delay;
    private final double coef;
    private Number resultVal;
    private Number resultTmp;
    private final AtomicInteger atomI;

    public FinishAccumulator(HjOperator ope, Class type) throws FinishAccumulatorException {
        this(ope, type, false, 1, 50.0D, 2.0D);
    }

    public FinishAccumulator(HjOperator ope, Class type, boolean isLazy) throws FinishAccumulatorException {
        this(ope, type, isLazy, 1, 50.0D, 2.0D);
    }

    public FinishAccumulator(HjOperator ope, Class type, boolean isLazy, int func, double delay, double coef) throws FinishAccumulatorException {
        throw new UnsupportedOperationException();
    }

    public void setAccessible(boolean isAccessible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Number val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(int val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(double val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number result() {
        throw new UnsupportedOperationException();
    }

    public void calculateAccum() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class getType() {
        return this.type;
    }

    @Override
    public HjOperator getOperator() {
        return this.ope;
    }

    @Override
    public void send(java.lang.Object paramObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(java.lang.Object[] paramArrayOfObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(int[] paramArrayOfInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(double[] paramArrayOfDouble) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(Number paramNumber, int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(int paramInt1, int paramInt2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(double paramDouble, int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number result(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int intResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int intResult(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double doubleResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double doubleResult(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public java.lang.Object objResult() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public java.lang.Object objResult(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] intResultArr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] intResultArr(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double[] doubleResultArr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double[] doubleResultArr(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public java.lang.Object[] objResultArr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public java.lang.Object[] objResultArr(int paramInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getRound() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getArrsize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
