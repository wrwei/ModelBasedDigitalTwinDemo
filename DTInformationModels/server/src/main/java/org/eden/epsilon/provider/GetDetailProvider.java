package org.eden.epsilon.provider;

import java.util.List;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.modules.EolModuleStore;

// correspond to getdetail EOL
public class GetDetailProvider extends BaseProvider<GetDetailProvider.Request, GetDetailProvider.Result> {

    public static class Request {
        public String itemName;
    }

    public static class Result {
        public List<String> sections;
        public List<String> attributeGroup;
        public List<List<String>> attributeName;
        public List<List<List<String>>> attributeValue;
        public List<List<List<String>>> attributeOption;
    }

    public static final GetDetailProvider instance = new GetDetailProvider();

    @Override
    protected boolean validate(Request request) {
        return request.itemName != null;
    }

    private GetDetailProvider() {
        super(RoadModelStore.instance, EolModuleStore.instance, "getdetail", false, Request.class, Result.class);
    }
}