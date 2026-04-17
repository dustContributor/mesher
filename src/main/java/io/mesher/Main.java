package io.mesher;

import io.mesher.misc.OpsJson;

public class Main {
    public static void main(String[] args) {
        var voxels = new Voxels(4, 4, 4);
        for (int x = 0; x < voxels.width; ++x) {
            for (int z = 0; z < voxels.depth; ++z) {
                voxels.setValue(x, 0, z, 255);
            }
        }
        var stripifier = new Stripifier(voxels);
        var strips = stripifier.work();
        var counter = new Object() {
            int v;
        };
        strips.forEach((p, s) -> {
            System.out.println("--- {%d} ---".formatted(counter.v++));
            System.out.println("VoxelPlane: ");
            System.out.println(OpsJson.toStringPretty(p));
            System.out.println("StripPlanes: ");
            System.out.println(OpsJson.toStringPretty(s));
        });
    }
}