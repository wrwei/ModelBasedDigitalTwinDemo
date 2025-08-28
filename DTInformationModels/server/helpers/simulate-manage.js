AFRAME.registerComponent('simulate-manage', {

    schema: {
        simulate: {type: 'selector'},
        backup: {type: 'selector'},
        restore: {type: 'selector'},
        duration: {type: 'selector'}
    },

    simulate: function () {

        const url = new URL(`${document.location.origin}/decay/${targetModel}`);

        fetch(url, {
            method: "POST",
            body: JSON.stringify({
                simulationHours: Number(this.data.duration.value)
            }),
            headers: {
                "Content-Type": "application/json"
            }
        });
    },

    backup: function () {

        const url = new URL(`${document.location.origin}/management/backup/${targetModel}`);

        fetch(url, {
            method: "POST"
        });
    },

    restore: function () {

        const url = new URL(`${document.location.origin}/management/restore/${targetModel}`);

        fetch(url, {
            method: "POST"
        });
    },

    init: function () {

        this.data.backup.addEventListener('click', this.backup.bind(this));
        this.data.restore.addEventListener('click', this.restore.bind(this));
        this.data.simulate.addEventListener('click', this.simulate.bind(this));
    },
});