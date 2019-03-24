package com.andrew.server.dto;

/**
 * @author andrew
 */
public class Payload {

  private String terminalId;
  private int sequenceNo;
  private long timestamp;

  //Required for JSON (de)serialization
  public Payload() {
  }

  public Payload(String terminalId, int sequenceNo, long timestamp) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Payload payload = (Payload) o;

    if (sequenceNo != payload.sequenceNo) return false;
    if (timestamp != payload.timestamp) return false;
    return terminalId.equals(payload.terminalId);
  }

  @Override
  public int hashCode() {
    int result = terminalId.hashCode();
    result = 31 * result + sequenceNo;
    result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Payload{" +
        "terminalId='" + terminalId + '\'' +
        ", sequenceNo=" + sequenceNo +
        ", timestamp=" + timestamp +
        '}';
  }
}
