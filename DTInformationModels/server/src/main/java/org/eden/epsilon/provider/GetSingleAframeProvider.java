package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EglModuleStore;

// correspond to single EGL
public class GetSingleAframeProvider extends BaseProvider<GetSingleAframeProvider.Request, GetSingleAframeProvider.Result> {
    
    public static class Request {
        public String modelName;
        public String sectionName;
    }

    public static class Result {
    }

    public static final GetSingleAframeProvider instance = new GetSingleAframeProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.modelName   != null &&
                request.sectionName != null;
    }

    private GetSingleAframeProvider() {
        super(RoadModelStore.instance, EglModuleStore.instance, "single", false, Request.class, Result.class);
    }
}
