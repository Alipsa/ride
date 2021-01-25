package se.alipsa.ride.model;

public class MuninConnection {
  private String serverName;
  private int serverPort;
  private String userName;
  private String password;

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String target() {
    return "http://" + getServerName() + ":" + getServerPort();
  }

  @Override
  public String toString() {
    return "MuninConnection{" +
        "serverName='" + serverName + '\'' +
        ", serverPort=" + serverPort +
        ", userName='" + userName + '\'' +
        ", password is " + password.length() + " characters long" +
        '}';
  }
}
