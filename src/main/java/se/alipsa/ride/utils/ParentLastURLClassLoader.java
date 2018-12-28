package se.alipsa.ride.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class ParentLastURLClassLoader extends ClassLoader {

  private ChildURLClassLoader childClassLoader;


  public ParentLastURLClassLoader(URL[] classpath, ClassLoader parent) {
    super(parent);
    childClassLoader = new ChildURLClassLoader(classpath, new FindClassClassLoader(this.getParent()));
  }


  public ParentLastURLClassLoader(List<URL> classpath) {
    super(Thread.currentThread().getContextClassLoader());

    URL[] urls = classpath.toArray(new URL[classpath.size()]);

    childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    try {
      // first we try to find a class inside the child classloader
      return childClassLoader.findClass(name);
    } catch (ClassNotFoundException e) {
      // didn't find it, try the parent
      return super.loadClass(name, resolve);
    }
  }

  /**
   * This class allows me to call findClass on a classloader
   */
  private static class FindClassClassLoader extends ClassLoader {
    public FindClassClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
      return super.findClass(name);
    }
  }

  /**
   * This class delegates (child then parent) for the findClass method for a URLClassLoader.
   * We need this because findClass is protected in URLClassLoader
   */
  private static class ChildURLClassLoader extends URLClassLoader {
    private FindClassClassLoader realParent;

    public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
      super(urls, null);

      this.realParent = realParent;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        // first try to use the URLClassLoader findClass
        return super.findClass(name);
      } catch (ClassNotFoundException e) {
        // if that fails, we ask our real parent classloader to load the class (we give up)
        return realParent.loadClass(name);
      }
    }
  }
}
