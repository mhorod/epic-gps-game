import { Component, useEffect, useState } from "react";
import L, { Map, MarkerClusterGroup } from "leaflet";
import * as geojson from "geojson";
import "leaflet.markercluster/dist/leaflet.markercluster";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import { useMap } from "react-leaflet";
import { Position } from "../model/model";
import { Icon } from "leaflet";
import { http_path } from "../backend";
import Supercluster from "supercluster";

export type Marker = {
  position: Position;
  gfxName: string;
  onClick: Function;
};

type PointProperties = { gfxName: string; onClick: Function };

type MarkerClusterProps = {
  markers: Marker[];
};

function getIcon(gfxName: string) {
  return new Icon({
    iconUrl: http_path("/" + gfxName),
    iconSize: [32, 32],
  });
}

const maxClusterZoom = 13;

type Point = geojson.Feature<geojson.Point, any>;

function MarkerCluster(props: MarkerClusterProps) {
  const map = useMap();
  const [markers, setMarkers] = useState(
    L.geoJson(null, {
      pointToLayer: (point: Point, latLng: L.LatLng) => {
        const zoom = map.getZoom();
        return createClusterIcon(point, latLng, zoom);
      },
    }).addTo(map),
  );

  const index = new Supercluster({
    radius: 150,
    maxZoom: maxClusterZoom,
    minPoints: 1,
  });
  const features: GeoJSON.Feature<geojson.Point, PointProperties>[] =
    props.markers.map((m) => {
      return {
        type: "Feature",
        geometry: {
          type: "Point",
          coordinates: [m.position.longitude, m.position.latitude],
        },
        properties: {
          gfxName: m.gfxName,
          onClick: m.onClick,
        },
      };
    });

  index.load(features);
  map.off("moveend");
  map.on("moveend", () => {
    update(map, index, markers);
  });

  update(map, index, markers);
  return null;
}

function update(map: Map, index: Supercluster, markers: L.GeoJSON) {
  const bounds = map.getBounds();
  const bbox: geojson.BBox = [
    bounds.getWest(),
    bounds.getSouth(),
    bounds.getEast(),
    bounds.getNorth(),
  ];
  const zoom = map.getZoom();
  const clusters = index.getClusters(bbox, zoom);
  const collection: geojson.FeatureCollection = {
    type: "FeatureCollection",
    bbox: bbox,
    features: clusters,
  };

  markers.clearLayers();
  markers.addData(collection);
}

function createClusterIcon(
  feature: Point,
  latLng: L.LatLng,
  zoom: number,
): L.Layer {
  if (!feature.properties.cluster) {
    if (zoom > maxClusterZoom) return createEnemyIcon(feature, latLng);
    else return createNumberIcon(1, "1", latLng);
  } else {
    const count = feature.properties.point_count;
    const text = feature.properties.point_count_abbreviated;
    return createNumberIcon(count, text, latLng);
  }
}

function createEnemyIcon(feature: Point, latLng: L.LatLng): L.Layer {
  const marker = L.marker(latLng, {
    icon: getIcon(feature.properties.gfxName),
  });
  marker.on("click", feature.properties.onClick);
  return marker;
}

function createNumberIcon(
  count: number,
  text: string,
  latLng: L.LatLng,
): L.Layer {
  var size = count < 100 ? "small" : count < 1000 ? "medium" : "large";

  var icon = L.divIcon({
    html: "<div><span>" + text + "</span></div>",
    className: "marker-cluster marker-cluster-" + size,
    iconSize: L.point(40, 40),
  });

  const marker = L.marker(latLng, {
    icon: icon,
  });

  return marker;
}

export default MarkerCluster;
