package se.alipsa.ride.utils;

public class StringUtils {

  public static String format(String text, Object... args) {
    return org.slf4j.helpers.MessageFormatter.arrayFormat(text, args).getMessage();
  }

  public static String fixedLengthString(String string, int length) {
    return String.format("%1$"+length+ "s", string);
  }
}
