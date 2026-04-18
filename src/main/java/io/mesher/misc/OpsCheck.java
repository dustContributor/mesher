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

  public static int positive(int v, String name) {
    if (v < 0) {
      throw new IllegalArgumentException("%s %d must be positive!".formatted(name, v));
    }
    return v;
  }
}
