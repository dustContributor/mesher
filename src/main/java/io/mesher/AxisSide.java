package io.mesher;

import java.util.List;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public enum AxisSide {
  HORIZONTAL_FRONT(+1, 0, 0, Axis.HORIZONTAL, Side.FRONT),
  HORIZONTAL_BACK(-1, 0, 0, Axis.HORIZONTAL, Side.BACK),
  VERTICAL_FRONT(0, +1, 0, Axis.VERTICAL, Side.FRONT),
  VERTICAL_BACK(0, -1, 0, Axis.VERTICAL, Side.BACK),
  DEPTH_FRONT(0, 0, +1, Axis.DEPTH, Side.FRONT),
  DEPTH_BACK(0, 0, -1, Axis.DEPTH, Side.BACK);

  public static final List<AxisSide> VALUES = List.of(values());
  static {
    VALUES.forEach(Object::hashCode);
  }

  public final Axis axis;
  public final Side side;
  public final Vector3ic direction;
  public final int x;
  public final int y;
  public final int z;

  AxisSide(int x, int y, int z, Axis axis, Side side) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.axis = axis;
    this.side = side;
    this.direction = new Vector3i(x, y, z);
  }

  @Override
  public final String toString() {
    return "%s(x=%d, y=%d, z=%d, axis=%s, side=%s)".formatted(name(), x, y, z, axis, side);
  }

  public static AxisSide of(Axis axis, Side side) {
    return switch (axis) {
      case HORIZONTAL -> checkSide(side, HORIZONTAL_BACK, HORIZONTAL_FRONT);
      case VERTICAL -> checkSide(side, VERTICAL_BACK, VERTICAL_FRONT);
      case DEPTH -> checkSide(side, DEPTH_BACK, DEPTH_FRONT);
      default -> throw new IllegalArgumentException("Unknown axis: " + axis);
    };
  }

  private static AxisSide checkSide(Side side, AxisSide forBack, AxisSide forFront) {
    return switch (side) {
      case BACK -> forBack;
      case FRONT -> forFront;
      default -> throw new IllegalArgumentException("Unknown side: " + side);
    };
  }

}
