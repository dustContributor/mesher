package io.mesher.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.mesher.Voxels;

/// Text format that represents voxels in Goxel's style. Uses '#' for comments.
///
/// Voxels are presented in the following form:
/// ```
/// # conceptually:
/// x y z value
/// # concrete example:
/// 5 2 0 ababab
/// ```
///
/// Where `x`, `y` and `z` are the integer coordinates of the voxel, and `value`
/// is an arbitrary hexadecimal value attached to the represented voxel. This
/// reader also accepts the `0x` prefix on every hexadecimal value.
public final class TextFormat {
  public static final int MAX_SUPPORTED_SIZE = 1024;

  private static final Pattern PART_SPLIT = Pattern.compile("\\s+");
  private static final String DELIMITER_COMMENT = "#";
  private static final String DELIMITER_HEX = "0x";

  private TextFormat() {
    throw new RuntimeException();
  }

  public static Voxels load(Path path) {
    Objects.requireNonNull(path, "path");
    try {
      return load(Files.lines(path));
    } catch (IOException e) {
      throw new UncheckedIOException("failed to load %s!".formatted(path), e);
    }
  }

  public static Voxels load(Stream<String> lines) {
    var items = lines.map(line -> {
      line = line.trim();
      if (line.isEmpty() || line.startsWith(DELIMITER_COMMENT)) {
        return null;
      }
      var parts = PART_SPLIT.split(line);
      if (parts.length != 4) {
        throw new IllegalStateException("expected 'x y z value', got '%s'!".formatted(line));
      }
      int x = Integer.parseInt(parts[0]);
      int y = Integer.parseInt(parts[1]);
      int z = Integer.parseInt(parts[2]);
      var strv = parts[3];
      // always hex, support both ffee and 0xffee formats
      int value = strv.startsWith(DELIMITER_HEX) ? Integer.decode(strv) : Integer.parseInt(strv, 16);
      return new Item(x, y, z, value);
    }).filter(Objects::nonNull).toList();

    if (items.isEmpty()) {
      throw new IllegalStateException("source has no usable data!");
    }
    // Re-center all voxels in case we have gaps on the outer sides
    var axisX = items.stream().mapToInt(Item::x).summaryStatistics();
    var axisY = items.stream().mapToInt(Item::y).summaryStatistics();
    var axisZ = items.stream().mapToInt(Item::z).summaryStatistics();

    int width = axisX.getMax() - axisX.getMin() + 1;
    int height = axisY.getMax() - axisY.getMin() + 1;
    int depth = axisZ.getMax() - axisZ.getMin() + 1;

    checkSize(width, "width");
    checkSize(height, "height");
    checkSize(depth, "depth");

    var voxels = new Voxels(width, height, depth);
    for (var item : items) {
      voxels = voxels.setValue(
          item.x - axisX.getMin(),
          item.y - axisY.getMin(),
          item.z - axisZ.getMin(),
          item.v);
    }

    return voxels;
  }

  private static void checkSize(int v, String name) {
    if (v < 0 || v > MAX_SUPPORTED_SIZE) {
      throw new IllegalStateException("%s of %d exceeds max supported size of %d!"
          .formatted(name, v, MAX_SUPPORTED_SIZE));
    }
  }

  private record Item(int x, int y, int z, int v) {
    // Empty
  }
}