package io.mesher;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class StripPlane implements Iterable<StripList> {

  public final Vector3ic position;
  public final VoxelPlane voxelPlane;
  public final int size;

  private final StripList[] stripsByAdvance;

  public StripPlane(Vector3ic position, VoxelPlane voxelPlane, StripList[] stripsByAdvance) {
    this.position = position;
    this.voxelPlane = voxelPlane;
    this.stripsByAdvance = stripsByAdvance;
    this.size = stripsByAdvance.length;
  }

  public static StripPlane of(Strip[][] strips, VoxelPlane voxelPlane, Vector3i position) {
    var innerStrips = new StripList[strips.length];
    for (int i = 0; i < strips.length; ++i) {
      var strip = strips[i];
      if (strip.length < 1) {
        // nothing else to do really
        innerStrips[i] = StripList.EMPTY;
        continue;
      }
      var starts = new int[strip.length];
      var lengths = new int[strip.length];
      var values = new int[strip.length];
      for (int j = 0; j < strip.length; ++j) {
        var tmp = strip[j];
        starts[j] = voxelPlane.forwardAxis().axisValue(tmp.x(), tmp.y(), tmp.z());
        lengths[j] = strip[j].length();
        values[j] = strip[j].value();
      }
      innerStrips[i] = StripList.of(starts, lengths, values);
    }
    return new StripPlane(position, voxelPlane, innerStrips);
  }

  public final StripList stripListAt(int advance) {
    Objects.checkIndex(advance, stripsByAdvance.length);
    return stripsByAdvance[advance];
  }

  @Override
  public final Iterator<StripList> iterator() {
    return Arrays.asList(stripsByAdvance).iterator();
  }

  @Override
  public final String toString() {
    return "StripPlane(position=%s, voxelPlane=%s, count=%d)".formatted(position, voxelPlane, size);
  }

}
