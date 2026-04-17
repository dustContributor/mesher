package io.mesher;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class StripPlane {

  private record InnerStrip(int[] starts, int[] lengths, int[] values) {
  }

  public record StripSegment(int start, int length, int value) {
  }

  public final Vector3ic position;
  public final Axis sideAxis;
  public final Axis forwardAxis;
  public final Side side;
  public final int size;

  private final InnerStrip[] stripsByAdvance;

  public StripPlane(Vector3ic position, Axis sideAxis, Axis forwardAxis, Side side, InnerStrip[] stripsByAdvance) {
    this.position = position;
    this.sideAxis = sideAxis;
    this.forwardAxis = forwardAxis;
    this.side = side;
    this.stripsByAdvance = stripsByAdvance;
    this.size = stripsByAdvance.length;
  }

  public static StripPlane of(Strip[][] strips, Axis sideAxis, Axis forwardAxis, Side side, int x, int y, int z) {
    var innerStrips = new InnerStrip[strips.length];
    for (int i = 0; i < strips.length; ++i) {
      var strip = strips[i];
      var starts = new int[strip.length];
      var lengths = new int[strip.length];
      var values = new int[strip.length];
      for (int j = 0; j < strip.length; ++j) {
        var tmp = strip[j];
        starts[j] = forwardAxis.axisValue(tmp.x(), tmp.y(), tmp.z());
        lengths[j] = strip[j].length();
        values[j] = strip[j].value();
      }
      innerStrips[i] = new InnerStrip(starts, lengths, values);
    }
    return new StripPlane(new Vector3i(x, y, z), sideAxis, forwardAxis, side, innerStrips);
  }

  public int segmentLengthOf(int advance, int start) {
    Objects.checkIndex(advance, stripsByAdvance.length);
    var strip = stripsByAdvance[advance];
    var stripi = Arrays.binarySearch(strip.starts, start);
    return stripi > 0 ? strip.starts[stripi] : 0;
  }

  public Iterator<StripSegment> segmentIteratorOf(int advance) {
    Objects.checkIndex(advance, stripsByAdvance.length);
    return new StripSegmentIterator(stripsByAdvance[advance]);
  }

  public Iterable<StripSegment> segmentIterableOf(int advance) {
    Objects.checkIndex(advance, stripsByAdvance.length);
    return () -> new StripSegmentIterator(stripsByAdvance[advance]);
  }

  // Inner class iterator for StripSegment
  private static final class StripSegmentIterator implements Iterator<StripSegment> {
    private final InnerStrip strip;

    private int index;
    private final int length;

    public StripSegmentIterator(InnerStrip strip) {
      this.strip = strip;
      this.index = 0;
      this.length = strip.starts.length;
    }

    @Override
    public boolean hasNext() {
      return index < length;
    }

    @Override
    public StripSegment next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      int i = index++;
      int startVertex = strip.starts[i];
      int segmentLength = strip.lengths[i];
      int segmentValue = strip.values[i];

      return new StripSegment(startVertex, segmentLength, segmentValue);
    }
  }

  @Override
  public String toString() {
    return "StripPlane{" +
        "position=" + position +
        ", sideAxis=" + sideAxis +
        ", forwardAxis=" + forwardAxis +
        ", side=" + side +
        ", strips=" + stripsByAdvance.length +
        '}';
  }

}
