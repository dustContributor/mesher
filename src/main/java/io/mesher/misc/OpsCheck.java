package io.mesher.misc;

public final class OpsCheck {
  private OpsCheck() {
    throw new RuntimeException();
  }

  public static int aboveZero(int v, String name) {
    if (v <= 0) {
      throw new IllegalArgumentException("%s %d must be above zero!".formatted(name, v));
    }
    return v;
  }
}
