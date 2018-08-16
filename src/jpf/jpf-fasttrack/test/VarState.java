
public class VarState
{
    private unsigned long R;
    private unsigned long W;
    private unsigned long Rvc[]; //use iff R == READ_SHARED

    public long getR()
    {
        return R;
    }

    public void setR(long R)
    {
        this.R = R;
    }

    public long getW()
    {
        return W;
    }

    public void setW(long W)
    {
        this.W = W;
    }

    public long[] getRvc()
    {
        return Rvc;
    }

    public void setRvc(int idx, long Rvc)
    {
        this.Rvc[idx] = Rvc;
    }
}