package se.alipsa.ride.model;

import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Repo implements Comparable<Repo> {
  private final SimpleStringProperty id;
  private final SimpleStringProperty type;
  private final SimpleStringProperty url;

  public Repo(String id, String type, String url) {
    this.id = new SimpleStringProperty(id);
    this.type = new SimpleStringProperty(type);
    this.url = new SimpleStringProperty(url);
  }

  public Repo() {
    this.id = new SimpleStringProperty("");
    this.type = new SimpleStringProperty("");
    this.url = new SimpleStringProperty("");
  }

  public String getId() {
    return id.get();
  }

  public void setId(String id) {
    this.id.set(id);
  }

  public SimpleStringProperty idProperty() {
    return id;
  }

  public String getType() {
    return type.get();
  }

  public void setType(String type) {
    this.type.set(type);
  }

  public SimpleStringProperty typeProperty() {
    return type;
  }

  public String getUrl() {
    return url.get();
  }

  public void setUrl(String url) {
    this.url.set(url);
  }

  public SimpleStringProperty urlProperty() {
    return url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Repo)) return false;
    Repo repo = (Repo) o;
    return Objects.equals(getId(), repo.getId()) &&
        Objects.equals(getType(), repo.getType()) &&
        Objects.equals(getUrl(), repo.getUrl());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getType(), getUrl());
  }

  @Override
  public int compareTo(@NotNull Repo o) {
    String oStr = o.getId() + o.getType() + o.getUrl();
    String tStr = getId() + getType() + getUrl();
    return tStr.compareTo(oStr);
  }

  @Override
  public String toString() {
    return "Repo(id=" + getId() + ", type=" + getType() + ", url=" + getUrl() + ")";
  }
}
