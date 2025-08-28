AFRAME.registerComponent('drone-manage', {

    init: function () {

        this.droneEls = {};
        
        this.ws = new WebSocket(`ws://${document.location.host}/drones/${targetModel}`);

       
        this.ws.onopen = () => {
            this.keepAliveID = setInterval(() => this.ws.send("ALIVE"), 10000);
        }
        this.ws.onmessage = msg => {
            const update = JSON.parse(msg.data);
            switch (update.change) {
                case "reload":
                    for (const droneKey in this.droneEls) {
                        this.droneEls[droneKey].remove();
                        delete this.droneEls[droneKey];
                    }
                    this.fetchAll();
                    break;
                case "add":
                    this.fetchOne(update.name);
                    break;
                case "remove":
                    this.deleteOne(update.name);
                    break;
                case "update":
                    this.fetchOne(update.name);
                    break;
            }
            return false;
        }
        this.ws.onclose = () => {    
            clearInterval(this.keepAliveID);
        }

        this.fetchAll();

    },

    fetchAll: function () {
        const url = new URL(`${document.location.origin}/getDrones/${targetModel}.json`);

        fetch(url)
        .then(res => res.json())
        .then(json => {
            for (const drone of json.drones) 
            {
                const droneEl = document.createElement("a-box");
                droneEl.id = drone.name;
                droneEl.setAttribute('position', {x: drone.position[0], y: drone.position[1], z: drone.position[2]});
                this.el.sceneEl.appendChild(droneEl);
                this.droneEls[drone.name] = droneEl;
            }
        });
    },

    fetchOne: function (targetDrone) {
        const url = new URL(`${document.location.origin}/getDrone/${targetModel}/${targetDrone}.json`);

        fetch(url)
        .then(res => res.json())
        .then(json => {
            if (targetDrone in this.droneEls)
                this.droneEls[targetDrone].setAttribute('position', {x: json.position[0], y: json.position[1], z: json.position[2]});
            else
            {
                const droneEl = document.createElement("a-box");
                droneEl.id = targetDrone;
                droneEl.setAttribute('position', {x: json.position[0], y: json.position[1], z: json.position[2]});
                this.el.sceneEl.appendChild(droneEl);
                this.droneEls[targetDrone] = droneEl;
            }
        });
    },

    deleteOne: function (targetDrone) {
        if (targetDrone in this.droneEls)
            this.droneEls[targetDrone].remove();
            delete this.droneEls[targetDrone];
    }
});
