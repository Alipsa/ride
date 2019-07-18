package se.alipsa.ride.menu;

import javafx.util.StringConverter;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.primitives.packaging.ClasspathPackageLoader;

public class PackageLoaderClassConverter extends StringConverter<Class<?>> {

  @Override
  public String toString(Class<?> object) {
    return object.getSimpleName();
  }

  @Override
  public Class<?> fromString(String className) {
    if (AetherPackageLoader.class.getSimpleName().equals(className)) {
      return AetherPackageLoader.class;
    }
    if (ClasspathPackageLoader.class.getSimpleName().equals(className)) {
      return ClasspathPackageLoader.class;
    }
    return null;
  }
}
