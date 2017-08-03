package utils;


public class ExceptionUtils {
    private static final String TAG = "onResponse";

    public static void throwException(String message) {
        throw new IllegalStateException(TAG + " : " + message);
    }
}
