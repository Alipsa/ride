package se.alipsa.ride;

import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class Constants {

  public static final String SESSION_MUNIN_CONNECTION = "MuninConnection";
  public static final String DEFAULT_MDR_REPORT_NAME = "Mdr Report";
  public static final String DEFAULT_R_REPORT_NAME = "R Report";

  public static final String PREF_MUNIN_SERVER = "MuninConnection.serverName";
  public static final String PREF_MUNIN_PORT = "MuninConnection.serverPort";
  public static final String PREF_MUNIN_USERNAME = "MuninConnection.userName";

  public static final int ICON_HEIGHT = 20;
  public static final int ICON_WIDTH = 20;

  public static final int HGAP = 5;
  public static final int VGAP = 5;
  public static final Insets FLOWPANE_INSETS = new Insets(5, 10, 5, 5);

  public static final KeyCodeCombination KEY_CODE_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);

  public static final String INDENT = "  ";

  public static final String THEME = "theme";
  public static final String DARK_THEME = "darkTheme.css";
  public static final String BRIGHT_THEME = "brightTheme.css";
  public static final String BLUE_THEME = "blueTheme.css";

  public static final String DRV_POSTGRES = "org.postgresql.Driver";
  public static final String DRV_MYSQL = "com.mysql.jdbc.Driver";
  public static final String DRV_MARIADB = "org.mariadb.jdbc.Driver";
  public static final String DRV_H2 = "org.h2.Driver";
  public static final String DRV_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  public static final String DRV_SQLLITE = "org.sqlite.JDBC";
  public static final String DRV_FIREBIRD = "org.firebirdsql.jdbc.FBDriver";
  public static final String DRV_DERBY = "org.apache.derby.jdbc.ClientDriver";
  public static final String DRV_ORACLE = "oracle.jdbc.OracleDriver";
  public static final String PREF_LAST_EXPORT_DIR = "last.export.dir";

  public enum GitStatus {
    GIT_ADDED("-fx-text-fill: #629755;"),
    GIT_UNTRACKED("-fx-text-fill: sienna"),
    GIT_CHANGED("-fx-text-fill: #6897BB"),
    GIT_CONFLICT("-fx-text-fill: red"),
    GIT_IGNORED("-fx-text-fill: grey"),
    GIT_MODIFIED("-fx-text-fill: blue"),
    GIT_UNCOMITTED_CHANGE("-fx-text-fill:  #8AA4C8"),
    GIT_NONE("");

    private final String style;
    GitStatus(String style) {
      this.style = style;
    }
    public String getStyle() {
      return style;
    }
  }

  public static final String REPORT_BUG = "Please report this bug to https://github.com/Alipsa/ride/issues!";
}
