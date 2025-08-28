package org.eden.javalin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eden.epsilon.models.RoadModelStore;
import org.eden.epsilon.models.BaseModelStore.ModelStoreException;
import org.eden.epsilon.provider.AddDefectProvider;
import org.eden.epsilon.provider.AddDroneProvider;
import org.eden.epsilon.provider.GetBrowseProvider;
import org.eden.epsilon.provider.GetCombineAframeProvider;
import org.eden.epsilon.provider.GetDefectsProvider;
import org.eden.epsilon.provider.GetDetailProvider;
import org.eden.epsilon.provider.GetDroneProvider;
import org.eden.epsilon.provider.GetDronesProvider;
import org.eden.epsilon.provider.GetFilteredProvider;
import org.eden.epsilon.provider.GetOffsetProvider;
import org.eden.epsilon.provider.GetSingleAframeProvider;
import org.eden.epsilon.provider.DecayProvider;
import org.eden.epsilon.provider.RemoveDefectProvider;
import org.eden.epsilon.provider.RemoveDroneProvider;
import org.eden.epsilon.provider.SetCarsPerHourProvider;
import org.eden.epsilon.provider.SetLanesProvider;
import org.eden.epsilon.provider.SetLowMaterialProvider;
import org.eden.epsilon.provider.SetMidMaterialProvider;
import org.eden.epsilon.provider.SetTopMaterialProvider;
import org.eden.epsilon.provider.UpdateDroneProvider;
import org.eden.file.HelperFileProvider;
import org.eden.file.MeshFileProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.websocket.WsContext;

// server directly correspond with providers
public class Server {

    // webscoket hashmap for file changes
    private final Map<String, List<WsContext>> staticListeners = new ConcurrentHashMap<>();

    // webscoket hashmap for drone changes
    private final Map<String, List<WsContext>> droneListeners = new ConcurrentHashMap<>();

    // add websocket listeners
    private void AddStaticListener(String modelName, WsContext ctx) {

        List<WsContext> list = staticListeners.get(modelName);
        if (list == null)
        {
            list = new LinkedList<>();
            list.add(ctx);
            staticListeners.put(modelName, list);
        }
        else
            list.add(ctx);
    }

    // add websocket listeners
    private void AddDroneListener(String modelName, WsContext ctx) {

        List<WsContext> list = droneListeners.get(modelName);
        if (list == null)
        {
            list = new LinkedList<>();
            list.add(ctx);
            droneListeners.put(modelName, list);
        }
        else
            list.add(ctx);
    }

    // remove websocket listeners
    private void RemoveStaticListener(String modelName, WsContext ctx) {

        List<WsContext> list = staticListeners.get(modelName);
        if (list == null)
            return;

        list.remove(ctx);
    }

    // remove websocket listeners
    private void RemoveDroneListener(String modelName, WsContext ctx) {

        List<WsContext> list = droneListeners.get(modelName);
        if (list == null)
            return;

        list.remove(ctx);
    }

