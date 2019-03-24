package com.andrew.client.dto;

/**
 * @author andrew
 */
public class TerminalDto {

  private String terminalId;
  private int sequenceNo;
  private long timestamp;

  //Required for JSON (de)serialization
  public TerminalDto() {
  }

  public TerminalDto(String terminalId, int sequenceNo, long timestamp) {
    this.terminalId = terminalId;
    this.sequenceNo = sequenceNo;
    this.timestamp = timestamp;
  }

  public String getTerminalId() {
    return terminalId;
  }

  public int getSequenceNo() {
    return sequenceNo;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "TerminalDto{" +
        "terminalId='" + terminalId + '\'' +
        ", sequenceNo=" + sequenceNo +
        ", timestamp=" + timestamp +
        '}';
  }
}
