package io.mesher.format;

import java.nio.file.Path;
import java.util.Objects;

import io.mesher.Quad;

public final class ObjFormat {

  private ObjFormat() {
    throw new RuntimeException();
  }

  public static void save(Iterable<Quad> quads, Path path) {
    Objects.requireNonNull(quads, "quads");
    Objects.requireNonNull(path, "path");
  }
}