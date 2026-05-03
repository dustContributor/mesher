# Mesher

An algorithm for generating triangle meshes from voxel data. This is a pure Java library that converts 3D voxel data into an optimized triangle mesh representation.

## Features

- **Voxel to Mesh Conversion**: Converts voxel data into triangle meshes
- **Common Algorithm**: Similar to greedy meshing
- **Simple Input Format**: Uses a simple text format for voxel data
- **Minimal Dependencies**: Uses JOML for its vector classes and Picocli for a CLI interface

## Usage

### Java API

```java
// Load voxels from a text file
Voxels voxels = TextFormat.ofDefault().load("input.txt");

// Convert voxels to strips
Stripifier stripifier = new Stripifier(voxels);
Strips strips = stripifier.work();

// Convert strips to mesh quads (with quad merging optimization)
Mesher mesher = new Mesher(strips);
List<Quad> mesh = mesher.work();

// Or skip the mesher pass for simpler geometry
mesh = strips.toQuads();

// Save as OBJ file
ObjFormat.ofDefault().save(mesh, "output.obj");
```

### CLI API

```bash
java -jar target/mesher-1.0-SNAPSHOT.jar --noswapzy input.txt output.obj
```

### Configuration

- `--noswapzy`: Skip automatic Y↔Z coordinate swap (assume Y-up input)
- `--skipmesher`: Skip the quad-merging 2nd optimization pass

## Input Format

The input uses a simple text format similar to Goxel's voxel format:

```
# Comments start with #
# x y z value
5 2 0 ababab
10 3 1 0x123456
```

- `x`, `y`, `z` are integer coordinates of the voxel
- `value` is a hexadecimal value (with or without `0x` prefix)
- Empty lines and lines starting with `#` are treated as comments
- Values > 0 represent voxels; 0 represents empty space

The voxels are re-centered on load to minimize the bounding box.

### Output Format

The output is in **OBJ format** (Wavefront Object), which includes:
- Vertex positions (`v`)
- UV coordinates (`vt`)
- Normals (`vn`)
- Triangle faces referencing vertices/UVs/normals (`f`)

## Project Structure

The main files are the following:
```
src/main/java/io/mesher/
├── Main.java          # CLI entry point using Picocli
├── Stripifier.java    # First pass, converts voxels to strips
├── Mesher.java        # Second pass, converts strips to merged quads
├── Voxels.java        # Generic 3D voxel data storage
└── format/
    ├── ObjFormat.java # OBJ file writer
    └── TextFormat.java # Voxel text format reader
```

## Algorithm Overview

### Two-Pass Approach

1. **"Stripification" Pass**: Voxels are grouped into "strips" - continuous sequences along one axis where voxels share the same value. This pass handles occlusion detection to determine which faces are visible.

2. **"Mesher" Pass**: Strips are combined into larger quads by finding adjacent strips that can form a coherent planar surface. This reduces the total number of triangles in the final mesh.

### Coordinate System

The algorithm uses three orthogonal axes:
- **HORIZONTAL** (X-axis)
- **VERTICAL** (Y-axis)  
- **DEPTH** (Z-axis)

The algorithm processes three voxel planes, which captures all faces of every voxel. Sub-dividing these planes into "front" and "back" facing faces.

## License

See [LICENSE](LICENSE) file.