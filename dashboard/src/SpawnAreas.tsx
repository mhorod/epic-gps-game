import { LatLngExpression } from "leaflet";
import { MapContainer, TileLayer, Polygon } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { useState } from "react";

import { Area } from "./model/model";

const position: LatLngExpression = [50.03028264463553, 19.907693170114893];

type MapComponentProps = {
  areas: Area[];
};

const colors = ["#90be6d", "#f9c74f", "#f3722c", "#f94144"];

function MapComponent(props: MapComponentProps) {
  return (
    <MapContainer center={position} zoom={13} className="map">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
        subdomains="abcd"
      />
      <>
        {props.areas.map((area) => {
          const d = area.dimensions;
          const latLongs: LatLngExpression[] = [
            [d.lowerLatitude, d.lowerLongitude],
            [d.lowerLatitude, d.upperLongitude],
            [d.upperLatitude, d.upperLongitude],
            [d.upperLatitude, d.lowerLongitude],
          ];

          const color = colors[area.difficulty - 1];
          return <Polygon positions={latLongs} color={color} />;
        })}
      </>
    </MapContainer>
  );
}

function SpawnAreas() {
  const [areas, setAreas] = useState([]);

  fetch(window.location.origin + "/v1/areas")
    .then((res) => res.json())
    .then((data) => setAreas(data));

  return (
    <div className="map-wrapper">
      <MapComponent areas={areas} />
    </div>
  );
}

export default SpawnAreas;
