import { Component, useEffect, useState } from "react";
import L, { Map, MarkerClusterGroup } from "leaflet";
import "leaflet.markercluster/dist/leaflet.markercluster";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import { Marker } from "leaflet";
import { useMap } from "react-leaflet";

type MarkerClusterProps = {
  markers: Marker[];
};

function MarkerCluster(props: MarkerClusterProps) {
  const [mcg, setMcg] = useState<MarkerClusterGroup>(L.markerClusterGroup());

  const map = useMap();

  useEffect(() => {
    mcg.clearLayers();
    props.markers.forEach((m) => m.addTo(mcg));
    map.addLayer(mcg);
  });

  return null;
}

export default MarkerCluster;
