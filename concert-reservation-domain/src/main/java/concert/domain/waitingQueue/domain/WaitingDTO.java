package concert.domain.waitingQueue.domain;

import lombok.Data;

@Data
public class WaitingDTO {
  private String timestamp;
  private String uuid;

  private String token;

  WaitingDTO(String timestamp, String uuid) {
    this.timestamp = timestamp;
    this.uuid = uuid;
  }

  WaitingDTO(String timestamp, String uuid, String token) {
    this.timestamp = timestamp;
    this.uuid = uuid;
    this.token = token;
  }

  public static WaitingDTO of(String uuid) {
    String timestamp = Long.toString(System.currentTimeMillis());
    return new WaitingDTO(timestamp, uuid);
  }

  public boolean isUuidEquals(String uuid) {
    return this.uuid.equals(uuid);
  }

  public String getToken() {
    if (this.token == null) {
      this.token = new StringBuilder(timestamp).append(":").append(uuid).toString();
    }
    return token;
  }


  public static WaitingDTO parse(String entry) {
    String[] split = entry.split(":");
    String timestamp = split[0];
    String uuid = split[1];

    WaitingDTO waitingDTO = new WaitingDTO(timestamp, uuid, entry);

    return waitingDTO;
  }


}
