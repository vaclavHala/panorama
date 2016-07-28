
package com.mygdx.game.model;

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import java.util.List;

//https://github.com/libgdx/fbx-conv/wiki/Version-0.1-%28libgdx-0.9.9%29
public class G3DJTemplate {

    int[] version;
    String id;
    List<G3DJMesh> meshes;
    List<G3DJMaterial> materials;
    List<G3DJNode> nodes;
    List<G3DJAnimation> animations;

    public static class G3DJMesh {
        List<G3DJMeshAttribute> attributes;
        float[] vertices;
        List<G3DJPart> parts;
    }

    public enum G3DJMeshAttribute{
        POSITION,NORMAL,COLOR
    }

    public static class G3DJPart {
        String id;
        G3DJPartType type;
        short[] indices;
    }

    public enum G3DJPartType{
        TRIANGLES
    }

    public static class G3DJMaterial {
        String id;
        float[] ambient;
        float[] diffuse;
        float[] emissive;
        float opacity;
        float[] specular;
        float shininess;
    }

    public static class G3DJNode {
        String id;
        List<G3DJNodeChild> children;
    }

    public static class G3DJNodeChild {
        String id;
        List<G3DJNodeChildPart> parts;
    }

    public static class G3DJNodeChildPart {
        String meshpartid;
        String materialid;
    }

    public static class G3DJAnimation {
    }

}
