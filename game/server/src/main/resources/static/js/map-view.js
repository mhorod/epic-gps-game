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
    }

    zoomOn(position) {
        this.map.setView({lat: position.latitude, lng: position.longitude}, 16)
    }
}

class EntityList {
    constructor(map) {
        this.map = map
        this.enemyList = document.getElementById("enemy-list");

        this.playerList = document.getElementById("player-list");
        this.playerMap = new Map();

    }

    addEnemy(enemy) {
        const enemyElement = document.createElement("li");
        enemyElement.onclick = () => this.map.zoomOn(enemy.position)
        enemyElement.innerText = enemy.name;
        this.enemyList.appendChild(enemyElement);

        let marker = L.marker([enemy.position.latitude, enemy.position.longitude], {icon: frogIcon}).addTo(map.map);
        marker.bindPopup(enemy.name)
    }

    addPlayer(player) {
        const playerElement = document.createElement("li");
        playerElement.onclick = () => this.map.zoomOn(player.position)
        playerElement.innerText = player.player.name;
        this.playerList.appendChild(playerElement);

        let marker = L.marker([player.position.latitude, player.position.longitude], {icon: warriorIcon}).addTo(map.map);
        marker.bindPopup(player.player.name)

        this.playerMap.set(player.player.name, {
            player: player.player,
            position: player.position,
            marker: marker
        })
    }

    updatePlayer(player) {
        const name = player.player.name;
        if  (!this.playerMap.has(player.player.name)) {
            this.addPlayer(player);
        }

        const current = this.playerMap.get(name)
        current.player = player;
        current.position = player.position;
        current.marker.setLatLng([player.position.latitude, player.position.longitude]).update()
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


const frogIcon = L.icon({
    iconUrl: "/img/frog.png",
    iconSize: [50, 50]
})

const warriorIcon = L.icon({
    iconUrl: "/img/warrior.png",
    iconSize: [50, 50]
})



const host = window.location.hostname;
const port = 8080;
const protocol = globalThis.protocol == "https" ? "wss" : "ws";
const httpUrl = "http://" +  host + ":" + port;

const gameServer = new GameServer(httpUrl)

const map = new WorldMap()
const entityList = new EntityList(map);

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
    }
    else if (obj.type === ".PlayerUpdate") {
        entityList.updatePlayer(obj);
    }
};
