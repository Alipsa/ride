package se.alipsa.ride.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class MavenUtils {

  public static MavenProject createMavenProject(File pomFile) throws Exception {

    if (pomFile == null || !pomFile.exists()) {
      throw new FileNotFoundException("Pom file " + pomFile + " does not exist");
    }

    MavenXpp3Reader mavenreader = new MavenXpp3Reader();
    FileReader reader = new FileReader(pomFile);
    Model model = mavenreader.read(reader);
    model.setPomFile(pomFile);
    MavenProject project = new MavenProject(model);
    return project;
  }

  public static ClassLoader getMavenClassLoader(MavenProject project) throws Exception {
    List<String> classpathElements = project.getCompileClasspathElements();
    classpathElements.add(project.getBuild().getOutputDirectory());
    classpathElements.add(project.getBuild().getTestOutputDirectory());
    URL[] urls = new URL[classpathElements.size()];
    for (int i = 0; i < classpathElements.size(); ++i) {
      urls[i] = new File(classpathElements.get(i)).toURI().toURL();
    }
    return new URLClassLoader(urls, MavenUtils.class.getClassLoader());
  }

  public static ClassLoader getMavenDependenciesClassloader(File pomFile) throws Exception {
    return getMavenClassLoader(createMavenProject(pomFile));
  }
}
