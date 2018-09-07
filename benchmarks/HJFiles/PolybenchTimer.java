public static class PolybenchTimer {
    private static long start_time = 0;
    private static long stop_time = 0;
    public static void start() {
        start_time = System.currentTimeMillis();
    }
    public static void stop() {
        stop_time = System.currentTimeMillis();
    }

    public static void print() {
        System.out.println(stop_time - start_time);
    }
}
