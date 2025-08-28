package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to setlanes EOL
public class SetLanesProvider extends BaseProvider<SetLanesProvider.Request, SetLanesProvider.Result> {
    
    public static class Request {
        public String itemName;
        public double lanes;
    }

    public static class Result {
    }

    public static final SetLanesProvider instance = new SetLanesProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.itemName != null &&
                request.lanes > 0.0;
    }

    private SetLanesProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "setlanes", true, Request.class, Result.class);
    }
}
