package io.mesher;

import java.util.List;
import java.util.Objects;

public record VoxelPlane(Axis sideAxis, Axis forwardAxis, Side side) {
  public VoxelPlane {
    Objects.requireNonNull(side, "side");
    Objects.requireNonNull(sideAxis, "sideAxis");
    Objects.requireNonNull(forwardAxis, "forwardAxis");
  }

  private static final List<VoxelPlane> VALUES;
  static {
    var tmp = new VoxelPlane[Axis.VALUES.size() * Axis.VALUES.size() * Side.VALUES.size()];
    for (var side : Side.VALUES) {
      for (var sideAxis : Axis.VALUES) {
        for (var forwardAxis : Axis.VALUES) {
          tmp[indexOf(sideAxis, forwardAxis, side)] = new VoxelPlane(
              sideAxis,
              forwardAxis,
              side);
        }
      }
    }
    VALUES = List.of(tmp);
  }

  public static VoxelPlane of(Axis sideAxis, Axis forwardAxis, Side side) {
    Objects.requireNonNull(side, "side");
    Objects.requireNonNull(sideAxis, "sideAxis");
    Objects.requireNonNull(forwardAxis, "forwardAxis");
    return VALUES.get(indexOf(sideAxis, forwardAxis, side));
  }

  private static int indexOf(Axis sideAxis, Axis forwardAxis, Side side) {
    return sideAxis.ordinal() * Axis.VALUES.size() * Side.VALUES.size()
        + forwardAxis.ordinal() * Side.VALUES.size()
        + side.ordinal();
  }
}