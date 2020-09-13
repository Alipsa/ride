package se.alipsa.ride.utils.gradle;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.Task;
import org.gradle.tooling.model.idea.IdeaDependency;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaSingleEntryLibraryDependency;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GradleUtils {

    private final GradleConnector connector;

    public GradleUtils(File gradleInstallationDir, File projectDir) {
        connector = GradleConnector.newConnector();
        connector.useInstallation(gradleInstallationDir);
        connector.forProjectDirectory(projectDir);
    }

    public String getGradleVersion() {
        return GradleVersion.current().getVersion();
    }

    public List<String> getGradleTaskNames() {
        List<GradleTask> tasks = getGradleTasks();
        return tasks.stream().map(Task::getName).collect(Collectors.toList());
    }

    public List<GradleTask> getGradleTasks() {
        List<GradleTask> tasks;
        try (ProjectConnection connection = connector.connect()) {
            GradleProject project = connection.getModel(GradleProject.class);
            tasks = new ArrayList<>(project.getTasks());
        }
        return tasks;
    }

    public void buildProject(String... tasks) {
        try(ProjectConnection connection = connector.connect()) {
            BuildLauncher build = connection.newBuild();
            if (tasks.length > 0) {
                build.forTasks(tasks);
            }
            build.run();
        }
    }

    public List<String> getProjectDependencyNames() {
        return getProjectDependencies().stream()
                .map(File::getName)
                .collect(Collectors.toList());

    }

    public List<File> getProjectDependencies() {
        List<File> dependencyFiles = new ArrayList<>();
        try (ProjectConnection connection = connector.connect()) {
            IdeaProject project = connection.getModel(IdeaProject.class);
            for (IdeaModule module : project.getModules()) {
                for (IdeaDependency dependency : module.getDependencies()) {
                    IdeaSingleEntryLibraryDependency ideaDependency = (IdeaSingleEntryLibraryDependency) dependency;
                    File file = ideaDependency.getFile();
                    dependencyFiles.add(file);
                }
            }
        }
        return dependencyFiles;
    }

    public ClassLoader createGradleCLassLoader(ClassLoader parent) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (File f : getProjectDependencies()) {
            urls.add(f.toURI().toURL());
        }
        try (ProjectConnection connection = connector.connect()) {
            IdeaProject project = connection.getModel(IdeaProject.class);
            for (IdeaModule module : project.getModules()) {
                urls.add(module.getCompilerOutput().getOutputDir().toURI().toURL());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]), parent);
    }
}
