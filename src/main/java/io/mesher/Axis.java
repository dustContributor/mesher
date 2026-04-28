package io.mesher;

import java.util.List;
import java.util.Objects;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public enum Axis {
  HORIZONTAL,
  VERTICAL,
  DEPTH;

  public static final List<Axis> VALUES = List.of(values());
  static {
    VALUES.forEach(Object::hashCode);
  }

  public Vector3i advance(Vector3i v) {
    return advance(v, 1);
  }

  public Vector3i advance(Vector3i v, int a) {
    return v.add(
        this == HORIZONTAL ? a : 0,
        this == VERTICAL ? a : 0,
        this == DEPTH ? a : 0);
  }

  public int axisValue(Vector3ic v) {
    return axisValue(v.x(), v.y(), v.z());
  }

  public int axisValue(int x, int y, int z) {
    return switch (this) {
      case HORIZONTAL -> x;
      case VERTICAL -> y;
      case DEPTH -> z;
      default -> throw new IllegalArgumentException(String.valueOf(this));
    };
  }

  public static Axis remaining(Axis axisA, Axis axisB) {
    if (Objects.requireNonNull(axisA, "axisA") == Objects.requireNonNull(axisB, "axisB")) {
      throw new IllegalArgumentException("axis must be different!");
    }
    for (Axis axis : VALUES) {
      if (axis != axisA && axis != axisB) {
        return axis;
      }
    }
    throw new IllegalStateException("no remaining axis!");
  }
}