    // creat websocket and REST endpoints
    public Server() {
        
        Javalin.create(
            config -> {
                config.router.mount(router -> {
                    router.ws("/updates/{model-name}", ws -> {
                        // add websocket to hashmap
                        ws.onConnect(ctx -> {
                            String modelName = ctx.pathParam("model-name");
                            if (!RoadModelStore.instance.modelExist(modelName))
                                return;
                            
                                AddStaticListener(modelName, ctx);
                        });
                        
                        // keep alive when client request ping, or else socket will close in 20s of no message as per protocol
                        ws.onMessage(ctx -> {
                            ctx.sendPing();
                        });

                        // remove websocket from hashmap
                        ws.onClose(ctx -> {
                            String modelName = ctx.pathParam("model-name");
                            if (!RoadModelStore.instance.modelExist(modelName))
                                return;
                            
                            RemoveStaticListener(modelName, ctx);
                        });
                    });

                    router.ws("/drones/{model-name}", ws -> {
                        // add websocket to hashmap
                        ws.onConnect(ctx -> {
                            String modelName = ctx.pathParam("model-name");
                            if (!RoadModelStore.instance.modelExist(modelName))
                                return;
                            
                                AddDroneListener(modelName, ctx);
                        });
                        
                        // keep alive when client request ping, or else socket will close in 20s of no message as per protocol
                        ws.onMessage(ctx -> {
                            ctx.sendPing();
                        });

                        // remove websocket from hashmap
                        ws.onClose(ctx -> {
                            String modelName = ctx.pathParam("model-name");
                            if (!RoadModelStore.instance.modelExist(modelName))
                                return;
                            
                            RemoveDroneListener(modelName, ctx);
                        });
                    });
                });
            }
        )
        .get("/getOffset/{model-name}/{mesh-name}.json", ctx -> getOffset(ctx))
        .get("/getDetail/{model-name}/{item-name}.json", ctx -> getDetail(ctx))
        .get("/getFiltered/{model-name}.json", ctx -> getFiltered(ctx))
        .get("/getMesh/{model-name}/{mesh-name}.glb", ctx -> getMesh(ctx))
        .get("/getHelper/{file-name-with-extension}", ctx -> getHelper(ctx))
        .get("/aframe/{model-name}/{section-name}.html", ctx -> getSingleAframe(ctx))
        .get("/aframe/{model-name}.html", ctx -> getCombineAframe(ctx))
        .get("/browse/{model-name}.html", ctx -> getBrowse(ctx))
        .get("/getDefects/{model-name}.json", ctx -> getDefects(ctx))
        .post("/setTopMaterial/{model-name}/{item-name}", ctx -> setTopMaterial(ctx))
        .post("/setMidMaterial/{model-name}/{item-name}", ctx -> setMidMaterial(ctx))
        .post("/setLowMaterial/{model-name}/{item-name}", ctx -> setLowMaterial(ctx))
        .post("/setLanes/{model-name}/{item-name}", ctx -> setLanes(ctx))
        .post("/setCarsPerHour/{model-name}/{item-name}", ctx -> setCarsPerHour(ctx))
        .post("/addDefect/{model-name}", ctx -> addDefect(ctx))
        .post("/removeDefect/{model-name}", ctx -> removeDefect(ctx))
        .post("/decay/{model-name}", ctx -> decay(ctx))
        .post("/management/backup/{model-name}", ctx -> backup(ctx))
        .post("/management/restore/{model-name}", ctx -> restore(ctx))
        .get("/getDrone/{model-name}/{drone-name}.json", ctx -> getDrone(ctx))
        .get("/getDrones/{model-name}.json", ctx -> getDrones(ctx))
        .post("/addDrone/{model-name}", ctx -> addDrone(ctx))
        .post("/updateDrone/{model-name}", ctx -> updateDrone(ctx))
        .post("/removeDrone/{model-name}", ctx -> removeDrone(ctx))
        .start(7070);
    }

    // helper to notify relevant websocket of changes
    private void notifyStaticWebSocket(String modelName, String name) {

        if (!staticListeners.containsKey(modelName))
            return;

        // construct json message
        HashMap<String, Object> root = new HashMap<>();
        root.put("name", name);
        root.put("time", System.currentTimeMillis());

        String json = new JSONObject(root).toJSONString();

        for (WsContext ctx : staticListeners.get(modelName))
            ctx.send(json);
    }

    // helper to notify relevant websocket of changes
    private void notifyDroneWebSocket(String modelName, String name, String change) {

        if (!droneListeners.containsKey(modelName))
            return;

        // construct json message
        HashMap<String, Object> root = new HashMap<>();
        root.put("name", name);
        root.put("change", change);
        root.put("time", System.currentTimeMillis());

        String json = new JSONObject(root).toJSONString();

        for (WsContext ctx : droneListeners.get(modelName))
            ctx.send(json);
    }

    // execute GetOffsetProvider
    private void getOffset(Context context) {

        GetOffsetProvider.Request request = new GetOffsetProvider.Request();
        request.modelName = context.pathParam("mesh-name");
        
        GetOffsetProvider.Result result = GetOffsetProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null || result.offset == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        HashMap<String, Object> output = new HashMap<>();
        output.put("offset", result.offset);

        context.result(new JSONObject(output).toJSONString());
    }

