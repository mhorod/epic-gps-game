(() => {
  // ../app/src/frontend/main.ts
  var host = window.location.hostname;
  var port = 8080;
  var protocol = globalThis.protocol == "https" ? "wss" : "ws";
  var map = L.map("map").setView([50.03, 19.9], 10);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);
  var websocket = new WebSocket(`${protocol}://${host}:${port}/game`);
  websocket.onopen = (e) => {
    console.log("OPEN", e);
    websocket.send(
      JSON.stringify({ "type": ".LoginInfo", "name": "admin", "password": "mud" })
    );
  };
  websocket.onmessage = (e) => {
    let obj = JSON.parse(e.data);
    console.log(obj);
    if (obj.type == ".EnemyAppears") {
      let enemy = obj.enemy;
      let marker = L.marker([enemy.position.latitude, enemy.position.longitude]).addTo(map);
      marker.bindPopup(enemy.name).openPopup();
    }
  };
})();
