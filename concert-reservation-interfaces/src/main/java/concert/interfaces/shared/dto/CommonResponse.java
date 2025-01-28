package concert.interfaces.shared.dto;


import lombok.Value;

import java.util.LinkedHashMap;

@Value
public class CommonResponse<T> {

  private boolean success;

  private T data;

  private String message;

  private LinkedHashMap<String, Object> meta;


  private CommonResponse(boolean success, T data, LinkedHashMap<String, Object> meta, String message) {
    this.success = success;
    this.data = data;
    this.meta = meta;
    this.message = message;
  }


  public static <T> CommonResponse success(T data) {
    return new CommonResponse(true, data, null, null);
  }

  public static <T> CommonResponse fail(String message) {
    return new CommonResponse(false, null, null, message);
  }


}
