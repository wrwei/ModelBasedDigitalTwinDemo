package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getoffset EOL
public class GetOffsetProvider extends BaseProvider<GetOffsetProvider.Request, GetOffsetProvider.Result> {

    public static class Request {
        public String modelName;
    }

    public static class Result {
        public List<Double> offset;
    }

    public static final GetOffsetProvider instance = new GetOffsetProvider();

    @Override
    protected boolean validate(Request request) {
        return request.modelName != null;
    }

    private GetOffsetProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getoffset", false, Request.class, Result.class);
    }
}
