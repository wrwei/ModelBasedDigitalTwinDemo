package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to udpatedrone EOL
public class UpdateDroneProvider extends BaseProvider<UpdateDroneProvider.Request, UpdateDroneProvider.Result> {

    public static class Request {
        public String droneName;
        public List<Double> position;
    }

    public static class Result {
    }

    public static final UpdateDroneProvider instance = new UpdateDroneProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.droneName != null   &&
                request.position != null &&
                request.position.size() == 3;
    }

    private UpdateDroneProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "updatedrone", true, Request.class, Result.class);
    }
}