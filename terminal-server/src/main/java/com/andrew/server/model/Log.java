package com.andrew.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author andrew
 */
@Entity
@Table(name = "log")
public class Log implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @NotNull(message = "Terminal ID cannot be null")
  @Column(name = "terminal_id", nullable = false)
  private String terminalId;

  @NotNull(message = "Sequence cannot be null")
  @Column(name = "sequence_no", nullable = false)
  private int sequenceNo;

  @NotNull(message = "Created cannot be null")
  @Column(name = "created", nullable = false)
  private LocalDateTime created;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTerminalId() {
    return terminalId;
  }

  public void setTerminalId(String terminalId) {
    this.terminalId = terminalId;
  }

  public int getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(int sequenceNo) {
    this.sequenceNo = sequenceNo;
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

    Log log = (Log) o;

    if (sequenceNo != log.sequenceNo) return false;
    if (!id.equals(log.id)) return false;
    return terminalId.equals(log.terminalId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + terminalId.hashCode();
    result = 31 * result + sequenceNo;
    return result;
  }

  @Override
  public String toString() {
    return "Log{" +
        "id=" + id +
        ", terminalId='" + terminalId + '\'' +
        ", sequenceNo=" + sequenceNo +
        ", created=" + created +
        '}';
  }
}
