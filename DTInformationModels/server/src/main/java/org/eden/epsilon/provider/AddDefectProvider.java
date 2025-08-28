package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to adddefect EOL
public class AddDefectProvider extends BaseProvider<AddDefectProvider.Request, AddDefectProvider.Result> {

    public static class Request {
        public String surfaceName;
        public String defectName;
        public String description;
        public String defectType;
        public List<Double> location;
    }

    public static class Result {
    }

    public static final AddDefectProvider instance = new AddDefectProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.surfaceName != null    &&
                request.description != null &&
                request.defectName != null  &&
                request.defectType != null  &&
                request.location != null &&
                request.location.size() == 3;
    }

    private AddDefectProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "adddefect", true, Request.class, Result.class);
    }
}