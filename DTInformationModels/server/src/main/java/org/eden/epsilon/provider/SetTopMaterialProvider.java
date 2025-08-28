package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to settopmaterial EOL
public class SetTopMaterialProvider extends BaseProvider<SetTopMaterialProvider.Request, SetTopMaterialProvider.Result> {
    
    public static class Request {
        public String itemName;
        public List<String> materialNames;
        public List<Double> materialQuantities;
    }

    public static class Result {
    }

    public static final SetTopMaterialProvider instance = new SetTopMaterialProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.itemName            != null &&
                request.materialNames       != null && request.materialNames.stream().allMatch(materialName -> materialName != null) &&
                request.materialQuantities  != null && request.materialQuantities.stream().allMatch(materialQuantity -> materialQuantity != null);
    }

    private SetTopMaterialProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "settopmaterial", true, Request.class, Result.class);
    }
}
