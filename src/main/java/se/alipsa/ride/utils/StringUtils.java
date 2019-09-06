package se.alipsa.ride.utils;

import org.apache.logging.log4j.message.FormattedMessageFactory;

public class StringUtils {

  private static FormattedMessageFactory factory = new FormattedMessageFactory();

  public static String format(String text, Object... args) {
    //return org.slf4j.helpers.MessageFormatter.arrayFormat(text, args).getMessage();
    return factory.newMessage(text, args).getFormattedMessage();
  }

  public static String fixedLengthString(String string, int length) {
    return String.format("%1$"+length+ "s", string);
  }
}
