package io.mesher;

import java.util.Objects;

import org.joml.Vector3ic;

import io.mesher.misc.OpsCheck;

public record Strip(int x, int y, int z, int length, Side side, int value) {

  public Strip {
    Objects.requireNonNull(side);
    OpsCheck.positive(x, "x");
    OpsCheck.positive(y, "y");
    OpsCheck.positive(z, "z");
    OpsCheck.aboveZero(length, "length");
  }

  public static Strip of(Vector3ic pos, int length, Side side, int value) {
    return new Strip(pos.x(), pos.y(), pos.z(), length, side, value);
  }
}
