package io.mesher;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Strips {
  public record VoxelPlane(Axis sideAxis, Axis forwardAxis, Side side) {
    public static VoxelPlane of(Axis sideAxis, Axis forwardAxis, Side side) {
      return new VoxelPlane(
          Objects.requireNonNull(sideAxis),
          Objects.requireNonNull(forwardAxis),
          Objects.requireNonNull(side));
    }
  }

  private final Map<VoxelPlane, StripPlane[]> stripsByVoxelPlane;

  private Strips(Map<VoxelPlane, StripPlane[]> stripsByVoxelPlane) {
    this.stripsByVoxelPlane = stripsByVoxelPlane;
  }

  public int sizeOf(VoxelPlane voxelPlane) {
    var strips = stripsByVoxelPlane.get(voxelPlane);
    return strips.length;
  }

  public StripPlane stripsAt(VoxelPlane voxelPlane, int advance) {
    var strips = stripsByVoxelPlane.get(voxelPlane);
    return strips[advance];
  }

  public final Strips forEach(BiConsumer<VoxelPlane, List<StripPlane>> consumer) {
    for (var entry : stripsByVoxelPlane.entrySet()) {
      consumer.accept(entry.getKey(), List.of(entry.getValue()));
    }
    return this;
  }

  public static Strips of(Map<VoxelPlane, List<StripPlane>> stripsByPlane) {
    return new Strips(stripsByPlane.entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(k -> k.getKey(),
            v -> v.getValue().toArray(StripPlane[]::new))));
  }
}
