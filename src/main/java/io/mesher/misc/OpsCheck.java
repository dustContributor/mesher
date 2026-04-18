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

  public static void equals(int refValue, int... otherValues) {
    for (int j = 0; j < otherValues.length; ++j) {
      var other = otherValues[j];
      if (other != refValue) {
        throw new IllegalArgumentException(
            "value %d at %d is different from %d, they all must be equal!".formatted(other, j, refValue));
      }
    }
  }

  public static void sorted(int[] values, String name) {
    for (int i = 0; i < (values.length - 1); ++i) {
      var p = values[i];
      var n = values[i + 1];
      if (p > n) {
        throw new IllegalArgumentException(
            "value %d at %d is grater than %d, the elements of %s arent sorted!".formatted(p, i, n, name));
      }
    }
  }
}
