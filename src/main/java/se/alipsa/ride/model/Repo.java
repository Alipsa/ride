package se.alipsa.ride.model;

import javafx.beans.property.SimpleStringProperty;

public class Repo {
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

  public SimpleStringProperty idProperty() {
    return id;
  }

  public void setId(String id) {
    this.id.set(id);
  }

  public String getType() {
    return type.get();
  }

  public SimpleStringProperty typeProperty() {
    return type;
  }

  public void setType(String type) {
    this.type.set(type);
  }

  public String getUrl() {
    return url.get();
  }

  public SimpleStringProperty urlProperty() {
    return url;
  }

  public void setUrl(String url) {
    this.url.set(url);
  }
}
