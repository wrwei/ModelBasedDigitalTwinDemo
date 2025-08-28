package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to decay EOL
public class DecayProvider extends BaseProvider<DecayProvider.Request, DecayProvider.Result> {

    public static class Request {
        public double simulationHours;
        public double totalDecayQuantityPerCarPerArea = 0.000000005;
    }

    public static class Result {
    }

    public static final DecayProvider instance = new DecayProvider();

    @Override
    protected boolean validate(Request request) {
        return request.simulationHours > 0.0 && request.totalDecayQuantityPerCarPerArea > 0.0;
    }

    private DecayProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "decay", true, Request.class, Result.class);
    }
}
