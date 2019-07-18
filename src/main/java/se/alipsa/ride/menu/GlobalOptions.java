package se.alipsa.ride.menu;

import java.util.HashMap;

public class GlobalOptions extends HashMap<String, Object> {

  public static final String PKG_LOADER = "PackageLoader";
  public static final String REMOTE_REPOSITORIES = "RemoteRepositories";
  public static final String CONSOLE_MAX_LENGTH_PREF = "ConsoleTextArea.MaxLength";
  private static final long serialVersionUID = -4781261903018339389L;


  public int getInt(String key) {
    Object val = get(key);
    if (val instanceof Integer) {
      return (Integer) val;
    } else {
      return Integer.parseInt(val.toString());
    }
  }
}
