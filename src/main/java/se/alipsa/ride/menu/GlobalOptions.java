package se.alipsa.ride.menu;

import se.alipsa.ride.model.Repo;

import java.util.HashMap;
import java.util.List;

public class GlobalOptions extends HashMap<String, Object> {

  public static final String PKG_LOADER = "PackageLoader";
  public static final String REMOTE_REPOSITORIES = "RemoteRepositories";
  public static final String CONSOLE_MAX_LENGTH_PREF = "ConsoleTextArea.MaxLength";
  public static final String MAVEN_HOME = "GlobalOptions.MavenHome";
  public static final String USE_MAVEN_CLASSLOADER = "GlobalOptions.UseMavenClassloader";
  public static final String ADD_BUILDDIR_TO_CLASSPATH = "GlobalOptions.AddBuildDirToClasspath";
  public static final String RESTART_SESSION_AFTER_MVN_RUN = "GlobalOptions.restartSessionAfterMavenBuild";
  public static final String ENABLE_GIT = "GlobalOptions.EnableGit";
  public static final String AUTORUN_GLOBAL = "GlobalOptions.AutoRunGlobal";
  public static final String AUTORUN_PROJECT = "GlobalOptions.AutoRunProject";

  private static final long serialVersionUID = -4781261903018339389L;


  public int getInt(String key) {
    Object val = get(key);
    if (val instanceof Integer) {
      return (Integer) val;
    } else {
      return Integer.parseInt(val.toString());
    }
  }

  public boolean getBoolean(String key) {
    Object val = get(key);
    if (val instanceof Boolean) {
      return (Boolean) val;
    } else {
      return Boolean.parseBoolean(val.toString());
    }
  }

  @SuppressWarnings("unchecked")
  public List<Repo> getRepoList(String key) {
    return (List<Repo>)get(key);
  }
}
