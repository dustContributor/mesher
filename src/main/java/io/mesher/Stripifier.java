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
      var size = chunk.dimension(advanceAxis);
      var backStrips = new ArrayList<StripPlane>();
      var frontStrips = new ArrayList<StripPlane>();
      for (int s = 0; s < size; ++s) {
        var b = work(Side.BACK, sideAxis, forwardAxis, s);
        var f = work(Side.FRONT, sideAxis, forwardAxis, s);
        backStrips.add(b);
        frontStrips.add(f);
      }
      stripsByVoxelPlane.put(
          new VoxelPlane(sideAxis, forwardAxis, Side.BACK), backStrips);
      stripsByVoxelPlane.put(
          new VoxelPlane(sideAxis, forwardAxis, Side.FRONT), frontStrips);
    }
    return Strips.of(stripsByVoxelPlane);
  }

  private StripPlane work(Side side, Axis sideAxis, Axis forwardAxis, int advanceOffset) {
    var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
    var sdSize = chunk.dimension(sideAxis);
    var stripsPlane = new Strip[sdSize][];
    for (int sdi = 0; sdi < sdSize; ++sdi) {
      var strips = strip(side, sideAxis, forwardAxis, sdi, advanceOffset);
      stripsPlane[sdi] = strips.toArray(Strip[]::new);
    }
    var start = advanceAxis.advance(new Vector3i(), advanceOffset);
    return StripPlane.of(stripsPlane, sideAxis, forwardAxis, side, start);
  }

  private ArrayList<Strip> strip(Side side, Axis sideAxis, Axis forwardAxis, int sidePos, int advancePos) {
    var fwSize = chunk.dimension(forwardAxis);
    var axisSide = AxisSide.of(sideAxis, side);
    var advanceAxis = Axis.remaining(sideAxis, forwardAxis);
    var segmentStart = sideAxis.advance(new Vector3i(), sidePos);
    advanceAxis.advance(segmentStart, advancePos);
    var strips = new ArrayList<Strip>();
    var refValue = OptionalInt.empty();
    int length = 1;
    var advanceOffset = new Vector3i(segmentStart);
    for (int fwi = 0; fwi < fwSize; ++fwi) {
      var nextValue = chunk.getValue(advanceOffset);
      if (refValue.isEmpty()) {
        refValue = nextValue;
        forwardAxis.advance(advanceOffset);
        continue;
      }
      if (refValue.equals(nextValue) && !chunk.isOccluded(advanceOffset, axisSide)) {
        ++length;
        forwardAxis.advance(advanceOffset);
        if (fwi < fwSize - 1) {
          // didnt reach the edge yet
          continue;
        }
      }
      strips.add(Strip.of(segmentStart, length, side, refValue.getAsInt()));
      // re-set the starting point of the next strip and reference values
      refValue = nextValue;
      length = 1;
      forwardAxis.advance(advanceOffset);
      segmentStart.set(advanceOffset);
    }
    return strips;
  }
}
