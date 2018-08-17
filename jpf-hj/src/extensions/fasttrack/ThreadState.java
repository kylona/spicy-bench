
public class ThreadState
{
    private int tid;
    private long C[];
    private long epoch; //invariant: epoch == C[tid]


    public int getTID()
    {
        return tid;       
    }

    public void setTID(int tid)
    {
        this.tid = tid;
    }

    public long[] getC
    {
        return C;
    }

    public void setC(int idx, long vc)
    {
        this.C[idx] = vc;
    }

    public long getEpoch()
    {
        return epoch;
    }

    public void setEpoch(long epoch)
    {
        this.epoch = epoch;
    }
}