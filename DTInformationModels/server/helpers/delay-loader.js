AFRAME.registerComponent('delay-coordinator', {
    schema: { type: 'array'},

    init: function () {
        this.load(0);
    },

    load: function (index) {
        // last element is always empty (extra comma at the end)
        if (index >= this.data.length - 1)
            return;

        let detail = this.data.at(index);

        let attribute = detail.split('%');
        let e = document.createElement('a-entity');
        e.setAttribute('color-manage', '');
        e.setAttribute('select-manage', {
            itemID: attribute[0],
            area: '#detail-area',
            close: '#close',
            change: '#change',
            reload: '#reload'
        });
        e.setAttribute('position', {
            x: attribute[1],
            y: attribute[2],
            z: attribute[3]
        });
        e.setAttribute('scale', {
            x: 0.1,
            y: 0.1,
            z: 0.1
        });
        e.setAttribute('gltf-model', '#' + attribute[0]);
        e.addEventListener('model-loaded', () => { this.load(index + 1); }, { once: true });
        this.el.sceneEl.appendChild(e);
    }
})

AFRAME.registerComponent('delay-loader', {
    schema: {
        assetID: {type: 'string'},
        delay: {type: 'number'}
    },

    init: function () {
        this.el.sceneEl.emit()

        setTimeout(() => {
            this.el.setAttribute('gltf-model', this.data.assetID);
        }, this.data.delay);
    }
});
