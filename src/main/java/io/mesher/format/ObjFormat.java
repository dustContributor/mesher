package io.mesher.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import io.mesher.Axis;
import io.mesher.AxisSide;
import io.mesher.Quad;
import io.mesher.Side;

public final class ObjFormat {

  private ObjFormat() {
    // Empty
  }

  public final void save(Iterable<Quad> quads, Path path) {
    Objects.requireNonNull(quads, "quads");
    Objects.requireNonNull(path, "path");

    var vertices = new ArrayList<Vector3ic>();
    var uvs = new ArrayList<Vector2ic>();
    var normals = new ArrayList<Vector3ic>();

    for (var quad : quads) {
      var position = to3i(quad.position());
      var forwardAxis = quad.forwardAxis();
      var sideAxis = quad.sideAxis();
      var remainingAxis = Axis.remaining(forwardAxis, sideAxis);
      int forwardSize = quad.forwardSize();
      int sideSize = quad.sideSize();
      var side = quad.side();

      if (side == Side.FRONT) {
        // Front face of the voxel is 1 unit further away
        remainingAxis.advance(position);
      }

      // Compute the four corner positions

      // Corner 1: (0, 0)
      vertices.add(to3i(position));
      // Corner 1: (forwardSize, 0)
      vertices.add(forwardAxis.advance(to3i(position), forwardSize));
      // Corner 2: (forwardSize, sideSize)
      vertices.add(forwardAxis.advance(sideAxis.advance(to3i(position), sideSize), forwardSize));
      // Corner 3: (0, sideSize)
      vertices.add(sideAxis.advance(to3i(position), sideSize));

      // Compute UV coordinates based on quad's size

      // Corner 0: (0, 0)
      uvs.add(to2i(0, 0));
      // Corner 1: (forwardSize, 0)
      uvs.add(to2i(forwardSize, 0));
      // Corner 2: (forwardSize, sideSize)
      uvs.add(to2i(forwardSize, sideSize));
      // Corner 3: (0, sideSize)
      uvs.add(to2i(0, sideSize));

      /*
       * The normal is perpendicular to the plane, in the direction of the remaining
       * axis
       */
      var normal = AxisSide.of(remainingAxis, side).direction();
      // Repeat same normal for vertices of the quad
      for (int i = 0; i < 4; ++i) {
        normals.add(normal);
      }
    }

    // Build OBJ content
    var sb = new StringBuilder();
    try (var formatter = new Formatter(sb, Locale.ROOT)) {
      for (var v : vertices) {
        formatter.format("v %d.0 %d.0 %d.0%n", v.x(), v.y(), v.z());
      }
      for (var uv : uvs) {
        formatter.format("vt %d.0 %d.0%n", uv.x(), uv.y());
      }
      for (var n : normals) {
        formatter.format("vn %d.0 %d.0 %d.0%n", n.x(), n.y(), n.z());
      }
      // Split each quad into 2 triangles: 0->1->2 and 0->2->3
      int index = 0;
      for (var _ : quads) {
        // Triangle 1: 0, 1, 2
        formatter.format("f %d/%d/%d %d/%d/%d %d/%d/%d%n",
            index + 1, index + 1, index + 1,
            index + 2, index + 2, index + 2,
            index + 3, index + 3, index + 3);
        // Triangle 2: 0, 2, 3 (counter-clockwise for proper winding)
        formatter.format("f %d/%d/%d %d/%d/%d %d/%d/%d%n",
            index + 1, index + 1, index + 1,
            index + 3, index + 3, index + 3,
            index + 4, index + 4, index + 4);
        index += 4;
      }
    }

    try {
      Files.writeString(path, sb);
    } catch (IOException e) {
      throw new UncheckedIOException("failed to save to %s!".formatted(path), e);
    }
  }

  public static ObjFormat ofDefault() {
    return new ObjFormat();
  }

  private static Vector2i to2i(int x, int y) {
    return new Vector2i(x, y);
  }

  private static Vector3i to3i(Vector3ic v) {
    return new Vector3i(v);
  }

}