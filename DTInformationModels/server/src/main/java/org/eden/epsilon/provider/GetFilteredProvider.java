package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getfiltered EOL
public class GetFilteredProvider extends BaseProvider<GetFilteredProvider.Request, GetFilteredProvider.Result> {

    public static class Request {
        public List<String> sectionNames = null;
        public List<String> types = null;
        public List<Double> aabbMin = null;
        public List<Double> aabbMax = null;
    }

    public static class Result {
        public List<String> matched;
    }

    public static final GetFilteredProvider instance = new GetFilteredProvider();

    @Override
    protected boolean validate(Request request) {
        return  (request.sectionNames == null || 
                    (request.sectionNames != null && 
                        request.sectionNames.stream().allMatch(sectionName -> sectionName != null))) && 
                (request.types == null || 
                    (request.types != null && 
                        request.types.stream().allMatch(type -> type != null))) &&
                ((request.aabbMin == null && request.aabbMax == null) ||
                    (request.aabbMin != null && request.aabbMax != null &&
                        request.aabbMin.size() == 3 && request.aabbMax.size() == 3));
    }
                         

    private GetFilteredProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getfiltered", false, Request.class, Result.class);
    }
}
