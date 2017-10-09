// Write GPR
public Node pop() {
    acquireW(this);
    Node temp = this.pop();
    releaseW(this);
    return temp;
}

// Read GPR
public void empty() {
    acquireR(this);
    boolean ret = (size > 0) ? false : true;
    releaseR(this);
    return ret;
}
