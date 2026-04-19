package io.mesher;

import java.util.Objects;

import org.joml.Vector3ic;

import io.mesher.misc.OpsCheck;

public record Quad(Vector3ic position, Axis forwardAxis, Axis sideAxis, int forwardSize, int sideSize, int value) {
  public Quad {
    Objects.requireNonNull(position, "postition");
    Objects.requireNonNull(forwardAxis, "forwardAxis");
    Objects.requireNonNull(sideAxis, "sideAxis");
    OpsCheck.aboveZero(forwardSize, "forwardSize");
    OpsCheck.aboveZero(sideSize, "sideSize");
    // value can be anything, don't check it
  }

  public final int area() {
    return forwardSize * sideSize;
  }
}