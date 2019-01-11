package se.alipsa.ride.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharsetDetector {

  private static Logger log = LoggerFactory.getLogger(CharsetDetector.class);

  public static Charset detect(byte[] value) {
    return charset(value, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII,
        StandardCharsets.UTF_16, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16BE);
  }

  public static Charset charset(byte[] value, Charset... charsets) {
    // String probe = StandardCharsets.UTF_8.name();
    for(Charset charset : charsets) {
      String content = new String(value, charset);
      if (Arrays.equals(value, content.getBytes())) {
        return charset;
      }
      /*
        if(value.equals(convert(convert(value, charset.name(), probe), probe, charset.name()))) {
          return charset;
      }*/
    }
    return StandardCharsets.UTF_8;
  }

  public static String convert(String value, String fromEncoding, String toEncoding) {
    try {
      return new String(value.getBytes(fromEncoding), toEncoding);
    } catch (UnsupportedEncodingException e) {
      log.warn("Failed to convert string from encoding {}  to {}", fromEncoding, toEncoding, e);
      return value;
    }
  }
}
