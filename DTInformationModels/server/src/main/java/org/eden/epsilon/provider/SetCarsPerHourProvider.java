package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to setcarsperhour EOL
public class SetCarsPerHourProvider extends BaseProvider<SetCarsPerHourProvider.Request, SetCarsPerHourProvider.Result> {
    
    public static class Request {
        public String itemName;
        public double carsPerHour;
        public List<Double> materialQuantities;
    }

    public static class Result {
    }

    public static final SetCarsPerHourProvider instance = new SetCarsPerHourProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.itemName    != null &&
                request.carsPerHour >= 0.0;
    }

    private SetCarsPerHourProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "setcarsperhour", true, Request.class, Result.class);
    }
}