    // execute GetDetailProvider
    private void getDetail(Context context) {

        GetDetailProvider.Request request = new GetDetailProvider.Request();
        request.itemName = context.pathParam("item-name");
        
        GetDetailProvider.Result result = GetDetailProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null || result.attributeName == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        HashMap<String, Object> attributes = new HashMap<>();

        for (int i = 0; i < result.attributeGroup.size(); i++) {

            String attribute = result.attributeGroup.get(i);
            List<String> names = result.attributeName.get(i);
            List<List<String>> values = result.attributeValue.get(i);
            List<List<String>> options = result.attributeOption.get(i);

            ArrayList<Object> entries = new ArrayList<Object>(values.size());
            for (int v = 0; v < values.size(); v++) {

                HashMap<String, Object> output = new HashMap<>();
                List<String> input = values.get(v);

                for (int n = 0; n < names.size(); n++)
                    output.put(names.get(n), input.get(n));

                entries.add(output);
            }

            HashMap<String, Object> choices = new HashMap<>();
            {   
                for (int n = 0; n < names.size(); n++) {
                //     List<String> input = options.get(n);
                //     ArrayList<Object> output = new ArrayList<>(input);
                //     for (int o = 0; o < input.size(); o++) {
                //         output.add(input);
                // }
                    choices.put(names.get(n), options.get(n));
                }
            }

            HashMap<String, Object> intermediate = new HashMap<>();
            intermediate.put("data", entries);
            intermediate.put("options", choices);

            attributes.put(attribute, intermediate);
        }

        HashMap<String, Object> root = new HashMap<>();
        root.put("section", result.sections);
        root.put("attributes", attributes);
        root.put("time", System.currentTimeMillis());

        context.result(new JSONObject(root).toJSONString());
    }

    // execute GetFilteredProvider
    private void getFiltered(Context context) {

        GetFilteredProvider.Request request = new GetFilteredProvider.Request();
        
        List<String> sectionNames = context.queryParams("section-names");
        if (sectionNames.size() > 0)
            request.sectionNames = sectionNames;

        List<String> types = context.queryParams("types");
        if (types.size() > 0)
            request.types = types;

        List<String> aabbMin = context.queryParams("aabb-min");
        if (aabbMin.size() > 0)
            request.aabbMin = aabbMin.stream().map(axis -> Double.parseDouble(axis)).collect(Collectors.toList());

        List<String> aabbMax = context.queryParams("aabb-max");
        if (aabbMax.size() > 0)
            request.aabbMax = aabbMax.stream().map(axis -> Double.parseDouble(axis)).collect(Collectors.toList());

        GetFilteredProvider.Result result = GetFilteredProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null || result.matched == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        HashMap<String, Object> output = new HashMap<>();
        output.put("matched", result.matched);

        context.result(new JSONObject(output).toJSONString());
    }

