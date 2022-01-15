package utils;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.model.RenjinLibrary;
import se.alipsa.ride.utils.LibraryUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryUtilsTest {

  @Test
  public void testGetGroup() {
    assertEquals("se.alipsa", LibraryUtils.getGroup("se.alipsa:rideutils"));
    assertEquals("", LibraryUtils.getGroup("magrittr"));
  }

  @Test
  public void testGetPackage() {
    assertEquals("rideutils", LibraryUtils.getPackage("se.alipsa:rideutils"));
    assertEquals("magrittr", LibraryUtils.getPackage("magrittr"));
  }

  @Test
  public void testAvailableLibraries() {
    Set<RenjinLibrary> packages = LibraryUtils.getAvailableLibraries();
    assertNotNull(packages, "Available libraries was unexpectedly null");
    assertTrue(packages.size() > 0, "Expected available libraries to contain at least one library");

    packages.forEach(p -> {
      if (p.getFullName().contains(":")) {
        assertEquals(p.getFullName(), LibraryUtils.getGroup(p.getFullName()) + ":" + LibraryUtils.getPackage(p.getFullName()));
      } else {
        // getGroup if no group should return empty string; so we test that
        assertEquals(p.getFullName(), LibraryUtils.getGroup(p.getFullName()) + LibraryUtils.getPackage(p.getFullName()));
      }
    });
  }
}
