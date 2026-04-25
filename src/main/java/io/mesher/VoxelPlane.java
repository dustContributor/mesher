package io.mesher;

import java.util.Objects;

public record VoxelPlane(Axis sideAxis, Axis forwardAxis, Side side) {
  public VoxelPlane {
    Objects.requireNonNull(sideAxis, "sideAxis");
    Objects.requireNonNull(forwardAxis, "forwardAxis");
    Objects.requireNonNull(side, "side");
  }

  private static final VoxelPlane[][][] VALUE_MATRIX;
  static {
    VALUE_MATRIX = new VoxelPlane[Side.VALUES.size()][Axis.VALUES.size()][Axis.VALUES.size()];
    for (var side : Side.VALUES) {
      for (var sideAxis : Axis.VALUES) {
        for (var forwardAxis : Axis.VALUES) {
          VALUE_MATRIX[side.ordinal()][sideAxis.ordinal()][forwardAxis.ordinal()] = new VoxelPlane(
              sideAxis,
              forwardAxis,
              side);
        }
      }
    }
  }

  public static VoxelPlane of(Axis sideAxis, Axis forwardAxis, Side side) {
    Objects.requireNonNull(side, "side");
    Objects.requireNonNull(sideAxis, "sideAxis");
    Objects.requireNonNull(forwardAxis, "forwardAxis");
    return VALUE_MATRIX[side.ordinal()][sideAxis.ordinal()][forwardAxis.ordinal()];
  }
}