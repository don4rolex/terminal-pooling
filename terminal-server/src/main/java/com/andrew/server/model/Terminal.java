package com.andrew.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author andrew
 */
@Entity
@Table(name = "terminal")
public class Terminal implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @NotNull(message = "ID cannot be null")
  @Column(nullable = false)
  private String id;

  @NotNull(message = "Created cannot be null")
  @Column(name = "created", nullable = false)
  private LocalDateTime created;

  public Terminal() {
  }

  public Terminal(String id, LocalDateTime created) {
    this.id = id;
    this.created = created;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public void setCreated(LocalDateTime created) {
    this.created = created;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Terminal terminal = (Terminal) o;

    return Objects.equals(id, terminal.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Terminal{" +
        "id='" + id + '\'' +
        ", created=" + created +
        '}';
  }
}
