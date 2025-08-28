package org.eden.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public abstract class BaseFileProvider {

    private HashMap<String, byte[]> cache = new HashMap<>();

    protected boolean hasSlashes(String value) {
        return value.contains("/") || value.contains("\\");
    }

    protected InputStream getFile(String sanitizedPath) {
        byte[] exist = cache.get(sanitizedPath);
        if (exist != null)
           return new ByteArrayInputStream(exist);

        try (FileInputStream stream = new FileInputStream(new File(sanitizedPath))) {
            byte[] bytes = stream.readAllBytes();
            cache.put(sanitizedPath, bytes);

            return new ByteArrayInputStream(bytes);
        }
        catch (IOException ex) {
            System.out.println("[Failure] Failed to read file");
            return null;
        }
    }
}
