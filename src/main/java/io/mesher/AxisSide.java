package io.mesher;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public enum AxisSide {
  HORIZONTAL_FRONT(+1, 0, 0, Axis.HORIZONTAL, Side.FRONT),
  HORIZONTAL_BACK(-1, 0, 0, Axis.HORIZONTAL, Side.BACK),
  VERTICAL_FRONT(0, +1, 0, Axis.VERTICAL, Side.FRONT),
  VERTICAL_BACK(0, -1, 0, Axis.VERTICAL, Side.BACK),
  DEPTH_FRONT(0, 0, +1, Axis.DEPTH, Side.FRONT),
  DEPTH_BACK(0, 0, -1, Axis.DEPTH, Side.BACK);

  public static final java.util.List<AxisSide> VALUES = java.util.List.of(values());

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
  public String toString() {
    return name() + "(x=" + x + ", y=" + y + ", z=" + z + ")";
  }

  /**
   * Returns the Axis associated with this AxisSide.
   */
  public Axis getAxis() {
    return axis;
  }

  /**
   * Returns the Side associated with this AxisSide.
   */
  public Side getSide() {
    return side;
  }

  /**
   * Returns an AxisSide based on the provided Axis and Side instances.
   *
   * @param axis the Axis instance
   * @param side the Side instance
   * @return the corresponding AxisSide
   */
  public static AxisSide of(Axis axis, Side side) {
    switch (axis) {
      case HORIZONTAL:
        return side == Side.FRONT ? HORIZONTAL_FRONT : HORIZONTAL_BACK;
      case VERTICAL:
        return side == Side.FRONT ? VERTICAL_FRONT : VERTICAL_BACK;
      case DEPTH:
        return side == Side.FRONT ? DEPTH_FRONT : DEPTH_BACK;
      default:
        throw new IllegalArgumentException("Unknown axis: " + axis);
    }
  }

}