    // supply mesh file of a given name for a model
    private void getMesh(Context context) {

        InputStream stream = MeshFileProvider.instance.getMesh(context.pathParam("model-name"), context.pathParam("mesh-name"));
        if (stream == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.result(stream);
    }

    // supply file, usuall css or js by name
    private void getHelper(Context context) {
        String fileName = context.pathParam("file-name-with-extension");
        InputStream stream = HelperFileProvider.instance.getFile(fileName);
        if (stream == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        if (fileName.endsWith(".js"))
            context.contentType("text/javascript");
        else if (fileName.endsWith(".css")) 
            context.contentType("text/css");
            else if (fileName.endsWith(".html")) 
                context.contentType("text/html");
        context.result(stream);
    }

    // execute GetSingleAframeProvider return webpage
    private void getSingleAframe(Context context) {


        GetSingleAframeProvider.Request request = new GetSingleAframeProvider.Request();
        request.modelName = context.pathParam("model-name");
        request.sectionName = context.pathParam("section-name");

        String printOut = GetSingleAframeProvider.instance.process(request.modelName, request).printOut;
        if (printOut == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.contentType(ContentType.HTML);
        context.result(printOut);
    }

    // execute GetCombineAfraemProvider return webpage
    private void getCombineAframe(Context context) {

        GetCombineAframeProvider.Request request = new GetCombineAframeProvider.Request();
        request.modelName = context.pathParam("model-name");

        String printOut = GetCombineAframeProvider.instance.process(request.modelName, request).printOut;
        if (printOut == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.contentType(ContentType.HTML);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(printOut.getBytes(StandardCharsets.UTF_8))) {
            context.result(inputStream);
        }
        catch (IOException ex) {
            System.out.println("[Failure] Failed to close input stream");
            return;
        }
    }

    // execute GetBrowseProvider return webpage
    private void getBrowse(Context context) {
        GetBrowseProvider.Request request = new GetBrowseProvider.Request();
        request.modelName = context.pathParam("model-name");

        String printOut = GetBrowseProvider.instance.process(request.modelName, request).printOut;
        if (printOut == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.contentType(ContentType.HTML);
        context.result(printOut);
    }

    // execute SetTopMaterialProvider
    private void setTopMaterial(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;
        
        Object fields = json.get("materials");
        if (fields == null || !(fields instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        LinkedList<String> materialNames = new LinkedList<>();
        LinkedList<Double> materialQuantities = new LinkedList<>();

        JSONArray materials = (JSONArray)fields;
        for (Object field : materials) {

            if (!(field instanceof JSONObject)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
            JSONObject material = (JSONObject)field;

            Object type = material.get("type");
            if (type == null || !(type instanceof String)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
    
            Object quantity = material.get("quantity");
            if (quantity == null || !(quantity instanceof Number)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }

            materialNames.add((String)type);
            materialQuantities.add(((Number)quantity).doubleValue());
        }

        String itemName = context.pathParam("item-name");
        SetTopMaterialProvider.Request request = new SetTopMaterialProvider.Request();
        request.itemName = itemName;
        request.materialNames = materialNames;
        request.materialQuantities = materialQuantities;
        
        String modelName = context.pathParam("model-name");
        SetTopMaterialProvider.Result result = SetTopMaterialProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, itemName);
    }

    // execute SetMidMaterialProvider
    private void setMidMaterial(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;
        
        Object fields = json.get("materials");
        if (fields == null || !(fields instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        LinkedList<String> materialNames = new LinkedList<>();
        LinkedList<Double> materialQuantities = new LinkedList<>();

        JSONArray materials = (JSONArray)fields;
        for (Object field : materials) {

            if (!(field instanceof JSONObject)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
            JSONObject material = (JSONObject)field;

            Object type = material.get("type");
            if (type == null || !(type instanceof String)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
    
            Object quantity = material.get("quantity");
            if (quantity == null || !(quantity instanceof Number)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }

            materialNames.add((String)type);
            materialQuantities.add(((Number)quantity).doubleValue());
        }

        String itemName = context.pathParam("item-name");
        SetMidMaterialProvider.Request request = new SetMidMaterialProvider.Request();
        request.itemName = itemName;
        request.materialNames = materialNames;
        request.materialQuantities = materialQuantities;
        
        String modelName = context.pathParam("model-name");
        SetMidMaterialProvider.Result result = SetMidMaterialProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, itemName);
    }

    // execute SetLowMaterialProvider
    private void setLowMaterial(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;
        
        Object fields = json.get("materials");
        if (fields == null || !(fields instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        LinkedList<String> materialNames = new LinkedList<>();
        LinkedList<Double> materialQuantities = new LinkedList<>();

        JSONArray materials = (JSONArray)fields;
        for (Object field : materials) {

            if (!(field instanceof JSONObject)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
            JSONObject material = (JSONObject)field;

            Object type = material.get("type");
            if (type == null || !(type instanceof String)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }
    
            Object quantity = material.get("quantity");
            if (quantity == null || !(quantity instanceof Number)) {
                context.status(HttpStatus.BAD_REQUEST);
                return;
            }

            materialNames.add((String)type);
            materialQuantities.add(((Number)quantity).doubleValue());
        }

        String itemName = context.pathParam("item-name");
        SetLowMaterialProvider.Request request = new SetLowMaterialProvider.Request();
        request.itemName = itemName;
        request.materialNames = materialNames;
        request.materialQuantities = materialQuantities;
        
        String modelName = context.pathParam("model-name");
        SetLowMaterialProvider.Result result = SetLowMaterialProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, itemName);
    }

    // exeucte SetLanesProvider
    private void setLanes(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object lanesField = json.get("lanes");
        if (lanesField == null || !(lanesField instanceof Number)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        
        String itemName = context.pathParam("item-name");
        SetLanesProvider.Request request = new SetLanesProvider.Request();
        request.itemName = itemName;
        request.lanes = ((Number)lanesField).doubleValue();
        
        String modelName = context.pathParam("model-name");
        SetLanesProvider.Result result = SetLanesProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, itemName);
    }

    // exeucte SetCarsPerHourProvider
    private void setCarsPerHour(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object carsPerHourField = json.get("carsPerHour");
        if (carsPerHourField == null || !(carsPerHourField instanceof Number)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        
        String itemName = context.pathParam("item-name");
        SetCarsPerHourProvider.Request request = new SetCarsPerHourProvider.Request();
        request.itemName =  itemName;
        request.carsPerHour = ((Number)carsPerHourField).doubleValue();
        
        String modelName = context.pathParam("model-name");
        SetCarsPerHourProvider.Result result = SetCarsPerHourProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, itemName);
    }

    // execute AddDefectProvider
    private void addDefect(Context context) {
        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object surfaceField = json.get("surfaceName");
        if (surfaceField == null || !(surfaceField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object nameField = json.get("defectName");
        if (nameField == null || !(nameField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object typeField = json.get("type");
        if (typeField == null || !(typeField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object descriptionField = json.get("description");
        if (descriptionField == null || !(descriptionField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object locationField = json.get("location");
        if (locationField == null || !(locationField instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        JSONArray location = (JSONArray)locationField;
        if (location.size() != 3) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (!(  (location.get(0) != null && location.get(0) instanceof Number) && 
                (location.get(1) != null && location.get(1) instanceof Number) && 
                (location.get(2) != null && location.get(2) instanceof Number))) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        AddDefectProvider.Request request = new AddDefectProvider.Request();
        request.surfaceName = (String)surfaceField;
        request.defectName = (String)nameField;
        request.defectType = (String)typeField;
        request.description = (String)descriptionField;
        request.location = new ArrayList<>(3);
        request.location.add(((Number)location.get(0)).doubleValue());
        request.location.add(((Number)location.get(1)).doubleValue());
        request.location.add(((Number)location.get(2)).doubleValue());
        System.out.println(request.location.size());

        AddDefectProvider.Result result = AddDefectProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);
    } 

    // execute RemoveDefectProvider
    private void removeDefect(Context context) {
        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object nameField = json.get("defectName");
        if (nameField == null || !(nameField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        RemoveDefectProvider.Request request = new RemoveDefectProvider.Request();
        request.defectName = (String)nameField;

        RemoveDefectProvider.Result result = RemoveDefectProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);
    } 

    // execute GetDefectProvider
    private void getDefects(Context context) {

        GetDefectsProvider.Request request = new GetDefectsProvider.Request();
        
        List<String> surfaceNames = context.queryParams("surface-names");
        if (surfaceNames.size() > 0)
            request.surfaceNames = surfaceNames;

        List<String> types = context.queryParams("types");
        if (types.size() > 0)
            request.types = types;

        List<String> aabbMin = context.queryParams("aabb-min");
        if (aabbMin.size() > 0)
            request.aabbMin = aabbMin.stream().map(axis -> Double.parseDouble(axis)).collect(Collectors.toList());

        List<String> aabbMax = context.queryParams("aabb-max");
        if (aabbMax.size() > 0)
            request.aabbMax = aabbMax.stream().map(axis -> Double.parseDouble(axis)).collect(Collectors.toList());

        GetDefectsProvider.Result result = GetDefectsProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null || result.names == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        ArrayList<Object> list = new ArrayList<>(result.names.size());

        for (int i = 0; i < result.names.size(); i++) {
            HashMap<String, Object> instance = new HashMap<>();
            instance.put("name", result.names.get(i));
            instance.put("type", result.types.get(i));
            instance.put("surface", result.surfaces.get(i));
            instance.put("description", result.descriptions.get(i));
            instance.put("location", result.locations.get(i));

            list.add(instance);
        }

        HashMap<String, Object> output = new HashMap<>();
        output.put("defects", list);

        context.result(new JSONObject(output).toJSONString());
    }

    // execute DecayProvider
    private void decay(Context context) {


        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object simulationHoursField = json.get("simulationHours");
        if (simulationHoursField == null || !(simulationHoursField instanceof Number)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        int simulationHours = ((Number)simulationHoursField).intValue();

        if (simulationHours < 1) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        
        DecayProvider.Request request = new DecayProvider.Request();
        request.simulationHours = simulationHours;
        
        String modelName = context.pathParam("model-name");
        DecayProvider.Result result = DecayProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        context.status(HttpStatus.OK);
        
        notifyStaticWebSocket(modelName, "*");
    }

    // execute Backup on model store
    private void backup(Context context) {
        try {
            RoadModelStore.instance.snapshotModel(context.pathParam("model-name"));
        }
        catch (ModelStoreException ex) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        
        context.status(HttpStatus.OK);
    }

    // execute Restore on model store
    private void restore(Context context) {
        String modelName = context.pathParam("model-name");
        try {
            RoadModelStore.instance.restoreModel(modelName);
        }
        catch (ModelStoreException ex) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        
        context.status(HttpStatus.OK);

        notifyStaticWebSocket(modelName, "*");
        notifyDroneWebSocket(modelName, "*", "reload");
    }

    // execute AddDroneProvider
    private void addDrone(Context context) {
        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object nameField = json.get("droneName");
        if (nameField == null || !(nameField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object positionField = json.get("position");
        if (positionField == null || !(positionField instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        JSONArray position = (JSONArray)positionField;
        if (position.size() != 3) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (!(  (position.get(0) != null && position.get(0) instanceof Number) && 
                (position.get(1) != null && position.get(1) instanceof Number) && 
                (position.get(2) != null && position.get(2) instanceof Number))) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        String droneName = (String)nameField;
        AddDroneProvider.Request request = new AddDroneProvider.Request();
        request.droneName = droneName;
        request.position = new ArrayList<>(3);
        request.position.add(((Number)position.get(0)).doubleValue());
        request.position.add(((Number)position.get(1)).doubleValue());
        request.position.add(((Number)position.get(2)).doubleValue());

        String modelName = context.pathParam("model-name");
        AddDroneProvider.Result result = AddDroneProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyDroneWebSocket(modelName, droneName, "add");
    } 

    // execute RemoveDefectProvider
    private void removeDrone(Context context) {
        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object nameField = json.get("droneName");
        if (nameField == null || !(nameField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        String droneName = (String)nameField;
        RemoveDroneProvider.Request request = new RemoveDroneProvider.Request();
        request.droneName = droneName;

        String modelName = context.pathParam("model-name");
        RemoveDroneProvider.Result result = RemoveDroneProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);

        notifyDroneWebSocket(modelName, droneName, "remove");
    } 

    // execute GetDefectProvider
    private void updateDrone(Context context) {

        if (context.contentType() != "application/json") {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        JSONParser parser = new JSONParser();
        Object parsed;
        try {
            parsed = parser.parse(context.body());
        }
        catch (ParseException ex) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        if (parsed instanceof JSONArray) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        JSONObject json = (JSONObject)parsed;

        Object nameField = json.get("droneName");
        if (nameField == null || !(nameField instanceof String)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        Object positionField = json.get("position");
        if (positionField == null || !(positionField instanceof JSONArray)) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        JSONArray position = (JSONArray)positionField;
        if (position.size() != 3) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (!(  (position.get(0) != null && position.get(0) instanceof Number) && 
                (position.get(1) != null && position.get(1) instanceof Number) && 
                (position.get(2) != null && position.get(2) instanceof Number))) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        String droneName = (String)nameField;
        UpdateDroneProvider.Request request = new UpdateDroneProvider.Request();
        request.droneName = droneName;
        request.position = new ArrayList<>(3);
        request.position.add(((Number)position.get(0)).doubleValue());
        request.position.add(((Number)position.get(1)).doubleValue());
        request.position.add(((Number)position.get(2)).doubleValue());

        String modelName = context.pathParam("model-name");
        UpdateDroneProvider.Result result = UpdateDroneProvider.instance.process(modelName, request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        };

        context.status(HttpStatus.OK);


        notifyDroneWebSocket(modelName, droneName, "update");
    }

    // execute GetDefectProvider
    private void getDrone(Context context) {

        GetDroneProvider.Request request = new GetDroneProvider.Request();
        request.droneName = context.pathParam("drone-name");

        GetDroneProvider.Result result = GetDroneProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        HashMap<String, Object> output = new HashMap<>();
        output.put("position", result.position);

        context.result(new JSONObject(output).toJSONString());
    }

    // execute GetDefectProvider
    private void getDrones(Context context) {

        GetDronesProvider.Request request = new GetDronesProvider.Request();

        GetDronesProvider.Result result = GetDronesProvider.instance.process(context.pathParam("model-name"), request).result;
        if (result == null || result.names == null) {
            context.status(HttpStatus.BAD_REQUEST);
            return;
        }

        ArrayList<Object> list = new ArrayList<>(result.names.size());

        for (int i = 0; i < result.names.size(); i++) {
            HashMap<String, Object> instance = new HashMap<>();
            instance.put("name", result.names.get(i));
            instance.put("position", result.positions.get(i));

            list.add(instance);
        }

        HashMap<String, Object> output = new HashMap<>();
        output.put("drones", list);

        context.result(new JSONObject(output).toJSONString());
    }

}
