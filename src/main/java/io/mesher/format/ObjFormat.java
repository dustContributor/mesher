package io.mesher.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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

    var indexedQuads = new ArrayList<IndexedQuad>();
    /*
     * Fill the tracker with all the vertices, normals and texture coordinates. The
     * tracker will coalesce equal vertices, assigning them an index each
     */
    var tracker = new VertTracker();
    for (var quad : quads) {
      var position = to3i(quad.position());
      var side = quad.side();
      var forwardAxis = quad.forwardAxis();
      var sideAxis = quad.sideAxis();
      int forwardSize = quad.forwardSize();
      int sideSize = quad.sideSize();
      if (side == Side.BACK) {
        // Have to flip them around for proper winding order on back faces
        sideAxis = forwardAxis;
        forwardAxis = quad.sideAxis();
        sideSize = forwardSize;
        forwardSize = quad.sideSize();
      }
      var remainingAxis = Axis.remaining(forwardAxis, sideAxis);
      if (side == Side.FRONT) {
        // Front face of the voxel is 1 unit further away
        remainingAxis.advance(position);
      }
      indexedQuads.add(new IndexedQuad(
          // Compute the four corner positions
          // Corner 0: (0, 0)
          tracker.vertex(to3i(position)),
          // Corner 1: (forwardSize, 0)
          tracker.vertex(forwardAxis.advance(to3i(position), forwardSize)),
          // Corner 2: (forwardSize, sideSize)
          tracker.vertex(forwardAxis.advance(sideAxis.advance(to3i(position), sideSize), forwardSize)),
          // Corner 3: (0, sideSize)
          tracker.vertex(sideAxis.advance(to3i(position), sideSize)),
          // Compute UV coordinates based on quad's size
          // Corner 0: (0, 0)
          tracker.coord(to2i(0, 0)),
          // Corner 1: (forwardSize, 0)
          tracker.coord(to2i(forwardSize, 0)),
          // Corner 2: (forwardSize, sideSize)
          tracker.coord(to2i(forwardSize, sideSize)),
          // Corner 3: (0, sideSize)
          tracker.coord(to2i(0, sideSize)),
          /*
           * The normal is perpendicular to the plane, in the direction of the remaining
           * axis
           */
          tracker.normal(AxisSide.of(remainingAxis, side).direction())));
    }
    // Build OBJ content
    var sb = new StringBuilder();
    try (var formatter = new Formatter(sb, Locale.ROOT)) {
      // Write each de-duplicated attribute first in the file
      tracker.vertices().forEach(v -> formatter.format("v %d.0 %d.0 %d.0%n", v.x(), v.y(), v.z()));
      tracker.coords().forEach(c -> formatter.format("vt %d.0 %d.0%n", c.x(), c.y()));
      tracker.normals().forEach(n -> formatter.format("vn %d.0 %d.0 %d.0%n", n.x(), n.y(), n.z()));
      // Split each quad into 2 triangles: 0->1->2 and 0->2->3
      for (var quad : indexedQuads) {
        // Triangle 1: 0, 1, 2
        formatter.format("f %d/%d/%d %d/%d/%d %d/%d/%d%n",
            quad.vert0(), quad.coord0(), quad.normal(),
            quad.vert1(), quad.coord1(), quad.normal(),
            quad.vert2(), quad.coord2(), quad.normal());
        // Triangle 2: 0, 2, 3 (counter-clockwise for proper winding)
        formatter.format("f %d/%d/%d %d/%d/%d %d/%d/%d%n",
            quad.vert0(), quad.coord0(), quad.normal(),
            quad.vert2(), quad.coord2(), quad.normal(),
            quad.vert3(), quad.coord3(), quad.normal());
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

  private static final class VertTracker {

    private final Map<Vector3ic, Integer> vertices;
    private final Map<Vector2ic, Integer> coords;
    private final Map<Vector3ic, Integer> normals;

    public VertTracker() {
      this.vertices = new HashMap<>();
      this.coords = new HashMap<>();
      this.normals = new HashMap<>();
    }

    public final int vertex(Vector3ic v) {
      return attrib(v, vertices);
    }

    public final int coord(Vector2ic v) {
      return attrib(v, coords);
    }

    public final int normal(Vector3ic v) {
      return attrib(v, normals);
    }

    public final Stream<Vector3ic> vertices() {
      return sortedStream(vertices);
    }

    public final Stream<Vector3ic> normals() {
      return sortedStream(normals);
    }

    public final Stream<Vector2ic> coords() {
      return sortedStream(coords);
    }

    private static <T> int attrib(T v, Map<T, Integer> map) {
      var idx = map.getOrDefault(v, Integer.valueOf(0)).intValue();
      if (idx <= 0) {
        idx = map.size() + 1;
        map.put(v, Integer.valueOf(idx));
      }
      return idx;
    }

    private static <T> Stream<T> sortedStream(Map<T, Integer> map) {
      return map.entrySet().stream()
          .sorted((a, b) -> a.getValue().compareTo(b.getValue()))
          .map(v -> v.getKey());
    }

  }

  private record IndexedQuad(
      int vert0,
      int vert1,
      int vert2,
      int vert3,
      int coord0,
      int coord1,
      int coord2,
      int coord3,
      int normal) {
  }

}