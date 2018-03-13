package ru.csc.bdse.util;

/**
 * @author semkagtn
 */
public class Random {

    private Random() {

    }

    private static final java.util.Random random = new java.util.Random();

    public static boolean randomBool() {
        return random.nextBoolean();
    }

    public static int randomInt(int to) {
        return random.nextInt(to);
    }

    public static String randomString() {
        return String.valueOf(random.nextLong());
    }

    public static String nextKey() {
        return String.valueOf(random.nextLong());
    }

    public static byte[] nextValue() {
        return nextKey().getBytes();
    }
}
