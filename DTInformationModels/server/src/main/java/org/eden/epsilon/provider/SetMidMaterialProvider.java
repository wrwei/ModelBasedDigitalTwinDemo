package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to setmidmaterial EOL
public class SetMidMaterialProvider extends BaseProvider<SetMidMaterialProvider.Request, SetMidMaterialProvider.Result> {
    
    public static class Request {
        public String itemName;
        public List<String> materialNames;
        public List<Double> materialQuantities;
    }

    public static class Result {
    }

    public static final SetMidMaterialProvider instance = new SetMidMaterialProvider();

    @Override
    protected boolean validate(Request request) {
        return  request.itemName            != null &&
                request.materialNames       != null && request.materialNames.stream().allMatch(materialName -> materialName != null) &&
                request.materialQuantities  != null && request.materialQuantities.stream().allMatch(materialQuantity -> materialQuantity != null);
    }

    private SetMidMaterialProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "setmidmaterial", true, Request.class, Result.class);
    }
}
