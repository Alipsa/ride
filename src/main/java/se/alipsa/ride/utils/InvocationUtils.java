package se.alipsa.ride.utils;

public final class InvocationUtils {

  public static String callingMethod(int... elementNum) {
    int idx = elementNum.length > 0 ? elementNum[0] : 2;
    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
    StackTraceElement e = stacktrace[idx];
    return e.getClassName() + "." + e.getMethodName();
  }
}
