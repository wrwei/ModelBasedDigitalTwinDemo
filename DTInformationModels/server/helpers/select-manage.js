AFRAME.registerComponent('select-coordinate', {
    init: function () {
        this.current = null;

        this.el.sceneEl.addEventListener('selectset', (evt) => this.set(evt.detail));
    },

    set: function (active) {

        if (this.current)
            this.current.emit('unset');

        this.current = active;
    },
});

AFRAME.registerComponent('update-socketer', {
    schema: {
        
    },

    init: function () {
        
    },

    update: function () {
      // Do something when component's data is updated.
    },

    remove: function () {
      // Do something the component or its entity is detached.
    },

    tick: function (time, timeDelta) {
      // Do something on every scene tick or frame.
    }
});


AFRAME.registerComponent('select-manage', {
    schema: {
        itemID: {type: 'string'},
        isDrone: {type: 'boolean', default: false},
        area: {type: 'selector'},
        close: {type: 'selector'},
        change: {type: 'selector'},
        reload: {type: 'selector'}
    },

    createLabel: function (text) {
        const element = document.createElement('label');
        element.classList.add('form-label');
        element.classList.add('mb-2');
        element.classList.add('text-white');
        element.textContent = text;
        return element;
    },

    createInput: function (placeholder = "", value = "", enabled = true) {
        const element = document.createElement('input');
        element.classList.add('form-control');
        element.classList.add('mb-1');
        element.disabled = !enabled;
        element.type = 'text';
        element.placeholder = placeholder;
        element.value = value;
        return element;
    },

    createSpacer: function (size = 3) {
        const element = document.createElement('div');
        element.classList.add(`mb-${size}`);
        return element;
    },

    createStandardField: function (area, title, placeholder, value, enabled, expanded, effect) {

        area.appendChild(this.createLabel(title));

        const sectionName = title.toLowerCase().replace(" ", "-");

        const expand = document.createElement('button');
        expand.classList.add('expand-section');
        expand.type = 'button';
        expand.setAttribute('data-toggle', 'collapse');
        expand.setAttribute('data-target', '#' + sectionName);
        expand.addEventListener('click', effect);
        area.appendChild(expand);

        const container = document.createElement('div');
        container.classList.add('collapse');
        if (expanded)
            container.classList.add('show');
        container.id = sectionName;

        const newField = this.createInput(placeholder, value, enabled);
        newField.classList.add('collapse');
        if (expanded)
            newField.classList.add('show');
        newField.id = sectionName;
        container.appendChild(newField);
        area.appendChild(container);
        area.appendChild(this.createSpacer(3));

        return newField;
    },

    createListField: function (area, title, placeholder, values, enabled, expanded, effect) {
        let newFields = [];

        area.appendChild(this.createLabel(title));

        const sectionName = title.toLowerCase().replace(" ", "-");

        const expand = document.createElement('button');
        expand.classList.add('expand-section');
        expand.type = 'button';
        expand.setAttribute('data-toggle', 'collapse');
        expand.setAttribute('data-target', '#' + sectionName);
        expand.addEventListener('click', effect);
        area.appendChild(expand);

        const container = document.createElement('div');
        container.classList.add('collapse');
        if (expanded)
            container.classList.add('show');
        container.id = sectionName;

        for (const value of values) {
            const newField = this.createInput(placeholder, value, enabled);
            newFields.push(newField);
            container.appendChild(newField);
            container.appendChild(this.createSpacer(1));
        }
        
        area.appendChild(container);

        area.appendChild(this.createSpacer(3));

        return newFields;
    },

    createDropdownInner: function (dropdown, selected, options, enabled) {

        const toggle = document.createElement('button'); 
        toggle.classList.add('btn');
        toggle.classList.add('btn-secondary');
        toggle.classList.add('dropdown-toggle');
        toggle.type = 'button';
        toggle.disabled = !enabled;
        toggle.setAttribute('data-bs-toggle', 'dropdown');
        toggle.textContent = selected;
        dropdown.appendChild(toggle);

        const menu = document.createElement('ul');
        menu.classList.add('dropdown-menu');

        for (const option of options) {
            const item = document.createElement('li'); 
            
            const choice = document.createElement('button'); 
            choice.classList.add('dropdown-item');
            choice.textContent = option;
            choice.onclick = (ev) => {
                toggle.textContent = option;
            }
            item.appendChild(choice);

            menu.appendChild(item);
        }
        dropdown.appendChild(menu);

        return toggle;
    },

    createDropdown: function (area, title, selected, options, enabled, expanded, effect) {

        area.appendChild(this.createLabel(title));

        const sectionName = title.toLowerCase().replace(" ", "-");

        const expand = document.createElement('button');
        expand.classList.add('expand-section');
        expand.type = 'button';
        expand.setAttribute('data-toggle', 'collapse');
        expand.setAttribute('data-target', '#' + sectionName);
        expand.addEventListener('click', effect);
        area.appendChild(expand);

        const container = document.createElement('div');
        container.classList.add('collapse');
        if (expanded)
            container.classList.add('show');
        container.id = sectionName;

        const dropdown = document.createElement('div');
        dropdown.classList.add('dropdown');
        const toggle = this.createDropdownInner(dropdown, selected, options, enabled);

        container.appendChild(dropdown);
        area.appendChild(container);

        area.appendChild(this.createSpacer(3));

        return toggle;
    },

    createVectorField: function (area, title, values, enabled, expanded, effect) {

        let newAxis = [];

        area.appendChild(this.createLabel(title));

        const sectionName = title.toLowerCase().replace(" ", "-");

        const expand = document.createElement('button');
        expand.classList.add('expand-section');
        expand.type = 'button';
        expand.setAttribute('data-toggle', 'collapse');
        expand.setAttribute('data-target', '#' + sectionName);
        expand.addEventListener('click', effect);
        area.appendChild(expand);

        const container = document.createElement('div');
        container.classList.add('collapse');
        if (expanded)
            container.classList.add('show');
        container.id = sectionName;
        
        const group = document.createElement('div');
        group.classList.add('input-group');
        group.classList.add('mb-1');

        for (const axis of ['x', 'y', 'z']) {
            const span = document.createElement('span');
            span.classList.add('input-group-text');
            span.textContent = axis;
            group.appendChild(span);
    
            const input = document.createElement('input');
            input.classList.add('form-control');
            input.type = 'text';
            input.placeholder = values[axis];
            input.disabled = !enabled;
            newAxis.push(input);
            group.appendChild(input);
        }

        container.appendChild(group);
        area.appendChild(container);
        
        area.appendChild(this.createSpacer(3));

        return newAxis;
        
    },

    createApplyButton: function (area) {

        const container = document.createElement('div');
        container.classList.add('d-grid');
        container.classList.add('gap-2');

        const apply = document.createElement('button'); 
        apply.classList.add('btn');
        apply.classList.add('btn-primary');
        apply.textContent = "Apply";
        container.appendChild(apply);

        area.appendChild(container);

        return apply;
    },

    createMaterialElement: function (area, value, options, enabled) {

        const group = document.createElement('div');
        group.classList.add('input-group');
        group.classList.add('mb-1');

        const toggle = this.createDropdownInner(group, value.type, options.type, enabled);
        toggle.style.width = '12rem';
        toggle.style.textAlign = 'left';

        const input = document.createElement('input');
        input.classList.add('form-control');
        input.type = 'text';
        input.placeholder = "Quantity";
        input.value = Math.round(value.quantity * 100) / 100;
        input.disabled = !enabled;
        group.appendChild(input);

        const remove = document.createElement('button');
        remove.classList.add('btn');
        remove.classList.add('btn-danger');
        remove.style.borderTopRightRadius = 'var(--bs-btn-border-radius)';
        remove.style.borderBottomRightRadius = 'var(--bs-btn-border-radius)';
        remove.textContent = 'X';
        group.appendChild(remove);

        area.appendChild(group);
        group.appendChild(this.createSpacer(1));

        return {toggle, input, group, remove};
    },

    createMaterialList: function (area, title, values, options, enabled, expanded, effect) {

        let newFields = [];

        area.appendChild(this.createLabel(title));

        const sectionName = title.toLowerCase().replace(" ", "-");

        const expand = document.createElement('button');
        expand.classList.add('expand-section');
        expand.type = 'button';
        expand.setAttribute('data-toggle', 'collapse');
        expand.setAttribute('data-target', '#' + sectionName);
        expand.addEventListener('click', effect);
        area.appendChild(expand);

        const container0 = document.createElement('div');
        container0.classList.add('collapse');
        if (expanded)
            container0.classList.add('show');
        container0.id = sectionName;

        const container1 = document.createElement('div');

        for (const value of values) {
            newFields.push(this.createMaterialElement(container1, value, options, enabled));
        }
        container1.appendChild(this.createSpacer(1));

        container0.appendChild(container1);

        const container2 = document.createElement('div');
        container2.classList.add('d-grid');
        container2.classList.add('gap-2');

        const addButton = document.createElement('button'); 
        addButton.classList.add('btn');
        addButton.classList.add('btn-success');
        addButton.textContent = "+";
        container2.appendChild(addButton);
        container0.appendChild(container2);

        area.appendChild(container0);

        area.appendChild(this.createSpacer(3));

        return {newFields, container: container1, addButton};
    },

    unset: function () {
        const area = this.data.area;

        area.setAttribute('style', 'display: none;');
        this.el.emit('grayOut');
        this.ws.close();

        this.data.close.removeEventListener('click', this.unsetBind);
        this.data.reload.removeEventListener('click', this.reloadBind);
    },

    populate: function () {

        let applyList = [];

        const area = this.data.area;

        const url = new URL(`${document.location.origin}/getDetail/${targetModel}/${this.data.itemID}.json`);

        fetch(url)
        .then(res => res.json())
        .then(json => {


            this.el.emit('highlight');

            area.setAttribute('style', 'display: none;');
            area.innerHTML = '';

            const self = this;

            if (self.nameExpand === undefined) 
                self.nameExpand = true;
            const nameField = this.createStandardField(area, "Item Name", "name of item", this.data.itemID, false, self.nameExpand, () => {
                self.nameExpand = !self.nameExpand;
            });
            
            if (self.sectionExpand === undefined) 
                self.sectionExpand = true;
            const sectionFields = this.createListField(area, "Part of Section", "name of section", json.section, false, self.sectionExpand, () => {
                self.sectionExpand = !self.sectionExpand;
            });

            if ('type' in json.attributes) {

                if (self.typeExpand === undefined) 
                    self.typeExpand = true;
                const typeField = this.createStandardField(area, 'Type', '', json.attributes['type'].data[0]['type'], false, self.typeExpand, () => {
                    self.typeExpand = !self.typeExpand;
                });
            }
            if ('subtype' in json.attributes) {

                if (self.subtypeExpand === undefined) 
                    self.subtypeExpand = true;
                const subtypeField = this.createStandardField(area, 'Sub Type', '', json.attributes['subtype'].data[0]['subtype'], false, self.subtypeExpand, () => {
                    self.subtypeExpand = !self.subtypeExpand;
                });
            }
            if ('location' in json.attributes) {

                if (self.locationExpand === undefined) 
                    self.locationExpand = true;
                const locationFields = this.createVectorField(area, 'Location', json.attributes['location'].data[0], false, self.locationExpand, () => {
                    self.locationExpand = !self.locationExpand;
                });
            }
            if ('color' in json.attributes) {
                if (self.colorExpand === undefined) 
                    self.colorExpand = true;
                const colorOption = this.createDropdown(area, 'Color', json.attributes['color'].data[0]['color'], json.attributes['color'].options['color'], false, self.colorExpand, () => {
                    self.colorExpand = !self.colorExpand;
                });
            }
            if ('lanes' in json.attributes) {

                if (self.lanesExpand === undefined) 
                    self.lanesExpand = true;
                const lanesField = this.createStandardField(area, 'Lanes', '', json.attributes['lanes'].data[0]['lanes'], true, self.lanesExpand, () => {
                    self.lanesExpand = !self.lanesExpand;
                });

                applyList.push(() => {
                    fetch(new URL(`${document.location.origin}/setLanes/${targetModel}/${this.data.itemID}`), {
                        method: "POST",
                        body: JSON.stringify({
                            lanes: Number(lanesField.value)
                        }),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                });

            }
            if ('carsPerHour' in json.attributes) {

                if (self.carsPerHourExpand === undefined) 
                    self.carsPerHourExpand = true;
                const carsPerHourField = this.createStandardField(area, 'Cars/Hour', '', json.attributes['carsPerHour'].data[0]['carsPerHour'], true, self.carsPerHourExpand, () => {
                    self.carsPerHourExpand = !self.carsPerHourExpand;
                });

                applyList.push(() => {
                    fetch(new URL(`${document.location.origin}/setCarsPerHour/${targetModel}/${this.data.itemID}`), {
                        method: "POST",
                        body: JSON.stringify({
                            carsPerHour: Number(carsPerHourField.value)
                        }),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                });

            }
            if ('span' in json.attributes) {

                if (self.spanExpand === undefined) 
                    self.spanExpand = true;
                const spanField = this.createStandardField(area, 'Surface Area', '', Math.round(json.attributes['span'].data[0]['span'] * 100) / 100, false, self.spanExpand, () => {
                    self.spanExpand = !self.spanExpand;
                });
            }
            if ('topMaterial' in json.attributes) {

                const options = json.attributes['topMaterial'].options;

                if (self.topMaterialExpand === undefined) 
                    self.topMaterialExpand = false;
                const {newFields, container, addButton} = this.createMaterialList(area, 'Surface Course', json.attributes['topMaterial'].data, options, true, self.topMaterialExpand, () => {
                    self.topMaterialExpand = !self.topMaterialExpand;
                });

                for (const field of newFields) {
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                }

                addButton.onclick = () => {
                    const field = this.createMaterialElement(container, {type: options.type[0], quantity: 0}, options, true);
                    newFields.push(field);
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                };

                applyList.push(() => {
                    const materials = [];

                    for (const field of newFields) {
                        materials.push({
                            type: field.toggle.textContent,
                            quantity: Number(field.input.value)
                        })
                    }

                    fetch(new URL(`${document.location.origin}/setTopMaterial/${targetModel}/${this.data.itemID}`), {
                        method: "POST",
                        body: JSON.stringify({
                            materials: materials
                        }),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                });

            }
            if ('midMaterial' in json.attributes) {

                const options = json.attributes['midMaterial'].options;
                
                if (self.midMaterialExpand === undefined) 
                    self.midMaterialExpand = false;
                const {newFields, container, addButton} = this.createMaterialList(area, 'Base Course', json.attributes['midMaterial'].data, options, true, self.midMaterialExpand, () => {
                    self.midMaterialExpand = !self.midMaterialExpand;
                });

                for (const field of newFields) {
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                }

                addButton.onclick = () => {
                    const field = this.createMaterialElement(container, {type: options.type[0], quantity: 0}, options, true);
                    newFields.push(field);
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                };

                applyList.push(() => {
                    const materials = [];

                    for (const field of newFields) {
                        materials.push({
                            type: field.toggle.textContent,
                            quantity: Number(field.input.value)
                        })
                    }

                    fetch(new URL(`${document.location.origin}/setMidMaterial/${targetModel}/${this.data.itemID}`), {
                        method: "POST",
                        body: JSON.stringify({
                            materials: materials
                        }),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                });

            }
            if ('lowMaterial' in json.attributes) {

                const options = json.attributes['lowMaterial'].options;
                
                if (self.lowMaterialExpand === undefined) 
                    self.lowMaterialExpand = false;
                const {newFields, container, addButton} = this.createMaterialList(area, 'Subbase', json.attributes['lowMaterial'].data, options, true, self.lowMaterialExpand, () => {
                    self.lowMaterialExpand = !self.lowMaterialExpand;
                });

                for (const field of newFields) {
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                }

                addButton.onclick = () => {
                    const field = this.createMaterialElement(container, {type: options.type[0], quantity: 0}, options, true);
                    newFields.push(field);
                    field.remove.onclick = () => {
                        container.removeChild(field.group);
                        newFields.splice(newFields.indexOf(field), 1);
                    };
                };

                applyList.push(() => {
                    const materials = [];

                    for (const field of newFields) {
                        materials.push({
                            type: field.toggle.textContent,
                            quantity: Number(field.input.value)
                        })
                    }

                    fetch(new URL(`${document.location.origin}/setLowMaterial/${targetModel}/${this.data.itemID}`), {
                        method: "POST",
                        body: JSON.stringify({
                            materials: materials
                        }),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                });

            }
            

            const applyButton = this.createApplyButton(area);

            applyButton.onclick = (ev) => {
                for (const applyFunc of applyList)
                    applyFunc();
            }

            area.removeAttribute('style');
        });
    },

    click: function () {

        this.el.sceneEl.emit('selectset', this.el);
        this.data.change.style.visibility = 'hidden';

        this.data.close.addEventListener('click', this.unsetBind, { once: true });
        this.data.reload.addEventListener('click', this.reloadBind);

        this.ws = new WebSocket(`ws://${document.location.host}/updates/${targetModel}`);

       
        this.ws.onopen = () => {
            this.keepAliveID = setInterval(() => this.ws.send("ALIVE"), 10000);
        }
        this.ws.onmessage = msg => {
            const update = JSON.parse(msg.data);
            if (update.name == this.data.itemID || update.name == "*")
                this.data.change.style.visibility = 'visible';
            return false;
        }
        this.ws.onclose = () => {    
            clearInterval(this.keepAliveID);
        }

        this.populate();
    },

    reload: function() {
        this.data.change.style.visibility = 'hidden';

        this.populate();
    },

    init: function () {

        this.unsetBind = this.unset.bind(this);
        this.reloadBind = this.reload.bind(this);

        this.el.addEventListener('click', this.click.bind(this));

        // called by other select-manage
        this.el.addEventListener('unset', this.unsetBind);
    },
});
