# GET /getOffset/{model-name}/{mesh-name}.json
get the global offset of a mesh in a model

return json schema:
``` json
{
    "offset": "list of 3 element for X, Y, Z"
}
```



# GET /getDetails/{model-name}/{item-name}.json
get the details of an item in a model (item like a piece of pavement or fence)

return json schema:
``` json
{        
    "section": "list of strings name of section this item belongs to",
    "attributes": "object with with objects in corresponding named fields"
}
```

a single attribute element:
``` json
{        
    "data": "1 to many list of instances of the data",            
    "options": "object with field names matching instances field, each field is a 1 to many list of options, if not changable, the list can be empty"
}
```

a complete example return:
``` json
{       
    "section": [ "014" ],   
    "time": /*System.currentTimeMillis()*/,    
    "attributes": {        
        "topMaterial": 
        { 
            "data": [  
                { 
                    "quantity": 1000.0,                   
                    "type": "CoarseAggregates"               
                },              
                {           
                    "quantity": 2000.0,                  
                    "type": "Bitumen"               
                }           
            ],
            "options": 
            {               
                "quantity": [],               
                "type": [ "FineAggregates", "CoarseAggregates", "Bitumen", "PortlandCement" ]           
            }       
        },   
        "midMaterial": 
        { 
            "data": [  
                { 
                    "quantity": 1000.0,                   
                    "type": "FineAggregates"               
                },              
                {           
                    "quantity": 2000.0,                  
                    "type": "Bitumen"               
                }           
            ],
            "options": 
            {               
                "quantity": [],               
                "type": [ "FineAggregates", "CoarseAggregates", "Bitumen", "PortlandCement" ]           
            }       
        },        
        "lowMaterial": 
        { 
            "data": [  
                { 
                    "quantity": 1000.0,                   
                    "type": "FineAggregates"               
                },              
                {           
                    "quantity": 2000.0,                  
                    "type": "Bitumen"               
                }           
            ],
            "options": 
            {               
                "quantity": [],               
                "type": [ "FineAggregates", "CoarseAggregates", "Bitumen", "PortlandCement" ]           
            }       
        },      
        "subtype": 
        {           
            "data": [     
                {                   
                    "subtype": "Pavement"               
                }           
            ],           
            "options": 
            {               
                "subtype": []           
            }       
        },       
        "type": {           
            "data": [               
                {                   
                    "type": "Surface"               
                }               
            ],           
            "options": 
            {               
                "type": []           
            }       
        },       
        "mesh":
        {           
            "data": [               
                {                   
                    "file": "Pavement.064",                   
                    "x": 702.2198486328125,                   
                    "y": -280.523193359375,                   
                    "z": -21.784385681152344               
                }           
            ],           
            "options": 
            {               
                "file": [],               
                "x": [],               
                "y": [],               
                "z": []           
            }       
        }   
    }
}
```

# GET /getFiltered/{model-name}.json

get a filtered list of items from a model

request schema:

- optionally include 1 or more section-names=string of section name     
    - returns items which belong to any one of the section provided
- optionally include 1 or more types=string of type name (Asset, Line, Mark) 
    - returns item which belongs to any one of the section provided
- optionally include exactly 3 aabb-min along with exactly 3 aabb-max at the same time
    - first aabb-min is min X, then min Y, then min Z- first aabb-max is max X, then max Y, then max Z



example:
```
http://localhost:7070/getFiltered/Interchange1.json?section-names=001&section-names=002&types=Mark&types=Line&aabb-min=-100&aabb-min=-100&aabb-min=-100&aabb-max=100&aabb-max=100&aabb-max=100
```


# GET /getMesh/{model-name}/{mesh-name}.glb
get glb file of the mesh in a model

# GET /getHelper/{file-name-with-extension}
get helper files, for providing UI functionalities

# GET /aframe/{model-name}/{section-name}.html
get aframe scene of only one section of road in a model

# GET /aframe/{model-name}.html
get aframe scene of all section of road in a model

# GET /browse/{model-name}.html
get a 2D webpage that utilize /getFiltered to query the model

# POST /setTopMaterial/{model-name}/{item-name}
# POST /setMidMaterial/{model-name}/{item-name}
# POST /setLowMaterial/{model-name}/{item-name}

get aframe scene of all section of road in a model

request schema:
``` json
{        
    "materials": "list of material objects"
}
```

