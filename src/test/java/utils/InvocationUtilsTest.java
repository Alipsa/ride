package utils;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.utils.GuiUtils;
import se.alipsa.ride.utils.InvocationUtils;

import static org.junit.jupiter.api.Assertions.*;

public class InvocationUtilsTest {

  @Test
  public void testInvocationUtils() {
    assertEquals("utils.InvocationUtilsTest.testInvocationUtils", InvocationUtils.callingMethod(), "stacktrace element == 2 (default) should be the calling method");
    assertEquals("utils.InvocationUtilsTest.testInvocationUtils", testMethod(), "stacktrace element == 3 should be the method called");
    try {
      GuiUtils.addStyle(null, null);
      fail("Expected GuiUtils.addStyle() to throw a RuntimeException, test condition is incorrect");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("utils.InvocationUtilsTest.testInvocationUtils"), "GuiUtils has the wrong stacktrace element number");
    }
  }


  private String testMethod() {
    return InvocationUtils.callingMethod(3);
  }
}
