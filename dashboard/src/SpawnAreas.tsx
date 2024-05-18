import { LatLngExpression } from "leaflet";
import { MapContainer, TileLayer, Polygon } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { Component } from "react";

import { PolygonWithDifficulty } from "./model/model";
import { http_path } from "./backend";

import convert from "color-convert";

const position: LatLngExpression = [50.03028264463553, 19.907693170114893];

type MapComponentProps = {
  areas: PolygonWithDifficulty[];
};

const colors = ["#90be6d", "#f9c74f", "#f3722c", "#f94943"];

const hslColors = colors.map((color) => convert.hex.hsl(color));

class ColorMap {
  private thresholds: number[] = [];
  constructor(
    readonly minDifficulty: number,
    readonly maxDifficulty: number,
  ) {
    const step = (maxDifficulty - minDifficulty) / colors.length;
    for (let i = 0; i <= colors.length; i++) {
      const threshold = minDifficulty + step * i;
      this.thresholds.push(threshold);
    }
  }

  color(difficulty: number): string {
    let i = 0;
    while (i + 2 < colors.length && difficulty > this.thresholds[i + 1]) i++;

    const lowerThreshold = this.thresholds[i];
    const upperThreshold = this.thresholds[i + 1];

    const t = (difficulty - lowerThreshold) / (upperThreshold - lowerThreshold);
    const lowerColor = hslColors[i];
    const upperColor = hslColors[i + 1];

    const color: [number, number, number] = [
      lowerColor[0] * (1 - t) + upperColor[0] * t,
      lowerColor[1] * (1 - t) + upperColor[1] * t,
      lowerColor[2] * (1 - t) + upperColor[2] * t,
    ];

    return "#" + convert.hsl.hex(color);
  }
}

function MapComponent(props: MapComponentProps) {
  const difficulties = props.areas.map((area) => area.difficulty);
  const minDiff = Math.min(...difficulties);
  const maxDiff = Math.max(...difficulties);

  const colorMap = new ColorMap(minDiff, maxDiff);

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

          const color = colorMap.color(area.difficulty);
          return (
            <Polygon
              key={latLongs.toString()}
              positions={latLongs}
              color={color}
            />
          );
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
