package [groupId].[lowercaseProjectName];

import java.net.URL;

public class ScriptLoader {

  public static URL getScript() {
    return ScriptLoader.class.getResource("/[fileName]");
  }
}