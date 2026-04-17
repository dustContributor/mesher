package io.mesher;

import java.util.List;

public class Mesher {
  private final StripPlane[] planes;

  public Mesher(List<StripPlane> planes) {
    this.planes = planes.toArray(StripPlane[]::new);
  }

  public void work() {

  }

}
