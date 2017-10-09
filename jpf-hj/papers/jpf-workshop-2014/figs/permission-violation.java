// Permission Violation
public void race(Stack X) {
    // Task A
    async(() -> {
        acquireW(X);
        X.push(5);
        releaseW(X);
    };

    // Task B
    async(() -> {
        acquireR(X);
        X.peek();
        releaseR(X);
    };
}
