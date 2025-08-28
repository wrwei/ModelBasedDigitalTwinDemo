package org.eden.epsilon.models;

// concrete class for "roads" meta type
public class RoadModelStore extends BaseModelStore {

    public static final RoadModelStore instance = new RoadModelStore();

    public RoadModelStore() {
        super("roads");
    }

}
