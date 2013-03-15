package com.isencia.passerelle.project.repository.api;

public class MetaData {

  public MetaData(String name, String path) {
    super();
    this.name = name;
    this.path = path;
  }

  public MetaData(String type, Long id, String description, String name, String comment, String revision, String path) {
    this(type, id, description, name, comment, revision);
    this.path = path;

  }

  public MetaData(String type, Long id, String description, String name, String comment, String revision) {
    super();
    this.id = id;
    this.description = description;
    this.name = name;
    this.comment = comment;
    this.revision = revision;
    this.type = type;
  }

  private String type;

  private Long id;

  private String description;

  private String name;

  private String comment;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  private String path;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  private String revision;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
