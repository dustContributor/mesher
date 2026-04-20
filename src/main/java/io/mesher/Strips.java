package io.mesher;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class Strips implements Iterable<Entry<VoxelPlane, List<StripPlane>>> {
  private final Entry<VoxelPlane, List<StripPlane>>[] stripsByVoxelPlane;

  private Strips(Entry<VoxelPlane, List<StripPlane>>[] stripsByVoxelPlane) {
    this.stripsByVoxelPlane = Objects.requireNonNull(stripsByVoxelPlane, "stripsByVoxelPlane");
  }

  public final Strips forEach(BiConsumer<VoxelPlane, List<StripPlane>> consumer) {
    for (var entry : stripsByVoxelPlane) {
      consumer.accept(entry.getKey(), entry.getValue());
    }
    return this;
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
