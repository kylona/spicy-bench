// Wait Method
while (waiterPhase >= phase) {
    phaseCondition.await();
}
waiterPhase++;

//Signal Method
phaser.signal();
if (allSignalersSignaled) {
    phase++;
    phaseCondition.signalAll();
}
