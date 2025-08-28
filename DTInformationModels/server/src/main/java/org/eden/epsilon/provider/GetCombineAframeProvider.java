package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EglModuleStore;

// correspond to combine EGL
public class GetCombineAframeProvider extends BaseProvider<GetCombineAframeProvider.Request, GetCombineAframeProvider.Result> {
    
    public static class Request {
        public String modelName;
    }

    public static class Result {
    }

    public static final GetCombineAframeProvider instance = new GetCombineAframeProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.modelName   != null;
    }

    private GetCombineAframeProvider() {
        super(RoadModelStore.instance, EglModuleStore.instance, "combine", false, Request.class, Result.class);
    }
}
