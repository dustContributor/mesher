package io.mesher;

import java.util.OptionalInt;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import io.mesher.misc.OpsCheck;

public final class Voxels {
  public final int width; // x
  public final int height; // y
  public final int depth; // z
  public final Vector3ic size;

  private final int[] data;

  public Voxels(int width, int height, int depth) {
    this.width = OpsCheck.aboveZero(width, "width");
    this.height = OpsCheck.aboveZero(height, "height");
    this.depth = OpsCheck.aboveZero(depth, "depth");
    this.size = new Vector3i(width, height, depth);
    this.data = new int[width * height * depth];
  }

  public final OptionalInt getValue(Vector3ic v) {
    return getValue(v.x(), v.y(), v.z());
  }

  public final OptionalInt getValue(int x, int y, int z) {
    if (isOutOfBounds(x, y, z)) {
      return OptionalInt.empty();
    }
    int index = indexOf(x, y, z);
    int value = data[index];
    if (value == 0) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(value);
  }

  public final Voxels setValue(int x, int y, int z, int value) {
    if (isOutOfBounds(x, y, z)) {
      throw new IllegalArgumentException("coords (%d,%d,%d) are out of bounds!".formatted(x, y, z));
    }
    int index = indexOf(x, y, z);
    data[index] = value;
    return this;
  }

  public final boolean isOccluded(Vector3ic p, AxisSide axisSide) {
    return isOccluded(p.x(), p.y(), p.z(), axisSide);
  }

  public final boolean isOccluded(int x, int y, int z, AxisSide axisSide) {
    x += axisSide.x;
    y += axisSide.y;
    z += axisSide.z;
    return getValue(x, y, z).isPresent();
  }

  private int indexOf(int x, int y, int z) {
    return z * height + y * width + x;
  }

  private boolean isOutOfBounds(int x, int y, int z) {
    if (x < 0 || y < 0 || z < 0) {
      return true;
    }
    if (x >= width || y >= height || z >= depth) {
      return true;
    }
    return false;
  }
}
