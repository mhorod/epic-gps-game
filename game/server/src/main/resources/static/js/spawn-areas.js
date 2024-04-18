class Position {
    constructor(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

class WorldMap {
    constructor() {
        this.map = L.map("map").setView([50.03, 19.9], 10);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
            subdomains: 'abcd',
            maxZoom: 20
        }).addTo(this.map)
    }

    zoomOn(position) {
        this.map.setView({lat: position.latitude, lng: position.longitude}, 16)
    }
}

const host = window.location.hostname;
const port = window.location.port;
const protocol = window.location.protocol === "https:" ? "wss" : "ws";

const httpUrl = window.location.protocol + "//" + host + ":" + port;

const map = new WorldMap()

const colors = ["#90be6d", "#f9c74f", "#f3722c", "#f94144"]

fetch(`${httpUrl}/v1/areas`)
    .then(data => data.json())
    .then(data => {
        for (let area of data) {
            const d = area.dimensions;
            const latLongs = [
                [d.lowerLatitude, d.lowerLongitude],
                [d.lowerLatitude, d.upperLongitude],
                [d.upperLatitude, d.upperLongitude],
                [d.upperLatitude, d.lowerLongitude]
            ]

            const color = colors[area.difficulty - 1];

            L.polygon(latLongs, {color: color}).addTo(map.map)
        }
    })

