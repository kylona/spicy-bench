finish(() -> {
    HjPhaser ph1 = newPhaser(SIG_WAIT);
    for (int i = 0; i < TASKS; i++) {
        // Tasks do work
        next(); //Synchronization with phaser
        // Tasks continue doing work
    }
});
