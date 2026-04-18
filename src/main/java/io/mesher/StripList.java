package io.mesher;

import java.util.*;

import io.mesher.misc.OpsCheck;

public final class StripList implements Iterable<StripSegment> {
  public static final StripList EMPTY;
  static {
    var empty = new int[0];
    EMPTY = new StripList(empty, empty, empty);
  }

  private final int[] starts;
  private final int[] lengths;
  private final int[] values;

  private StripList(int[] starts, int[] lengths, int[] values) {
    this.starts = Objects.requireNonNull(starts, "starts");
    this.lengths = Objects.requireNonNull(lengths, "lengths");
    this.values = Objects.requireNonNull(values, "values");
    OpsCheck.sorted(starts, "starts");
    OpsCheck.equals(starts.length, lengths.length, values.length);
  }

  public static StripList of(int[] starts, int[] lengths, int[] values) {
    return new StripList(starts, lengths, values);
  }

  public final Optional<StripSegment> findExact(int refStart, int refLength, int refValue) {
    OpsCheck.positive(refStart, "start");
    OpsCheck.aboveZero(refLength, "length");
    var i = Arrays.binarySearch(starts, refStart);
    if (i < 0) {
      return Optional.empty();
    }
    var l = lengthAt(i);
    if (l != refLength) {
      return Optional.empty();
    }
    var v = valueAt(i);
    if (v != refValue) {
      return Optional.empty();
    }
    // found exact match of start and length
    return Optional.of(new StripSegment(refStart, refLength, refValue));
  }

  public final int startAt(int i) {
    return starts[i];
  }

  public final int lengthAt(int i) {
    return lengths[i];
  }

  public final int valueAt(int i) {
    return values[i];
  }

  public final int count() {
    return starts.length;
  }

  @Override
  public final Iterator<StripSegment> iterator() {
    return new Iterator<StripSegment>() {
      int index;

      @Override
      public final boolean hasNext() {
        return index < count();
      }

      @Override
      public final StripSegment next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        int i = index++;
        return new StripSegment(startAt(i), lengthAt(i), valueAt(i));
      }
    };
  }
}