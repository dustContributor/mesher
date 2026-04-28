package io.mesher;

import java.io.File;
import java.util.concurrent.Callable;

import io.mesher.format.ObjFormat;
import io.mesher.format.TextFormat;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "run", description = "Reads a simple txt format for voxel data and turns it into a mesh.")
public final class Main implements Callable<Integer> {

    @Option(names = {
            "--noswapzy" }, description = "Loader assumes the file has Y up, use this flag to skip swapping the coordinates to Z up.")
    private boolean noSwapZY;

    @Option(names = {
            "--skipmesher" }, description = "Skip second pass of the algorithm, emitting geometry only for 'strips' of voxels.")
    private boolean skipMesher;

    @Parameters(index = "0", description = "File to read voxel data from.")
    private File src;

    @Parameters(index = "1", description = "File to write the mesh to.")
    private File dst;

    @Override
    public final Integer call() throws Exception {
        var voxels = TextFormat.ofConfig(new TextFormat.Config(!noSwapZY)).load(src.toPath());
        var stripifier = new Stripifier(voxels);
        var strips = stripifier.work();
        var mesher = new Mesher(strips);
        var mesh = skipMesher ? strips.toQuads() : mesher.work();
        ObjFormat.ofDefault().save(mesh, dst.toPath());
        return 0;
    }

    static {
        // No formatter is easier for debugging
        System.setProperty("joml.format", "false");
    }

    public static void main(String[] args) {
        int code = new CommandLine(new Main()).execute(args);
        System.exit(code);
    }
}
