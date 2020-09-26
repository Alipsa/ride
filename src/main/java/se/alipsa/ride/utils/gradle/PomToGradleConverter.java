package se.alipsa.ride.utils.gradle;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingException;
import se.alipsa.ride.utils.maven.MavenUtils;

import java.io.File;
import java.util.List;

public class PomToGradleConverter {

    File pomFile;

    public PomToGradleConverter(File pomFile) {
        this.pomFile = pomFile;
    }

    public void convert(File gradleFile) throws ModelBuildingException {
        Model mavenModel = MavenUtils.parsePom(pomFile);
        List<Dependency> dependencies = mavenModel.getDependencies();
        Plugin renjinPlugin = mavenModel.getBuild().getPlugins().stream()
                .filter(p -> p.getArtifactId().equals("renjin-maven-plugin"))
                .findAny().orElse(null);
    }
}
