var sectionList = [];
var typeList = ['assets', 'lines', 'marks'];

let conversionTable = {
    assets: 'Asset',
    lines: 'Line',
    marks: 'Mark'
};

function intersectCallback(entries, observer) {
    entries.forEach((entry) => {
        const containerEl = entry.target;
        const nameEl = containerEl.querySelector('#'+'element-name');
        const typeEl = containerEl.querySelector('#'+'element-type');
        const sectionEl = containerEl.querySelector('#'+'element-section');

        const url = new URL(`${document.location.origin}/getDetail/${targetModel}/${nameEl.textContent}.json`);
            
        fetch(url)
        .then(res => res.json())
        .then(json => {
            if (json.attributes && json.attributes.type)
                typeEl.textContent = json.attributes.type.data[0].type;
            if (json.section && Array.isArray(json.section))
                sectionEl.textContent = json.section.join(',');
        });
    });
}

function addSection() {
    const sectionName = document.getElementById('section-entry').value;
    if (sectionName.length < 1)
        return;
    if (sectionList.includes(sectionName))
        return;

    const container = document.getElementById('section-list');

    const outer = document.createElement('div');
    outer.classList.add('input-group');
    outer.classList.add('mb-1');

    const text = document.createElement('input');
    text.classList.add('form-control');
    text.placeholder = sectionName;
    text.disabled = true;
    outer.appendChild(text);

    const remove = document.createElement('button');
    remove.classList.add('btn');
    remove.classList.add('btn-danger');
    remove.textContent = 'X';
    remove.onclick = (ev) => {
        container.removeChild(outer);
        sectionList.splice(sectionList.indexOf(sectionName), 1);
    }
    outer.appendChild(remove);

    container.appendChild(outer);
    sectionList.push(sectionName);
}

function addType(type) {
    const target = !typeList.includes(type);
    const toggle = document.getElementById('type-toggle-' + type);
    if (target) {
        if (!toggle.classList.contains('active'))
            toggle.classList.add('active');
        typeList.push(type);
    }
    else {
        if (toggle.classList.contains('active'))
            toggle.classList.remove('active');
        typeList.splice(typeList.indexOf(type), 1);
    }
}

function getAABB() {
    let output = {
        minX: document.getElementById('aabb-min-x').value,
        minY: document.getElementById('aabb-min-y').value,
        minZ: document.getElementById('aabb-min-z').value,
        maxX: document.getElementById('aabb-max-x').value,
        maxY: document.getElementById('aabb-max-y').value,
        maxZ: document.getElementById('aabb-max-z').value
    }

    if (output.minX.length == 0 || output.minY.length == 0 || output.minZ.length == 0 || 
        output.maxX.length == 0 || output.maxY.length == 0 || output.maxZ.length == 0)
        return null;
    else
        return output;
}

function filter() {
    const url = new URL(`${document.location.origin}/getFiltered/${targetModel}.json`);
    for (const section of sectionList)
        url.searchParams.append('section-names', section);
    for (const type of typeList) {
        console.log(type);
        url.searchParams.append('types', conversionTable[type]);
    }
    const aabb = getAABB();
    if (aabb != null) {
        url.searchParams.append('aabb-min', aabb.minX);
        url.searchParams.append('aabb-min', aabb.minY);
        url.searchParams.append('aabb-min', aabb.minZ);
        url.searchParams.append('aabb-max', aabb.maxX);
        url.searchParams.append('aabb-max', aabb.maxY);
        url.searchParams.append('aabb-max', aabb.maxZ);
    }

    console.log(url.toString());


    fetch(url)
    .then(res => res.json())
    .then(json => {
        console.log(json);
        const matchedArea = document.getElementById('matched-area');

        matchedArea.innerHTML = '';
        if (this.intersectObserver)
            this.intersectObserver.disconnect();
        this.intersectObserver = new IntersectionObserver(intersectCallback, {
            root: null,
            rootMargin: '0px',
            threshold: 0.1
        });

        for (const name of json.matched) {

            const containerEl = document.createElement('tr');

            const nameEl = document.createElement('th');
            nameEl.id = "element-name";
            nameEl.textContent = name;
            containerEl.appendChild(nameEl);

            const typeEl = document.createElement('td');
            typeEl.id = "element-type";
            containerEl.appendChild(typeEl);

            const sectionEl = document.createElement('td');
            sectionEl.id = "element-section";
            containerEl.appendChild(sectionEl);

            matchedArea.appendChild(containerEl);

            this.intersectObserver.observe(containerEl);
        }
    })
}