package io.mesher;

import java.util.*;

import org.joml.Vector3i;

public class Stripifier {
  private final Voxels chunk;

  public Stripifier(Voxels chunk) {
    this.chunk = Objects.requireNonNull(chunk);
  }

  public Strips work() {
    Axis[][] planes = {
        { Axis.HORIZONTAL, Axis.DEPTH }, // X-Z
        { Axis.HORIZONTAL, Axis.VERTICAL }, // X-Y
        { Axis.DEPTH, Axis.VERTICAL } // Z-Y
    };
    var voxelPlanes = new ArrayList<VoxelPlane>();
    var stripPlanes = new ArrayList<StripPlane>();
    for (var plane : planes) {
      var sideAxis = plane[0];
      var forwardAxis = plane[1];
      var backVoxelPlane = new VoxelPlane(sideAxis, forwardAxis, Side.BACK);
      var frontVoxelPlane = new VoxelPlane(sideAxis, forwardAxis, Side.FRONT);
      var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
      var size = chunk.dimension(advanceAxis);
      for (int s = 0; s < size; ++s) {
        // TODO: Push back/front iteration lower in the logic
        var b = work(backVoxelPlane, s);
        var f = work(frontVoxelPlane, s);
        stripPlanes.add(b);
        stripPlanes.add(f);
      }
      voxelPlanes.add(backVoxelPlane);
      voxelPlanes.add(frontVoxelPlane);
    }
    return Strips.of(voxelPlanes, stripPlanes);
  }

  private StripPlane work(VoxelPlane plane, int advanceOffset) {
    var advanceAxis = Axis.remaining(plane.sideAxis(), plane.forwardAxis());
    var sdSize = chunk.dimension(plane.sideAxis());
    var stripsPlane = new Strip[sdSize][];
    for (int sdi = 0; sdi < sdSize; ++sdi) {
      var strips = strip(plane, sdi, advanceOffset);
      stripsPlane[sdi] = strips.toArray(Strip[]::new);
    }
    var start = advanceAxis.advance(new Vector3i(), advanceOffset);
    return StripPlane.of(stripsPlane, plane, start);
  }

  private ArrayList<Strip> strip(VoxelPlane plane, int sidePos, int advancePos) {
    var fwSize = chunk.dimension(plane.forwardAxis());
    var axisSide = AxisSide.of(plane.sideAxis(), plane.side());
    var advanceAxis = Axis.remaining(plane.sideAxis(), plane.forwardAxis());
    var segmentStart = plane.sideAxis().advance(new Vector3i(), sidePos);
    advanceAxis.advance(segmentStart, advancePos);
    var strips = new ArrayList<Strip>();
    var refValue = OptionalInt.empty();
    int length = 1;
    var advanceOffset = new Vector3i(segmentStart);
    for (int fwi = 0; fwi <=fwSize; ++fwi) {
      var nextValue = chunk.getValue(advanceOffset);
      if (refValue.isEmpty()) {
        refValue = nextValue;
        segmentStart.set(advanceOffset);
        plane.forwardAxis().advance(advanceOffset);
        continue;
      }
      if (refValue.equals(nextValue) && !chunk.isOccluded(advanceOffset, axisSide)) {
        ++length;
        plane.forwardAxis().advance(advanceOffset);
        continue;
      }
      strips.add(Strip.of(segmentStart, length, plane.side(), refValue.getAsInt()));
      // re-set the starting point of the next strip and reference values
      refValue = OptionalInt.empty();
      length = 1;
    }
    return strips;
  }
}
