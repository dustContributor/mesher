package io.mesher.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    var tracker = new Tracker();
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
          tracker.normal(AxisSide.of(remainingAxis, side).direction()),
          // mesh properties
          quad.value()));
    }
    // Important to sort before writing the file!
    indexedQuads.sort(IndexedQuad.CMP);
    // Build OBJ content
    try (var formatter = new Formatter(path.toFile(), StandardCharsets.UTF_8, Locale.ROOT)) {
      formatter.format("# header:file generated with voxel->mesh tool%n");
      formatter.format("# tstamp:%s%n", Instant.now().truncatedTo(ChronoUnit.MILLIS));
      formatter.format(
          "# json:{\"type\":\"%s\",\"vertices\":%d,\"normals\":%d,\"texCoords\":%d,\"triangles\":%d}%n",
          "meshStats",
          tracker.verticesCount(),
          tracker.normalsCount(),
          tracker.coordsCount(),
          indexedQuads.size() * 2 // two triangles per quad
      );
      // Write each de-duplicated attribute first in the file
      formatter.format("# region:vertices%n");
      tracker.vertices().forEach(v -> formatter.format("v %d.0 %d.0 %d.0%n", v.x(), v.y(), v.z()));
      formatter.format("# region:texcoords%n");
      tracker.coords().forEach(c -> formatter.format("vt %d.0 %d.0%n", c.x(), c.y()));
      formatter.format("# region:normals%n");
      tracker.normals().forEach(n -> formatter.format("vn %d.0 %d.0 %d.0%n", n.x(), n.y(), n.z()));
      // Split each quad into 2 triangles: 0->1->2 and 0->2->3
      var lastValue = 0;
      formatter.format("# region:faces%n");
      for (var quad : indexedQuads) {
        if (lastValue != quad.value()) {
          formatter.format("g json:{\"type\":\"%s\",\"value\":%d}%n", "voxelMeta", lastValue = quad.value());
        }
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

  private record IndexedQuad(
      int vert0,
      int vert1,
      int vert2,
      int vert3,
      int coord0,
      int coord1,
      int coord2,
      int coord3,
      int normal,
      int value) {
    /**
     * Sort quads to group them by voxel metadata below, plus sort them by vertices
     * so the geometry will be more contiguous in the file since we're at it
     */
    public static final Comparator<IndexedQuad> CMP = Comparator
        .comparingInt(IndexedQuad::value)
        .thenComparingInt(IndexedQuad::vert0)
        .thenComparingInt(IndexedQuad::vert1)
        .thenComparingInt(IndexedQuad::vert2)
        .thenComparingInt(IndexedQuad::vert3);
  }

  private static final class Tracker {
    private final Map<Vector3ic, Integer> vertices;
    private final Map<Vector2ic, Integer> coords;
    private final Map<Vector3ic, Integer> normals;

    public Tracker() {
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

    public final int verticesCount() {
      return vertices.size();
    }

    public final int normalsCount() {
      return normals.size();
    }

    public final int coordsCount() {
      return coords.size();
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

}