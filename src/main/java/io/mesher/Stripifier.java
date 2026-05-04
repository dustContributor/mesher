package io.mesher;

import java.util.ArrayList;
import java.util.Objects;
import java.util.OptionalInt;

import org.joml.Vector3i;

public class Stripifier {
  private final Voxels voxels;

  public Stripifier(Voxels voxels) {
    this.voxels = Objects.requireNonNull(voxels, "voxels");
  }

  public Strips work() {
    Axis[][] planes = {
        { Axis.HORIZONTAL, Axis.DEPTH }, // X-Z
        { Axis.VERTICAL, Axis.HORIZONTAL }, // Y-X
        { Axis.DEPTH, Axis.VERTICAL } // Z-Y
    };
    var voxelPlanes = new ArrayList<VoxelPlane>();
    var stripPlanes = new ArrayList<StripPlane>();
    for (var plane : planes) {
      var sideAxis = plane[0];
      var forwardAxis = plane[1];
      var backVoxelPlane = VoxelPlane.of(sideAxis, forwardAxis, Side.BACK);
      var frontVoxelPlane = VoxelPlane.of(sideAxis, forwardAxis, Side.FRONT);
      var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
      var size = voxels.dimension(advanceAxis);
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
    var sdSize = voxels.dimension(plane.sideAxis());
    var stripsPlane = new Strip[sdSize][];
    for (int sdi = 0; sdi < sdSize; ++sdi) {
      var strips = strip(plane, sdi, advanceOffset);
      stripsPlane[sdi] = strips;
    }
    var start = advanceAxis.advance(new Vector3i(), advanceOffset);
    return StripPlane.of(stripsPlane, plane, start);
  }

  private Strip[] strip(VoxelPlane plane, int sidePos, int advancePos) {
    var forwardSize = voxels.dimension(plane.forwardAxis());
    var advanceAxis = Axis.remaining(plane.sideAxis(), plane.forwardAxis());
    // check occlusion along the orthogonal axis
    var occlusionSide = AxisSide.of(advanceAxis, plane.side());
    var segmentStart = plane.sideAxis().advance(new Vector3i(), sidePos);
    advanceAxis.advance(segmentStart, advancePos);
    var strips = new ArrayList<Strip>();
    var refValue = OptionalInt.empty();
    int length = 1;
    var forwardOffset = new Vector3i(segmentStart);
    /*
     * We want to check *including* the last voxel, that will check for occlusion
     * out of bounds, which is correct to handle the outer faces of the voxels
     */
    while (plane.forwardAxis().axisValue(forwardOffset) <= forwardSize) {
      var nextValue = voxels.getValue(forwardOffset);
      // find the first non-empty voxel
      if (refValue.isEmpty()) {
        if (voxels.isOccluded(forwardOffset, occlusionSide)) {
          // treat occluded voxels as empty and keep searching
          refValue = OptionalInt.empty();
        } else {
          // found our non occluded voxel
          refValue = nextValue;
        }
        segmentStart.set(forwardOffset);
        plane.forwardAxis().advance(forwardOffset);
        continue;
      }
      if (refValue.equals(nextValue) && !voxels.isOccluded(forwardOffset, occlusionSide)) {
        ++length;
        plane.forwardAxis().advance(forwardOffset);
        continue;
      }
      strips.add(Strip.of(segmentStart, length, plane.side(), refValue.getAsInt()));
      // re-set the starting point of the next strip and reference values
      refValue = OptionalInt.empty();
      length = 1;
    }
    return strips.toArray(Strip[]::new);
  }
}
