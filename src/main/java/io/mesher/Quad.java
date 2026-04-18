package io.mesher;

import org.joml.Vector3ic;

public record Quad(Vector3ic position, Axis forwardAxis, Axis sideAxis, int forwardSize, int sideSize, int value) {
  // Empty
}