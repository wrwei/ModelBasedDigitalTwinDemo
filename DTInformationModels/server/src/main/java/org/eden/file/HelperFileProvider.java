package org.eden.file;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class HelperFileProvider extends BaseFileProvider {
    
    public static final HelperFileProvider instance = new HelperFileProvider();

    public InputStream getFile(String fileName) {

        if (hasSlashes(fileName)) {
            System.out.println("[User Error] Supplied file name contain slashes");
            return null;
        }

        String sanitizedPath;
        try {
            String joinedPath = Paths.get("helpers", fileName).toString();
            sanitizedPath = new File(joinedPath).getCanonicalPath();
        }
        catch (IOException | SecurityException ex) {
            System.out.println("[User Error] Supplied file name not found");
            return null;
        }

        return super.getFile(sanitizedPath);
    }
}
