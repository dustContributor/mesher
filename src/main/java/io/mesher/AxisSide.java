package io.mesher;

import java.util.Objects;

public record AxisSide(org.joml.Vector3ic direction, Axis axis, Side side) {
  public AxisSide {
    Objects.requireNonNull(direction, "direction");
    Objects.requireNonNull(axis, "axis");
    Objects.requireNonNull(side, "side");
  }

  private static final AxisSide[][] VALUE_MATRIX;
  static {
    VALUE_MATRIX = new AxisSide[Side.VALUES.size()][Axis.VALUES.size()];
    for (var axis : Axis.VALUES) {
      for (var side : Side.VALUES) {
        VALUE_MATRIX[side.ordinal()][axis.ordinal()] = new AxisSide(
            new org.joml.Vector3i(
                Axis.HORIZONTAL == axis ? Side.FRONT == side ? 1 : -1 : 0,
                Axis.VERTICAL == axis ? Side.FRONT == side ? 1 : -1 : 0,
                Axis.DEPTH == axis ? Side.FRONT == side ? 1 : -1 : 0),
            axis,
            side);
      }
    }
  }

  public static AxisSide of(Axis axis, Side side) {
    Objects.requireNonNull(axis, "axis");
    Objects.requireNonNull(side, "side");
    return VALUE_MATRIX[side.ordinal()][axis.ordinal()];
  }
}