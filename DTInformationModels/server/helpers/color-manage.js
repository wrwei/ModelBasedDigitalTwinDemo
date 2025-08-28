AFRAME.registerComponent('color-manage', {
    schema: {
        grayOutColor: { type: 'color', default: '#808080'},
        regularColor: { type: 'color', default: '#ffffff'},
    },

    init: function () {
        this.grayOutObj = new THREE.Color(this.data.grayOutColor);
        this.regularObj = new THREE.Color(this.data.regularColor);

        this.el.addEventListener('object3dset', this.objectSet.bind(this));

        this.el.addEventListener('grayOut', this.grayOut.bind(this));
        this.el.addEventListener('highlight', this.highlight.bind(this));
    },

    update: function (oldData) {
        if (oldData.greyOutColor != this.data.greyOutColor)
            this.grayOutObj.set(this.data.greyOutColor);

        if (oldData.regularColor != this.data.regularColor)
            this.regularObj.set(this.data.regularColor);

        if (this.grayed !== undefined) {
            if (this.grayed)
                this.grayOut();
            else
                this.highlight();
        }
    },

    objectSet: function () {
        this.loaded = true;
        this.grayOut();
    },

    grayOut: function () {
        if (!this.loaded) return;

        this.grayed = true;
        this.setColor(this.grayOutObj);
    },

    highlight: function () {
        if (!this.loaded) return;

        this.grayed = false;
        this.setColor(this.regularObj);
    },

    setColor: function (color) {
        const mesh = this.el.getObject3D('mesh');
        if (!mesh) return;

        mesh.traverse((node) => {
            if (node.material && node.material.color)
                node.material.color = color;
        })
    }
});
