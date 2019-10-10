package se.alipsa.ride.utils;

import org.apache.logging.log4j.message.FormattedMessageFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  private static final Pattern LEADING_SPACES = Pattern.compile("^\\s+");

  private static FormattedMessageFactory factory = new FormattedMessageFactory();

  public static String format(String text, Object... args) {
    //return org.slf4j.helpers.MessageFormatter.arrayFormat(text, args).getMessage();
    return factory.newMessage(text, args).getFormattedMessage();
  }

  public static String fixedLengthString(String string, int length) {
    return String.format("%1$"+length+ "s", string);
  }

  public static String getLeadingSpaces(String str) {
    Matcher m = LEADING_SPACES.matcher(str);
    if(m.find()){
      return m.group(0);
    }
    return "";
  }
}
