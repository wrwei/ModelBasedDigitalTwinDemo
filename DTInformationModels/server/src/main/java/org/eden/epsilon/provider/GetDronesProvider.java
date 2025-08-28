package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getdefects EOL
public class GetDronesProvider extends BaseProvider<GetDronesProvider.Request, GetDronesProvider.Result> {

    public static class Request {
    }

    public static class Result {
        public List<String> names;
        public List<List<Double>> positions;
    }

    public static final GetDronesProvider instance = new GetDronesProvider();

    @Override
    protected boolean validate(Request request) {
        return  true;
    }

    private GetDronesProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getdrones", false, Request.class, Result.class);
    }
}