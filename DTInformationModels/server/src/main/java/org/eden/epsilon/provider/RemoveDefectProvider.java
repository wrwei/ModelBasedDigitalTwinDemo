package org.eden.epsilon.provider;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to removedefect EOL
public class RemoveDefectProvider extends BaseProvider<RemoveDefectProvider.Request, RemoveDefectProvider.Result> {

    public static class Request {
        public String defectName;
    }

    public static class Result {
    }

    public static final RemoveDefectProvider instance = new RemoveDefectProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.defectName != null;
    }

    private RemoveDefectProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "removedefect", true, Request.class, Result.class);
    }
}