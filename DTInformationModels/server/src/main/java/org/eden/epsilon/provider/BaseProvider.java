package org.eden.epsilon.provider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eden.epsilon.models.BaseModelStore;
import org.eden.epsilon.modules.BaseModuleStore;

// abstract provider class to codify request and result structure of various operation
public abstract class BaseProvider<Request, Result> {

    private final BaseModelStore modelStore;
    private final BaseModuleStore<?> moduleStore;
    private final String operationName;
    private final boolean exclusive;
    private final Class<Request> requestClass;
    private final Class<Result> resultClass;

    protected BaseProvider(BaseModelStore modelStore, BaseModuleStore<?> moduleStore, String operationName, boolean exclusive, Class<Request> requestClass, Class<Result> resultClass) {
        this.modelStore = modelStore;
        this.moduleStore = moduleStore;
        this.operationName = operationName;
        this.exclusive = exclusive;
        this.requestClass = requestClass;
        this.resultClass = resultClass;
    }

    public static class Return<Result> {
        public final String printOut;
        public final Result result;

        public Return(String printOut, Result result) {
            this.printOut = printOut;
            this.result = result;
        } 
    }

    protected abstract boolean validate(Request request);

    // execute reuest
    public Return<Result> process(String modelName, Request request) {
        // validate input format
        if (!validate(request)) {
            System.out.println("[User Error] Input failed validation");
            return new Return<Result>(null, null);
        }

        try {
            // convert input struct to hashmap
            HashMap<String, Object> parameters = new HashMap<>();
            for (Field field : requestClass.getDeclaredFields())
                parameters.put(field.getName(), field.get(request));
    
            // construct output hashmap from result struct
            HashMap<String, Object> outputs = new HashMap<>();
            for (Field field : resultClass.getDeclaredFields()) 
                outputs.put(field.getName(), null);
    
            // execute module, get printOut
            String printOut = moduleStore.runModule(modelStore, operationName, modelName, exclusive, parameters, outputs);
    
            // construct declared result struct
            Result result = resultClass.getDeclaredConstructor().newInstance();
    
            // fill result struct
            for (Field field : resultClass.getDeclaredFields()) 
                field.set(result, outputs.get(field.getName()));
            
            return new Return<Result>(printOut, result);
        }
        catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
            System.out.println("[Failure] Exception: " + ex.getMessage());
            ex.printStackTrace();
            return new Return<Result>(null, null);
        }
    }


}
