package utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.model.RenjinLibrary;
import se.alipsa.ride.utils.LibraryUtils;

import java.io.IOException;
import java.util.Set;

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
  public void testAvailableLibraries() throws IOException {
    Set<RenjinLibrary> packages = LibraryUtils.getAvailableLibraries(getClass().getClassLoader());
    assertNotNull(packages, "Available libraries was unexpectedly null");
    assertTrue(packages.size() > 0, "Expected available libraries to contain at least one library");

    packages.forEach(p -> {
      //System.out.println(p);
      if (p.getFullName().contains(":")) {
        assertEquals(p.getFullName(), LibraryUtils.getGroup(p.getFullName()) + ":" + LibraryUtils.getPackage(p.getFullName()));
      } else {
        // getGroup if no group should return empty string; so we test that
        assertEquals(p.getFullName(), LibraryUtils.getGroup(p.getFullName()) + LibraryUtils.getPackage(p.getFullName()));
      }
    });
  }
}
