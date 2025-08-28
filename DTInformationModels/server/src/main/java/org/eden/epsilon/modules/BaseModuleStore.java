package org.eden.epsilon.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.json.JsonModel;
import org.eclipse.epsilon.emc.json.JsonModelObject;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.ModelRepository;
import org.eden.epsilon.models.BaseModelStore;
import org.eden.epsilon.models.BaseModelStore.ModelStoreException;
import org.json.simple.JSONObject;

// abstract class for managing epsilon scripts, 1 concrete class per epsilon script type
public abstract class BaseModuleStore<ModuleType extends IEolModule> {

    private ConcurrentHashMap<String, ModulePool<ModuleType>> modules = new ConcurrentHashMap<>();

    // handle lifecycle of script instance, one pool per script source
    public static class ModulePool<ModuleType> {
        private LinkedList<ModuleType> inuse = new LinkedList<>();
        private LinkedList<ModuleType> idle = new LinkedList<>();

        // provide idle/unuse instance if possible, otherwise give null
        public synchronized ModuleType get() {
            if (idle.size() > 0) {
                ModuleType module = idle.pop();
                inuse.push(module);
                return module;
            }
            else 
                return null;
        }

        // push freed to idle/unuse list
        public synchronized void free(ModuleType module) {
            if (inuse.remove(module)) {
                idle.push(module);
            }
        }

        // push new entry to idle/unuse list
        public synchronized void put(ModuleType module) {
            idle.push(module);
        }
    }

    // custom exception for module store
    public static class ModuleStoreException extends Exception {
        public ModuleStoreException(String message) {
            super(message);
        }
    }

    private final String folder;
    private final String extension;
    private final Class<ModuleType> moduleClass;

    // constructor
    public BaseModuleStore(String folder, String extension, Class<ModuleType> moduleClass) {
        this.folder = folder;
        this.extension = extension;
        this.moduleClass = moduleClass;
    }

    // thread safe, ok if loaded more than one
    public boolean load(String moduleName) throws ModuleStoreException {
        String sanitizedOperationPath;
        try {
            String joinedOperationPath = Paths.get(folder, moduleName + extension).toString();
            sanitizedOperationPath = new File(joinedOperationPath).getCanonicalPath();
        }
        catch (IOException | SecurityException ex) {
            throw new ModuleStoreException("[Failure] Operation " + moduleName + " not found");
        }

        ModuleType module;
        try {
            // Parse the EOL program
            module = moduleClass.getConstructor().newInstance();
            module.parse(new File(sanitizedOperationPath));
        }
        catch (Exception ex) {
            throw new ModuleStoreException("[Failure] Operation " + moduleName + " loading failed");
        }

        modules.putIfAbsent(moduleName, new ModulePool<>());
        modules.get(moduleName).put(module);
        return true;
    }

    // thread safe, get module instance
    public ModuleType getEnsurue(String moduleName) throws ModuleStoreException {
        ModulePool<ModuleType> pool = modules.get(moduleName);
        if (pool == null)
            load(moduleName);
        pool = modules.get(moduleName);

        // requested module maybe taken up by another thread
        // keep loading until able to get 1
        ModuleType module = pool.get();
        while (module == null) {
            load(moduleName);
            module = pool.get();
        }

        return module;
    }

    // run module, setup input and output structure return values
    public String runModule(BaseModelStore modelStore, String moduleName, String modelName, boolean exclusive, HashMap<String, Object> inputs, HashMap<String, Object> outputs) {

        ModuleType module;
        EmfModel model;
        try {
            // acquire permission to model
            module = getEnsurue(moduleName);
            if (exclusive)
                model = modelStore.getWrite(modelName);
            else
                model = modelStore.getRead(modelName);
        } catch (ModuleStoreException | ModelStoreException ex) {
            System.out.println(ex.getMessage());
            return null;
        } catch (EolModelLoadingException ex) {
            System.out.println("[Failure] Exception:");
            ex.printStackTrace();
            return null;
        }

        try {
            // setup input
            JsonModel inputModel = new JsonModel();
            inputModel.setName("Input");
            inputModel.setJsonContent(new JSONObject(inputs).toJSONString());

            // setup output
            JsonModel outputModel = new JsonModel();
            outputModel.setName("Output");
            outputModel.setJsonContent("{}");

            // setup module IO
            ModelRepository repository = module.getContext().getModelRepository();
            repository.addModel(model);
            repository.addModel(inputModel);
            repository.addModel(outputModel);

            // get string output, typically used for EGL
            String printOut;
            try {
                printOut = (String) module.execute();
            }
            catch (EolRuntimeException ex) {
                if (ex.getReason().startsWith("[User Error]"))
                    System.out.println(ex.getReason());
                else {
                    System.out.println("[Failure] Exception:");
                    ex.printStackTrace();
                }
                return null;
            }
            finally {
                repository.removeModel(model);
                repository.removeModel(inputModel);
                repository.removeModel(outputModel);
            }

            if (!(outputModel.getRoot() instanceof JsonModelObject)) {
                System.out.println("[Failure] Output root is not an object");
                return null;
            }

            // construct output entries
            JsonModelObject root = (JsonModelObject)outputModel.getRoot();
            for (String key : outputs.keySet()) {
                outputs.put(key, root.get(key));
            }

            inputModel.dispose();
            outputModel.dispose();

            return printOut;
        }
        finally {
            if (exclusive) {
                model.store();
                modelStore.freeWrite(modelName);
            }
            else
                modelStore.freeRead(modelName);
            
        }
    }
}
