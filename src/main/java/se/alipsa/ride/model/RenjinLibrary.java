package se.alipsa.ride.model;

import org.jetbrains.annotations.NotNull;

public class RenjinLibrary implements Comparable<RenjinLibrary>{
  private String title;
  private String group;
  private String version;
  private String packageName;
  private boolean loaded;

  public RenjinLibrary() {}

  public RenjinLibrary(String title, String group, String packageName, String version) {
    setTitle(title);
    setGroup(group);
    setVersion(version);
    setPackageName(packageName);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title == null ? null : title.trim();
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group == null ? null : group.trim();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version == null ? null : version.trim();
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName == null ? null : packageName.trim();
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  public String getFullName() {
    return (group == null || group.isEmpty() ? "" : group + ":") + packageName;
  }

  public String toString() {
    return (group == null ? "" : group + ":") + packageName + (version == null ? "" : ":" + version);
  }

  @Override
  public int compareTo(@NotNull RenjinLibrary o) {
    return packageName.compareTo(o.getPackageName());
  }


}
