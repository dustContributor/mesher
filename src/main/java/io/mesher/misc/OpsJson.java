package io.mesher.misc;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public final class OpsJson {
  /** Non-intantiable class */
  private OpsJson() {
    throw new RuntimeException();
  }

  private static final JsonMapper JSON = JsonMapper
      .builder()
      .build();
  private static final JsonMapper JSON_PRETTY = JsonMapper
      .builder()
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .build();

  public static <T> String toString(T obj) {
    return JSON.writeValueAsString(obj);
  }

  public static <T> String toStringPretty(T obj) {
    return JSON_PRETTY.writeValueAsString(obj);
  }
}
