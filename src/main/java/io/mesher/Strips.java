package io.mesher;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.joml.Vector3i;

public final class Strips implements Iterable<Entry<VoxelPlane, List<StripPlane>>> {
  private final Entry<VoxelPlane, List<StripPlane>>[] stripsByVoxelPlane;

  private Strips(Entry<VoxelPlane, List<StripPlane>>[] stripsByVoxelPlane) {
    this.stripsByVoxelPlane = Objects.requireNonNull(stripsByVoxelPlane, "stripsByVoxelPlane");
  }

  /**
   * Converts all strips in this Strips to quads with sideSize=1 (no merging).
   * Each strip becomes its own quad with the correct position based on advance.
   *
   * @return a list of quads, one for each strip
   */
  public ArrayList<Quad> toQuads() {
    var quads = new ArrayList<Quad>();
    for (var e : this) {
      var voxelPlane = e.getKey();
      for (var stripPlane : e.getValue()) {
        var sideAxis = voxelPlane.sideAxis();
        var forwardAxis = voxelPlane.forwardAxis();
        for (int advi = 0; advi < stripPlane.size; ++advi) {
          var stripList = stripPlane.stripListAt(advi);
          var planePosition = sideAxis.advance(new Vector3i(stripPlane.position), advi);
          for (var stripSegment : stripList) {
            var position = new Vector3i(planePosition);
            forwardAxis.advance(position, stripSegment.start());
            quads.add(new Quad(
                position,
                voxelPlane,
                stripSegment.length(),
                1, // sideSize=1, no merging
                stripSegment.value()));
          }
        }
      }
    }
    return quads;
  }

  public final int count() {
    int count = 0;
    for (var e : this) {
      for (var plane : e.getValue()) {
        for (var stripList : plane) {
          count += stripList.count();
        }
      }
    }
    return count;
  }

  @Override
  public final Iterator<Entry<VoxelPlane, List<StripPlane>>> iterator() {
    return Arrays.asList(stripsByVoxelPlane).iterator();
  }

  @SuppressWarnings("unchecked")
  public static Strips of(List<VoxelPlane> voxelPlanes, List<StripPlane> stripPlanes) {
    var stripsByPlane = stripPlanes.stream().collect(Collectors.groupingBy(k -> k.voxelPlane));
    var tmp = voxelPlanes.stream()
        .map(v -> Map.entry(v, List.copyOf(stripsByPlane.get(v))))
        .toArray(Entry[]::new);
    return new Strips(tmp);
  }
}
