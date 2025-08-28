package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EglModuleStore;

// correspond to browse EGL
public class GetBrowseProvider extends BaseProvider<GetBrowseProvider.Request, GetBrowseProvider.Result> {
    
    public static class Request {
        public String modelName;
    }

    public static class Result {
    }

    public static final GetBrowseProvider instance = new GetBrowseProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.modelName   != null;
    }

    private GetBrowseProvider() {
        super(RoadModelStore.instance, EglModuleStore.instance, "browse", false, Request.class, Result.class);
    }
}
