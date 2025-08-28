package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to adddrone EOL
public class AddDroneProvider extends BaseProvider<AddDroneProvider.Request, AddDroneProvider.Result> {

    public static class Request {
        public String droneName;
        public List<Double> position;
    }

    public static class Result {
    }

    public static final AddDroneProvider instance = new AddDroneProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.droneName != null   &&
                request.position != null &&
                request.position.size() == 3;
    }

    private AddDroneProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "adddrone", true, Request.class, Result.class);
    }
}