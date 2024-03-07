const host = window.location.hostname
const port = 8080
const protocol = globalThis.protocol == 'https' ? 'wss' : 'ws';

let map = L.map('map').setView([50.03, 19.9], 10);

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

const websocket = new WebSocket(`${protocol}://${host}:${port}/game`);
websocket.onopen = (e) => {
    console.log("OPEN", e);
    websocket.send(
        JSON.stringify({ "type": ".LoginInfo", "name": "admin", "password": "mud" })
    );
}
websocket.onmessage = (e) => {
    let obj = JSON.parse(e.data);
    console.log(obj);
    if (obj.type == ".EnemyAppears") {
        let enemy = obj.enemy;
        let marker = L.marker([enemy.position.latitude, enemy.position.longitude]).addTo(map);
        marker.bindPopup(enemy.name).openPopup();
    }
}


