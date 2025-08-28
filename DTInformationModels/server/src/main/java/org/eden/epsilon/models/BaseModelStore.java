package org.eden.epsilon.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

import com.google.common.io.Files;

// Abstract Model Store, each meta model is associated with 1 corresponding concrete model store
// Each model is loaded when first requested
public abstract class BaseModelStore {

    // Unique exception class for ModelStore errors
    public static class ModelStoreException extends Exception {
        public ModelStoreException(String message) {
            super(message);
        }
    }

    // One entry per model, containing read write lock per model
    private class Entry {
        public EmfModel model = null;
        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        public final Lock readLock = readWriteLock.readLock();
        public final Lock writeLock = readWriteLock.writeLock();
    }
    
    // Entries are only added or modified, never replaced or deleted
    private ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    // Name of the meta store
    private final String metaName;

    // constructor
    protected BaseModelStore(String metaName) {
        this.metaName = metaName;
    }

    // load model by name
    public boolean loadModel(String modelName) {

        // Create entry if not exist
        entries.putIfAbsent(modelName, new Entry());
        
        Entry entry = entries.get(modelName);

        entry.writeLock.lock();
        try {
            // Sanitize user provided paths
            String sanitizedModelPath;
            String sanitizedMetaPath;
            try {
                String joinedModelPath = Paths.get("models", modelName + ".model").toString();
                sanitizedModelPath = new File(joinedModelPath).getCanonicalPath();
                String joinedMetaPath = Paths.get("metas", metaName + ".ecore").toString();
                sanitizedMetaPath = new File(joinedMetaPath).getCanonicalPath();
            }
            catch (IOException | SecurityException ex) {
                return false;
            }
            
            // Remove existing model if already exist
            if (entry.model != null)
                entry.model.disposeModel();

            // This should never be closed unless overwritten
            @SuppressWarnings("resource")
            EmfModel model = new EmfModel();
            model.setName("Model");
            model.setModelFile(sanitizedModelPath);
            model.setMetamodelFile(sanitizedMetaPath);
            model.setReadOnLoad(true);
            model.setStoredOnDisposal(true);
            model.setCachingEnabled(true);
            model.setConcurrent(true);

            entry.model = model;

            return true;
        }
        finally {
            entry.writeLock.unlock();
        }
    }

    // wrapper for get with non-exclusive access
    public EmfModel getRead(String modelName) throws ModelStoreException, EolModelLoadingException {
        return get(modelName, false);
    }

    // wrapper for get with exclusive access
    public EmfModel getWrite(String modelName) throws ModelStoreException, EolModelLoadingException {
        return get(modelName, true);
    }

    // wrapper to free non-exclusive access
    public void freeRead(String modelName) {
        free(modelName, false);
    }

    // wrapper to free exclusive access
    public void freeWrite(String modelName) {
        free(modelName, true);
    }

    // give access to read or write lock depend on parameter
    private EmfModel get(String modelName, boolean exclusive) throws ModelStoreException, EolModelLoadingException {
        
        // See assumption in definition of entries
        Entry entry = entries.get(modelName);
        if (entry == null) {
            loadModel(modelName);
            entry = entries.get(modelName);
            if (entry == null)
                throw new ModelStoreException("[Failure] Model " + modelName + " not found");
        }

        // This check also implies model is not null since it will be loaded before
        if (exclusive)
            entry.writeLock.lock();
        else
            entry.readLock.lock();


        EmfModel model = entry.model;

        // Load model if not loaded yet
        if (!model.isLoaded()) {
            try {
                model.load();
            }
            catch (EolModelLoadingException ex) {
                if (exclusive)
                    entry.writeLock.lock();
                else
                    entry.readLock.lock();

                if (ex.getReason().endsWith("(No such file or directory)"))
                    throw new ModelStoreException("[User Error] Supplied model name not found");
                else
                    throw ex;
            }
        }
        
        return model;
    }

    // free access to read or write lock depend on parameter
    private void free(String modelName, boolean exclusive) {

        // See assumption in definition of entries
        Entry entry = entries.get(modelName);

        // Model does not exist
        if (entry == null)
            return;

        if (exclusive)
            entry.writeLock.unlock();
        else
            entry.readLock.unlock();
    }

    // create a copy from the main model file
    public void snapshotModel(String modelName) throws ModelStoreException {
        
        // See assumption in definition of entries
        Entry entry = entries.get(modelName);
        if (entry == null) {
            loadModel(modelName);
            entry = entries.get(modelName);
            if (entry == null)
                throw new ModelStoreException("[Failure] Model " + modelName + " not found");
        }

        entry.writeLock.lock();

        try {
            // copy model file into a file with "backup" appended to the name 
            EmfModel model = entry.model;
            String sourcePath = model.getModelFile();
            File sourceFile = new File(sourcePath);
            File descriptionFile = sourceFile.toPath().resolveSibling(modelName + "-Backup").toFile();
            Files.copy(sourceFile, descriptionFile);
        }
        catch (IOException ex) {
            throw new ModelStoreException("[Failure] Model " + modelName + " failed trying to copy main to backup file");
        }
        finally {
            entry.writeLock.unlock();
        }
    }

    // load backup previously created into the main model file
    public void restoreModel(String modelName) throws ModelStoreException {

        // See assumption in definition of entries
        Entry entry = entries.get(modelName);
        if (entry == null) {
            loadModel(modelName);
            entry = entries.get(modelName);
            if (entry == null)
                throw new ModelStoreException("[Failure] Model " + modelName + " not found");
        }

        entry.writeLock.lock();

        try {
            // copy file with "backup" appended in the name to the main model file
            EmfModel model = entry.model;
            String destinationPath = model.getModelFile();
            File descriptionFile = new File(destinationPath);
            File sourceFile = descriptionFile.toPath().resolveSibling(modelName + "-Backup").toFile();

            model.dispose();

            Files.copy(sourceFile, descriptionFile);

            entries.remove(modelName);
        }
        catch (IOException ex) {
            throw new ModelStoreException("[Failure] Model " + modelName + " failed trying to copy backup to main file");
        }
        finally {
            entry.writeLock.unlock();
        }
    }

    // check file exist
    public boolean modelExist(String modelName) {
        String sanitizedModelPath;
        try {
            String joinedModelPath = Paths.get("models", modelName + ".model").toString();
            sanitizedModelPath = new File(joinedModelPath).getCanonicalPath();
        }
        catch (IOException | SecurityException ex) {
            return false;
        }

        File file = new File(sanitizedModelPath);
        return file.exists() && !file.isDirectory();
}
}
