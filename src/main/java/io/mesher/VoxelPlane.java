package io.mesher;

import java.util.Objects;

public record VoxelPlane(Axis sideAxis, Axis forwardAxis, Side side) {
  public VoxelPlane {
       Objects.requireNonNull(sideAxis);
       Objects.requireNonNull(forwardAxis);
       Objects.requireNonNull(side);
  }
}