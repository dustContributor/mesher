package io.mesher;

import java.util.*;

import org.joml.Vector3i;

import io.mesher.Strips.VoxelPlane;

public class Stripifier {
  private final Voxels chunk;

  public Stripifier(Voxels chunk) {
    this.chunk = Objects.requireNonNull(chunk);
  }

  public Strips work() {
    var stripsByVoxelPlane = new HashMap<VoxelPlane, List<StripPlane>>();
    Axis[][] planes = {
        { Axis.HORIZONTAL, Axis.DEPTH }, // X-Z
        { Axis.HORIZONTAL, Axis.VERTICAL }, // X-Y
        { Axis.DEPTH, Axis.VERTICAL } // Z-Y
    };
    for (var plane : planes) {
      var sideAxis = plane[0];
      var forwardAxis = plane[1];
      var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
      var size = advanceAxis.axisValue(chunk.size);
      var backStrips = new ArrayList<StripPlane>();
      var frontStrips = new ArrayList<StripPlane>();
      for (int s = 0; s < size; ++s) {
        var b = work(Side.BACK, sideAxis, forwardAxis, s);
        var f = work(Side.FRONT, sideAxis, forwardAxis, s);
        backStrips.add(b);
        frontStrips.add(f);
      }
      stripsByVoxelPlane.put(
          VoxelPlane.of(sideAxis, forwardAxis, Side.BACK), backStrips);
      stripsByVoxelPlane.put(
          VoxelPlane.of(sideAxis, forwardAxis, Side.FRONT), frontStrips);
    }
    return Strips.of(stripsByVoxelPlane);
  }

  private StripPlane work(Side side, Axis sideAxis, Axis forwardAxis, int advanceOffset) {
    var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
    var axisSide = AxisSide.of(sideAxis, side);
    var sdSize = sideAxis.axisValue(chunk.size);
    var fwSize = forwardAxis.axisValue(chunk.size);
    var stripsPlane = new Strip[sdSize][];
    var start = advanceAxis.advance(new Vector3i(), advanceOffset);
    var offset = new Vector3i(start);
    for (int sdi = 0; sdi < sdSize; ++sdi) {
      var segmentStart = new Vector3i(start);
      int length = 1;
      var strips = new ArrayList<Strip>();
      var refValue = OptionalInt.empty();
      for (int fwi = 0; fwi < fwSize; ++fwi) {
        var nextValue = chunk.getValue(offset);
        if (refValue.isEmpty()) {
          refValue = nextValue;
          forwardAxis.advance(offset);
          continue;
        }
        if (refValue.equals(nextValue) && !chunk.isOccluded(start, axisSide)) {
          ++length;
          forwardAxis.advance(offset);
          continue;
        }
        strips.add(new Strip(segmentStart.x(), segmentStart.y(), segmentStart.z(), length, side, refValue.getAsInt()));
        refValue = nextValue;
        length = 1;
        forwardAxis.advance(offset);
      }
      stripsPlane[sdi] = strips.toArray(Strip[]::new);
      sideAxis.advance(start);
    }
    return StripPlane.of(stripsPlane, sideAxis, forwardAxis, side, start.x(), start.y(), start.z());
  }
}
