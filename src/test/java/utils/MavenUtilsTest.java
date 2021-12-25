package utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.repository.LocalRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.maven.MavenUtils;

import java.io.File;
import java.io.IOException;

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
   public void testBomDependency() throws IOException, SettingsBuildingException, ModelBuildingException, XmlPullParserException {
      File pomFile = FileUtils.getResource("utils/pom_bom.xml");
      Model project = MavenUtils.parsePom(pomFile);
      Dependency dep = project.getDependencies().stream().filter(
          a -> "org.junit.jupiter".equals(a.getGroupId()) && "junit-jupiter".equals(a.getArtifactId()))
          .findAny()
          .orElse(null);
      assertNotNull(dep, "Failed to resolve junit dependency");
      assertEquals("org.junit.jupiter:junit-jupiter:5.8.2", dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
   }

   @Test
   public void testPomWithParent() throws IOException, SettingsBuildingException, ModelBuildingException, XmlPullParserException {
      File pomFile = FileUtils.getResource("utils/pom_parent.xml");
      Model project = MavenUtils.parsePom(pomFile);
      Dependency dep = project.getDependencies().stream().filter(
              a -> "org.junit.jupiter".equals(a.getGroupId()) && "junit-jupiter-api".equals(a.getArtifactId()))
          .findAny()
          .orElse(null);
      assertNotNull(dep, "Failed to resolve junit dependency");
      assertEquals("org.junit.jupiter:junit-jupiter-api:5.8.2", dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());

      dep = project.getDependencies().stream().filter(
              a -> "org.springframework.boot".equals(a.getGroupId()) && "spring-boot-starter-test".equals(a.getArtifactId()))
          .findAny()
          .orElse(null);
      assertNotNull(dep, "Failed to resolve spring boot dependency");
      assertEquals("org.springframework.boot:spring-boot-starter-test:2.6.1", dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
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
      ClassLoader cl = MavenUtils.getMavenDependenciesClassloader(projectPomFile, getClass().getClassLoader());
      Class<?> clazz = cl.loadClass(className);
      log.info("Class resolved to {}", clazz);
   }

   @Test
   public void testSessionWithMavenClassLoader() throws Exception {
      ClassLoader cl = MavenUtils.getMavenDependenciesClassloader(projectPomFile, getClass().getClassLoader());
      ClasspathPackageLoader loader = new ClasspathPackageLoader(cl);
      SessionBuilder builder = new SessionBuilder();
      Session session = builder
          .withDefaultPackages()
          .setPackageLoader(loader)
          .setClassLoader(cl)
          .build();
      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
      RenjinScriptEngine engine = factory.getScriptEngine(session);
      assertThat(engine.eval("print('Hello World')") + "", equalTo("Hello World"));

      StringBuilder str = new StringBuilder("import(com.google.i18n.phonenumbers.PhoneNumberUtil)\n")
          .append("numUtil <- PhoneNumberUtil$getInstance()\n")
          .append("number <- numUtil$parseAndKeepRawInput('+46701234567', 'SE')\n")
          .append("numUtil$isValidNumber(number)");

      SEXP result = (SEXP)engine.eval(str.toString());
      assertThat(result.asLogical().toBooleanStrict(), equalTo(true));
   }

}
