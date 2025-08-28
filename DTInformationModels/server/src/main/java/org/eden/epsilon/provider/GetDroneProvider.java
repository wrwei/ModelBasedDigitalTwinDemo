package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getdefects EOL
public class GetDroneProvider extends BaseProvider<GetDroneProvider.Request, GetDroneProvider.Result> {

    public static class Request {
        public String droneName;
    }

    public static class Result {
        public List<Double> position;
    }

    public static final GetDroneProvider instance = new GetDroneProvider();

    @Override
    protected boolean validate(Request request) {
        return request.droneName != null;
    }

    private GetDroneProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getdrone", false, Request.class, Result.class);
    }
}