material object:
``` json
{        
    "type": "fixed list of material type getDetails provide a list of options",        "quantity": "number, can be decimal, undefined unit"
}
```

example:
``` json
{        
    "materials": [                
        {                        
            "type":"Bitumen",                        
            "quantity":1000                
        },                
        {                        
            "type":"CoarseAggregates",                        
            "quantity":2000                
        }        
    ]
}
```

# GET /getDefects/{model-name}.json

return schema:
``` json
{             
    "defects": [                
        {                        
            "surfaceName": "name of surface defect is part of",                            
            "defectName": "name of defec",                            
            "type": "name of type matching enum, currently only Potholes",
            "description": "string description",                         
            "location": [/*x*/, /*y*/, /*z*/]        
        },              
        // ...        
    ]   
}
```

# POST /addDefect/{model-name}

request schema:
``` json
{        
    "surfaceName": "name of surface defect is part of",    
    "defectName": "name of defect, has to be unique, will get BADREQUEST if not",    
    "type": "name of type matching enum, currently only Potholes",    
    "description": "string description",                             
    "location": [/*x*/, /*y*/, /*z*/]        
}
```


# POST /removeDefect/{model-name}

request schema:
``` json
{        
    "defectName": "name of defect"
}
```


# POST /randomDecay/{model-name}

request schema:
``` json
{        
    "simulationHours": 70 //(greater than 0.0)
}
```

# POST /management/backup/{model-name}

create a backup of the model

# POST /management/restore/{model-name}

restore the backup of the model

# GET /getDrone/{model-name}/{drone-name}.json

get drone position

return schema:
``` json
{
    "position": [/*x*/, /*y*/, /*z*/]    
}
```

# GET /getDrones/{model-name}.json

get all drones in model

return schema:
``` json
{
    "drones":[
        {
            "name":"Drone.002",
            "position": [/*x*/, /*y*/, /*z*/]  
        },
        {
            "name":"Drone.001",
            "position": [/*x*/, /*y*/, /*z*/]  
        }
    ]
}
```

# POST /addDrone/{model-name}

request schema:
``` json
{
    "droneName": "Drone.002 use this format", 
    "position": [/*x*/, /*y*/, /*z*/]  
}
```

# POST /updateDrone/{model-name}

request schema:
``` json
{
    "droneName": "Drone.002 use this format", 
    "position": [/*x*/, /*y*/, /*z*/]  
}
```

# POST /removeDrone/{model-name}

request schema:
``` json
{
    "droneName": "Drone.002 use this format"
}
```

# WebSocket /updates/{model-name}

notify when an item has changed
as per normal websocket protocol, 
client needs to send any message within every 20 seconds to keep connection alive
client expected to fetch updated item details from REST endpoint

example client:
``` js
this.ws = new WebSocket(`ws://${document.location.host}/updates/${targetModel}`);

this.ws.onopen = () => {
    this.keepAliveID = setInterval(() => this.ws.send("ALIVE"), 10000);
}
this.ws.onmessage = msg => {
    const update = JSON.parse(msg.data);
    // do work
    return false;
}
this.ws.onclose = () => {    
    clearInterval(this.keepAliveID);
}
```

update message:
``` json
{
    "name": "item name or '*' for any, i.e. Pavement.006",
    "time": /*System.currentTimeMillis()*/
}
```

# WebSocket /drones/{model-name}

notify when any drone change
as per normal websocket protocol, 
client needs to send any message within every 20 seconds to keep connection alive
client expected to fetch updated item details from REST endpoint

example client:
``` js
this.ws = new WebSocket(`ws://${document.location.host}/drones/${targetModel}`);

this.ws.onopen = () => {
    this.keepAliveID = setInterval(() => this.ws.send("ALIVE"), 10000);
}
this.ws.onmessage = msg => {
    const update = JSON.parse(msg.data);
    switch (update.change) {
        case "reload":
            // delete all local drones
            // fetch all drones
            break;
        case "add":
            // add one drone
            break;
        case "remove":
            // delete one drone
            break;
        case "update":
            // update one drone
            break;
    }
    return false;
}
this.ws.onclose = () => {    
    clearInterval(this.keepAliveID);
}

// fetch all drones on start
```

update message:
``` json
{
    "change": "reload/add/remove/update",
    "name": "item name or '*' for any, i.e. Drone.001",
    "time": /*System.currentTimeMillis()*/
}
```