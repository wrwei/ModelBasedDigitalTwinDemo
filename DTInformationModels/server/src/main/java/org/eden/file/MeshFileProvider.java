package org.eden.file;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class MeshFileProvider extends BaseFileProvider {
    
    public static final MeshFileProvider instance = new MeshFileProvider();

    public InputStream getMesh(String modelName, String meshName) {
        
        if (hasSlashes(modelName) || hasSlashes(meshName)) {
            System.out.println("[User Error] Supplied file name contain slashes");
            return null;
        }

        String sanitizedPath;
        try {
            String joinedPath = Paths.get("meshes", modelName, meshName + ".glb").toString();
            sanitizedPath = new File(joinedPath).getCanonicalPath();
        }
        catch (IOException | SecurityException ex) {
            System.out.println("[User Error] Supplied mesh name not found");
            return null;
        }

        return super.getFile(sanitizedPath);
    }
}
