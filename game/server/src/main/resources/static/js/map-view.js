class Position {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}


class Entity {
    constructor(name, position) {
        this.name = name;
        this.position = position;
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

const enemyList = document.getElementById("enemy-list");
const playerList = document.getElementById("player-list");


function addEnemy(enemy, map) {
    let marker = L.marker([enemy.position.latitude, enemy.position.longitude], {icon: frogIcon}).addTo(map);
    marker.bindPopup(enemy.name)
    const enemyElement = document.createElement("li");
    enemyElement.onclick = () => zoomOn(map, enemy.position)
    enemyElement.innerText = enemy.name;
    enemyList.appendChild(enemyElement);
}

function addPlayer(player, map) {
    console.log(player)
    let marker = L.marker([player.position.latitude, player.position.longitude], {icon: warriorIcon}).addTo(map);
    marker.bindPopup(player.player.name)
    const playerElement = document.createElement("li");
    playerElement.onclick = () => zoomOn(map, player.position)
    playerElement.innerText = player.player.name;
    playerList.appendChild(playerElement);
}

function zoomOn(map, position) {
  map.setView({lat: position.latitude, lng: position.longitude}, 16)
}

const host = window.location.hostname;
const port = 8080;
const protocol = globalThis.protocol == "https" ? "wss" : "ws";
const httpUrl = "http://" +  host + ":" + port;

const map = L.map("map").setView([50.03, 19.9], 10);
L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

fetch(httpUrl + "/v1/enemies")
    .then(res => res.json())
    .then(data => {
        for (const enemy of data) {
            addEnemy(enemy, map)
        }
    })
    .catch(err => console.log(err));

fetch(httpUrl + "/v1/players")
    .then(res => res.json())
    .then(data => {
        for (const player of data) {
            addPlayer(player, map)
        }
    })
    .catch(err => console.log(err));

const websocket = new WebSocket(`${protocol}://${host}:${port}/ws/dashboard`);
websocket.onopen = (e) => {
    console.log("OPEN", e);
};


websocket.onmessage = (e) => {
    let obj = JSON.parse(e.data);
    console.log(obj);
    if (obj.type === ".EnemyAppears") {
        let enemy = obj.enemy;
        addEnemy(enemy, map);
    }
    else if (obj.type === ".PlayerUpdate") {
        let player = obj.player;
        addPlayer(player, map);
    }
};
