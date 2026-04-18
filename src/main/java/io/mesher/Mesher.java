package io.mesher;

import java.util.*;

import org.joml.Vector3i;

public final class Mesher {
  private final Strips strips;

  public Mesher(Strips strips) {
    this.strips = Objects.requireNonNull(strips);
  }

  public final ArrayList<Quad> work() {
    var quads = new ArrayList<Quad>();
    strips.forEach((p, s) -> process(p, s, quads));
    return quads;
  }

  private record Key(int start, int length, Axis sideAxis, Axis forwardAxis, Side side) {
    public static Key of(VoxelPlane p, StripSegment s) {
      return new Key(s.start(), s.length(), p.sideAxis(), p.forwardAxis(), p.side());
    }
  }

  private void process(VoxelPlane voxelPlane, List<StripPlane> stripPlanes, List<Quad> dst) {
    for (var stripPlane : stripPlanes) {
      process(stripPlane, dst);
    }
  }

  private void process(StripPlane stripPlane, List<Quad> dst) {
    var processed = new HashSet<Key>();
    for (int advi = 0; advi < stripPlane.size; ++advi) {
      var strips = stripPlane.stripListAt(advi);
      for (var strip : strips) {
        var q = process(strip, stripPlane, advi, processed);
        if (q.isPresent()) {
          dst.add(q.get());
        }
      }
    }
  }

  private Optional<Quad> process(StripSegment segment, StripPlane stripPlane, int advance, HashSet<Key> processed) {
    if (!processed.add(Key.of(stripPlane.voxelPlane, segment))) {
      return Optional.empty();
    }
    var sideLength = 0;
    for (int advi = advance + 1; advi <= stripPlane.size; ++advi) {
      var nextStrips = advi == stripPlane.size ? StripList.EMPTY : stripPlane.stripListAt(advi);
      var n = nextStrips.findExact(segment.start(), segment.length(), segment.value());
      if (n.isEmpty()) {
        break;
      }
      if (!processed.add(Key.of(stripPlane.voxelPlane, n.get()))) {
        throw new RuntimeException("shouldn't happen!");
      }
      ++sideLength;
    }
    var sideAxis = stripPlane.voxelPlane.sideAxis();
    var forwardAxis = stripPlane.voxelPlane.forwardAxis();
    var position = new Vector3i(stripPlane.position);
    sideAxis.advance(position, advance);
    var quad = new Quad(position, forwardAxis, sideAxis, segment.length(), sideLength, segment.value());
    return Optional.of(quad);
  }

}
