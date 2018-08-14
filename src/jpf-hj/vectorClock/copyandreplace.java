
const int tidOffset = 48;

void short TID(unsigned long e)
{
    return e >> tidOffset;
}

void read(Varstate x, ThreadState t)
{
    if(x.getR() == t.getEpoch()) return;

    // write-read race?
    if(TID(x.getW()) != t.getTID() && x.getW() > t.getC()[TID(x.getW())]) throw new RunTimeException();

    //TODO: Figure out what READ_SHARED's value should be
    //update read state
    if(x.getR() == READ_SHARED)
    {
        x.setRvc(t.getTID(), t.getEpoch());
    }
    else
    {
        if(x.getR() <= t.getC()[TID(x.getR())])
        {
            x.setR(t.getEpoch());
        }
        else
        {
            if(x.getRvc() == null)
                x.Rvc = new ClockVector();
            
            x.setRvc(TID(x.getR()), x.getR());
            x.setRvc(t.getTID(), t.getEpoch());
            x.setR(READ_SHARED);
        }
    }
}

void write(VarState x, ThreadState t)
{
    if(x.getW() == t.getEpoch()) return;

    // write-write race? // LOOK AT READ, discuss if theres an error in
    // pseudocode in fasttrack paper
    if(TID(x.getW()) != t.getTID() && x.getW() > t.getC()[TID(x.getW())]) throw new RunTimeException();

    // read-write race?

    if(x.getR() != READ_SHARED)
    {
        if(x.getR() > t.getC()[TID(x.getR())]) throw new RunTimeException();
    }
    else
    {   
        //TODO: Figure out what u is and what its supposed to be
        if(x.getRvc()[u] > t.getC()[u] ... for any u) throw new RunTimeException();
    }

    //update write state
    x.setW(t.getEpoch()); 
}
