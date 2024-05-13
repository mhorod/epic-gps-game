import { LatLngBoundsExpression, LatLngExpression } from "leaflet";
import { MapContainer, TileLayer, Polygon } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { Component, useState } from "react";

import { Area, PolygonWithDifficulty } from "./model/model";
import { http_path } from "./backend";

const position: LatLngExpression = [50.03028264463553, 19.907693170114893];

type MapComponentProps = {
  areas: PolygonWithDifficulty[];
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
          const latLongs: LatLngExpression[] = [];
          for (const point of area.polygon.points) {
            latLongs.push([point.latitude, point.longitude]);
          }

          const color = colors[area.difficulty - 1];
          return <Polygon positions={latLongs} color={color} />;
        })}
      </>
    </MapContainer>
  );
}

type SpawnAreasState = { areas: PolygonWithDifficulty[] };
type SpawnAreasProps = {};

class SpawnAreas extends Component<SpawnAreasProps, SpawnAreasState> {
  constructor(props: SpawnAreasProps) {
    super(props);
    this.state = { areas: [] };

    fetch(http_path("/v1/areas"))
      .then((res) => res.json())
      .then((data) => this.setAreas(data));
  }

  setAreas(areas: PolygonWithDifficulty[]) {
    this.setState({ areas: areas });
  }

  render() {
    return (
      <div className="map-wrapper">
        <MapComponent areas={this.state.areas} />
      </div>
    );
  }
}

export default SpawnAreas;
