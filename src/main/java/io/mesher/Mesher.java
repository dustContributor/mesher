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
    strips.forEach(e -> process(e.getKey(), e.getValue(), quads));
    return quads;
  }

  private void process(VoxelPlane voxelPlane, List<StripPlane> stripPlanes, List<Quad> dst) {
    stripPlanes.forEach(stripPlane -> process(stripPlane, dst));
  }

  private record Key(int start, int length, int advance, VoxelPlane voxelPlane) {
    public static Key of(VoxelPlane p, StripSegment s, int advance) {
      return new Key(s.start(), s.length(), advance, p);
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
    if (!processed.add(Key.of(stripPlane.voxelPlane, segment, advance))) {
      return Optional.empty();
    }
    var sideLength = 1;
    for (int advi = advance + 1; advi <= stripPlane.size; ++advi) {
      var nextStrips = advi == stripPlane.size ? StripList.EMPTY : stripPlane.stripListAt(advi);
      var n = nextStrips.findExact(segment.start(), segment.length(), segment.value());
      if (n.isEmpty()) {
        break;
      }
      if (!processed.add(Key.of(stripPlane.voxelPlane, n.get(), advi))) {
        throw new RuntimeException("shouldn't happen!");
      }
      ++sideLength;
    }
    var sideAxis = stripPlane.voxelPlane.sideAxis();
    var forwardAxis = stripPlane.voxelPlane.forwardAxis();
    var position = new Vector3i(stripPlane.position);
    /*
     * Need to offset the quad by axis we're advancing on and the initial segment
     * start along the forward axis
     */
    sideAxis.advance(position, advance);
    forwardAxis.advance(position, segment.start());
    var quad = new Quad(position, stripPlane.voxelPlane, segment.length(), sideLength, segment.value());
    return Optional.of(quad);
  }

}