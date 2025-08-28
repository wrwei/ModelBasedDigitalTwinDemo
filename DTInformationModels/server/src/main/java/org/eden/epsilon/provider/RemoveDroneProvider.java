package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to removedrone EOL
public class RemoveDroneProvider extends BaseProvider<RemoveDroneProvider.Request, RemoveDroneProvider.Result> {

    public static class Request {
        public String droneName;
    }

    public static class Result {
    }

    public static final RemoveDroneProvider instance = new RemoveDroneProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.droneName != null;
    }

    private RemoveDroneProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "removedrone", true, Request.class, Result.class);
    }
}