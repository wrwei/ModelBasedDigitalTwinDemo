package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getdefects EOL
public class GetDefectsProvider extends BaseProvider<GetDefectsProvider.Request, GetDefectsProvider.Result> {

    public static class Request {
        public List<String> surfaceNames;
        public List<String> types;
        public List<Double> aabbMin = null;
        public List<Double> aabbMax = null;
    }

    public static class Result {
        public List<String> descriptions;
        public List<String> types;
        public List<List<Double>> locations;
        public List<String> surfaces;
        public List<String> names;
    }

    public static final GetDefectsProvider instance = new GetDefectsProvider();

    @Override
    protected boolean validate(Request request) {
        return  (request.surfaceNames == null || 
                    (request.surfaceNames != null && 
                        request.surfaceNames.stream().allMatch(surfaceName -> surfaceName != null))) && 
                (request.types == null || 
                    (request.types != null && 
                        request.types.stream().allMatch(type -> type != null))) &&
                ((request.aabbMin == null && request.aabbMax == null) ||
                    (request.aabbMin != null && request.aabbMax != null &&
                        request.aabbMin.size() == 3 && request.aabbMax.size() == 3));
    }

    private GetDefectsProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getdefects", false, Request.class, Result.class);
    }
}