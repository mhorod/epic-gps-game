import { Icon, LatLngExpression } from "leaflet";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";

import "./MapView.css";
import MapSearch from "./MapSearch";

const position: LatLngExpression = [12, 24];

const frogIcon = new Icon({
  iconUrl: "/img/frog.png",
  iconSize: [50, 50],
});

function MapComponent() {
  return (
    <MapContainer center={position} zoom={13} className="map">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={position} icon={frogIcon}>
        <Popup>
          A pretty CSS3 popup. <br /> Easily customizable.
        </Popup>
      </Marker>
    </MapContainer>
  );
}

function MapView() {
  return (
    <div className="map-wrapper">
      <MapSearch />
      <MapComponent />
    </div>
  );
}

export default MapView;
