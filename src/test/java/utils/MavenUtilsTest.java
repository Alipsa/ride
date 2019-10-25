package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.repository.LocalRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import se.alipsa.ride.utils.maven.MavenUtils;

import java.io.File;
import org.apache.maven.model.Model;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class MavenUtilsTest {

   static Logger log = LogManager.getLogger();

   File projectPomFile = new File(getClass().getResource("/utils/pom.xml").getFile());

   @Test
   public void testPomParsing() throws Exception {
      log.info("pom file is {}", projectPomFile);

      Model project = MavenUtils.parsePom(projectPomFile);
      assertThat(project.getArtifactId(), equalTo("phone-number"));
      assertThat(project.getProperties().getProperty("maven.compiler.source"), equalTo("1.8"));

      LocalRepository localRepository = MavenUtils.getLocalRepository();

      assertThat(localRepository, is(IsNull.notNullValue()));
      log.info("local Maven repository set to {}", localRepository.getBasedir());

   }

   @Test
   public void testPomClassLoader() throws Exception {
      String className = "com.google.i18n.phonenumbers.Phonenumber";
      try {
         this.getClass().getClassLoader().loadClass(className);
         fail("Loading the class should not have worked");
      } catch (Exception e) {
         assertThat(e, instanceOf(ClassNotFoundException.class));
      }
      ClassLoader cl = MavenUtils.getMavenDependenciesClassloader(projectPomFile);
      Class clazz = cl.loadClass(className);
      log.info("Class resolved to {}", clazz);
   }

}
