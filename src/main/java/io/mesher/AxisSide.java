package io.mesher;

import java.util.List;
import java.util.Objects;

public record AxisSide(org.joml.Vector3ic direction, Axis axis, Side side) {
  public AxisSide {
    Objects.requireNonNull(direction, "direction");
    Objects.requireNonNull(axis, "axis");
    Objects.requireNonNull(side, "side");
  }

  public static final List<AxisSide> VALUES;
  static {
    var tmp = new AxisSide[Side.VALUES.size() * Axis.VALUES.size()];
    for (var axis : Axis.VALUES) {
      for (var side : Side.VALUES) {
        tmp[indexOf(axis, side)] = new AxisSide(
            new org.joml.Vector3i(
                Axis.HORIZONTAL == axis ? Side.FRONT == side ? 1 : -1 : 0,
                Axis.VERTICAL == axis ? Side.FRONT == side ? 1 : -1 : 0,
                Axis.DEPTH == axis ? Side.FRONT == side ? 1 : -1 : 0),
            axis,
            side);
      }
    }
    VALUES = List.of(tmp);
  }

  public static AxisSide of(Axis axis, Side side) {
    Objects.requireNonNull(axis, "axis");
    Objects.requireNonNull(side, "side");
    return VALUES.get(indexOf(axis, side));
  }

  private static int indexOf(Axis axis, Side side) {
    return axis.ordinal() * Side.VALUES.size() + side.ordinal();
  }
}