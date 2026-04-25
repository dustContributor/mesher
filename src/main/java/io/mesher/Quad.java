package io.mesher;

import java.util.Objects;

import org.joml.Vector3ic;

import io.mesher.misc.OpsCheck;

public record Quad(Vector3ic position, VoxelPlane voxelPlane, int forwardSize, int sideSize, int value) {
  public Quad {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(voxelPlane, "voxelPlane");
    OpsCheck.aboveZero(forwardSize, "forwardSize");
    OpsCheck.aboveZero(sideSize, "sideSize");
    // value can be anything, don't check it
  }

  public final int area() {
    return forwardSize * sideSize;
  }

  public final Axis forwardAxis() {
    return voxelPlane.forwardAxis();
  }

  public final Axis sideAxis() {
    return voxelPlane.sideAxis();
  }

  public final Side side() {
    return voxelPlane.side();
  }

}