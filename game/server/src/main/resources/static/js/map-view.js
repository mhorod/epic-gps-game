class Position {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

class WorldMap {
    constructor() {
        this.map = L.map("map").setView([50.03, 19.9], 10);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(this.map);
        this.markers = new Set()

        this.map.on("zoomend", () => {
            const zoom = this.map.getZoom();
            for (const marker of this.markers) {
                const icon = marker.options.icon;
                icon.options.iconSize = [zoom * 3, zoom * 3]
                marker.setIcon(icon);
            }
        })
    }

    zoomOn(position) {
        this.map.setView({lat: position.latitude, lng: position.longitude}, 16)
    }

    addMarker(marker) {
        this.markers.add(marker)
    }

    removeMarker(marker) {
        this.markers.remove(marker)
    }
}

class EntityList {
    constructor(map) {
        this.map = map
        this.enemyMap = new Map();
        this.playerMap = new Map();
        this.infoElement = document.getElementById("entity-info")
    }

    addEnemy(enemy) {
        const enemyElement = document.createElement("li");
        enemyElement.onclick = () => this.map.zoomOn(enemy.position)
        enemyElement.innerText = enemy.name;

        let marker = L.marker([enemy.position.latitude, enemy.position.longitude], {icon: frogIcon}).addTo(map.map);
        this.map.addMarker(marker)
        marker.on("click", () => { this.showEnemyInfo(enemy.enemyId.id); })

        this.enemyMap.set(enemy.enemyId.id, enemy)
    }

    addPlayer(player) {
        const playerElement = document.createElement("li");
        playerElement.onclick = () => this.map.zoomOn(player.position)
        playerElement.innerText = player.player.name;

        let marker = L.marker([player.position.latitude, player.position.longitude], {icon: warriorIcon}).addTo(map.map);
        this.map.addMarker(marker)
        marker.on("click", () => { this.showPlayerInfo(player.player.name); })

        this.playerMap.set(player.player.name, {
            player: player.player,
            position: player.position,
            marker: marker
        })
    }

    updatePlayer(player) {
        const name = player.player.name;
        if (!this.playerMap.has(player.player.name)) {
            this.addPlayer(player);
        }

        const current = this.playerMap.get(name)
        current.player = player;
        current.position = player.position;
        current.marker.setLatLng([player.position.latitude, player.position.longitude]).update()
    }

    showPlayerInfo(player) {
        this.infoElement.innerHTML = ""
        this.infoElement.append(this.createPlayerInfoElement(player))
        this.infoElement.classList.add("active")
    }

    createPlayerInfoElement(playerName) {
        const player = this.playerMap.get(playerName).player
        const wrapper = document.createElement("div")
        wrapper.innerHTML = `
                    <div class="card-header">
                        <h1> ${player.name} </h1>
                        <span class="card-close-icon" id="entity-info-close-icon">
                            <ion-icon name="close"></ion-icon>
                        </span>
                    </div>
                    <div class="card-content">
                        <span class="light-gray"> Player lvl ${player.lvl} </span>
                        <ul class="entity-stats-list">
                            <li> <ion-icon name="sparkles"> </ion-icon> XP: ${player.xp} </li>
                            <li> <ion-icon name="heart"> </ion-icon> Health: ${player.hp} / ${player.maxHp} </li>
                            <li> <ion-icon name="flash"> </ion-icon> Attack: ${player.attack} </li>
                            <li> <ion-icon name="shield"> </ion-icon> Defense: ${player.defense} </li>
                        </ul>
                    </div>
        `
        wrapper.querySelector("#entity-info-close-icon").addEventListener("click", () => this.hideInfo())
        return wrapper
    }

    showEnemyInfo(enemy) {
        this.infoElement.innerHTML = ""
        this.infoElement.append(this.createEnemyInfoElement(enemy))
        this.infoElement.classList.add("active")
    }

    createEnemyInfoElement(enemyId) {
        const enemy = this.enemyMap.get(enemyId)
        const wrapper = document.createElement("div")
        wrapper.innerHTML = `
                    <div class="card-header">
                        <h1> ${enemy.name} </h1>
                        <span class="card-close-icon" id="entity-info-close-icon">
                            <ion-icon name="close"></ion-icon>
                        </span>
                    </div>
                    <div class="card-content">
                        <span class="light-gray"> Enemy lvl ${enemy.lvl} </span>
                    </div>
        `
        wrapper.querySelector("#entity-info-close-icon").addEventListener("click", () => this.hideInfo())
        return wrapper
    }

    hideInfo() {
        this.infoElement.classList.remove("active")
    }
}

class GameServer {
    constructor(url) {
        this.url = url
    }

    async getEnemies() {
        return await fetch(this.url + "/v1/enemies")
            .then(res => res.json())
            .then(data => data)
            .catch(err => console.log(err));
    }

    async getPlayers() {
        return await fetch(this.url + "/v1/players")
            .then(res => res.json())
            .then(data => data)
            .catch(err => console.log(err));
    }
}

class SearchModal {
    constructor(map, entityList) {
        this.map = map
        this.entityList = entityList

        this.active = false;
        this.searchBar = document.getElementById("search-bar");
        this.searchModal = document.getElementById("search-modal");
        this.searchModalCloseButton = document.getElementById("search-modal-close-icon");
        this.searchResults = document.getElementById("search-results")

        this.searchBar.addEventListener("click", () => this.beginSearch());
        this.searchModalCloseButton.addEventListener("click", () => this.endSearch());
        document.addEventListener("click", e => {
            if (!this.searchBar.contains(e.target) && !this.searchModal.contains(e.target)) {
                this.endSearch();
            }
        })
    }

    beginSearch() {
        this.searchBar.classList.add("active")
        this.searchModal.classList.add("active")
        this.active = true
        this.refreshResults()
    }

    refreshResults() {
        if (!this.active) return;

        this.searchResults.innerHTML = ""
        for (let enemy of this.entityList.enemyMap.values()) {
            const element = this.createSearchResultElement(
                "skull",
                enemy.name,
                "Enemy lvl " + enemy.lvl,
                () => this.map.zoomOn(enemy.position)
            )
            this.searchResults.appendChild(element);
        }

        for (let player of this.entityList.playerMap.values()) {
            const p = player.player
            const element = this.createSearchResultElement(
                "person",
                p.name,
                "Player lvl " + p.lvl,
                () => this.map.zoomOn(player.position)
            )
            this.searchResults.appendChild(element);
        }
    }

    createSearchResultElement(resultIcon, resultName, resultType, onclick) {
        const wrapper = document.createElement("div")
        wrapper.classList.add("search-result")

        const name = document.createElement("div")
        name.classList.add("search-result-name")

        const iconWrapper = document.createElement("div")
        iconWrapper.classList.add("search-result-icon")
        iconWrapper.innerHTML = `<ion-icon name=${resultIcon}></ion-icon>`

        const nameTextWrapper = document.createElement("span")
        nameTextWrapper.innerText = resultName


        const typeWrapper = document.createElement("div")
        typeWrapper.classList.add("search-result-type")
        typeWrapper.innerText = resultType

        name.append(iconWrapper, nameTextWrapper)
        wrapper.append(name, typeWrapper)
        wrapper.addEventListener("click", onclick)
        return wrapper
    }

    endSearch() {
        this.searchBar.classList.remove("active")
        this.searchModal.classList.remove("active")
        this.active = false
    }
}


const frogIcon = L.icon({
    iconUrl: "/img/frog.png",
    iconSize: [50, 50]
})

const warriorIcon = L.icon({
    iconUrl: "/img/warrior.png",
    iconSize: [50, 50]
})


const host = window.location.hostname;
const port = window.location.port;
const protocol = window.location.protocol === "https:" ? "wss" : "ws";

const httpUrl = window.location.protocol + "//" + host + ":" + port;

const gameServer = new GameServer(httpUrl)

const map = new WorldMap()
const entityList = new EntityList(map);


const searchModal = new SearchModal(map, entityList);

gameServer.getEnemies().then(enemies => enemies.forEach(e => entityList.addEnemy(e)));
gameServer.getPlayers().then(players => players.forEach(p => entityList.addPlayer(p)));

const websocket = new WebSocket(`${protocol}://${host}:${port}/ws/dashboard`);
websocket.onopen = (e) => {
    console.log("OPEN", e);
};


websocket.onmessage = (e) => {
    let obj = JSON.parse(e.data);
    console.log(obj);
    if (obj.type === ".EnemyAppears") {
        entityList.addEnemy(obj.enemy);
        searchModal.refreshResults()
    } else if (obj.type === ".PlayerUpdate") {
        entityList.updatePlayer(obj);
        searchModal.refreshResults()
    }
};
