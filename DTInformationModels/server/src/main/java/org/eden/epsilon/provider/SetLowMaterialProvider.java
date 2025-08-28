package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to setlowmaterial EOL
public class SetLowMaterialProvider extends BaseProvider<SetLowMaterialProvider.Request, SetLowMaterialProvider.Result> {
    
    public static class Request {
        public String itemName;
        public List<String> materialNames;
        public List<Double> materialQuantities;
    }

    public static class Result {
    }

    public static final SetLowMaterialProvider instance = new SetLowMaterialProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.itemName            != null &&
                request.materialNames       != null && request.materialNames.stream().allMatch(materialName -> materialName != null) &&
                request.materialQuantities  != null && request.materialQuantities.stream().allMatch(materialQuantity -> materialQuantity != null);
    }

    private SetLowMaterialProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "setlowmaterial", true, Request.class, Result.class);
    }
}
