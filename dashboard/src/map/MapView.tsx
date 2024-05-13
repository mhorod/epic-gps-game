import { Icon, LatLngExpression, Map as LeafletMap } from "leaflet";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";

import "./MapView.css";
import MapSearch from "./MapSearch";
import { Component } from "react";
import {
  Enemy,
  EnemyTypeId,
  EnemyType,
  PlayerWithPosition,
  Position,
} from "../model/model";
import Entities from "./Entities";
import {
  EnemiesAppear,
  EnemiesDisppear,
  EnemyAppears,
  EnemyDisappears,
  PlayerDisappears,
  PlayerUpdate,
} from "../model/messages";
import EntityInfo from "./EntityInfo";
import { ws_path } from "../backend";
import configManager from "../Config";

const warriorIcon = new Icon({
  iconUrl: "/dashboard/img/warrior.png",
  iconSize: [50, 50],
});

type MapComponentProps = {
  entities: Entities;
  setMap: (m: LeafletMap) => void;
  selectEnemy: (e: Enemy) => void;
  selectPlayer: (p: PlayerWithPosition) => void;
};

function mapPosition(position: Position): LatLngExpression {
  return [position.latitude, position.longitude];
}

const MapAccess = ({ setMap }: { setMap: (m: LeafletMap) => void }) => {
  const map = useMap();
  setMap(map);
  return null;
};

function enemyIcon(gfxName: string) {
  return new Icon({
    iconUrl: "/" + gfxName,
    iconSize: [32, 32],
  });
}

function MapComponent(props: MapComponentProps) {
  return (
    <MapContainer
      center={[50.03028264463553, 19.907693170114893]}
      zoom={13}
      className="map"
    >
      <MapAccess setMap={props.setMap} />
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <div>
        {Array.from(props.entities.enemies.values()).map((e) => {
          const t = configManager.getEnemyTypeById(e.enemyTypeId);
          return (
            <Marker
              key={"enemy-" + e.enemyId.id}
              position={mapPosition(e.position)}
              icon={enemyIcon(t?.gfxName || "undefined")}
              eventHandlers={{
                click: () => props.selectEnemy(e),
              }}
            ></Marker>
          );
        })}
        {Array.from(props.entities.players.values())
          .filter((p) => p.position != null)
          .map((p) => {
            return (
              <Marker
                key={"player-" + p.player.name}
                position={mapPosition(p.position!)}
                icon={warriorIcon}
                eventHandlers={{
                  click: () => props.selectPlayer(p),
                }}
              ></Marker>
            );
          })}
      </div>
    </MapContainer>
  );
}

type MapViewProps = {};

type SelectedEntity = {
  type: string;
  entity: Enemy | PlayerWithPosition;
};

type MapViewState = {
  entities: Entities;
  searchActive: boolean;
  selectedEntity: SelectedEntity | null;
};

class MapView extends Component<MapViewProps, MapViewState> {
  map: LeafletMap | undefined = undefined;
  constructor(props: {}) {
    super(props);

    this.state = {
      entities: new Entities(),
      searchActive: false,
      selectedEntity: null,
    };
  }

  componentDidMount(): void {
    const websocket = new WebSocket(ws_path("/dashboard"));

    websocket.onmessage = (e) => {
      let obj = JSON.parse(e.data);
      console.log("RECEIVED: ", obj);

      if (obj.type === ".PlayerUpdate") {
        this.playerUpdate(obj);
      } else if (obj.type === ".PlayerDisappears") {
        this.playerDisappears(obj);
      } else if (obj.type === ".EnemyAppears") {
        this.enemyAppears(obj);
      } else if (obj.type === ".EnemiesAppear") {
        this.enemiesAppear(obj);
      } else if (obj.type === ".EnemyDisappears") {
        this.enemyDisappears(obj);
      } else if (obj.type === ".EnemiesDisappear") {
        this.enemiesDisappear(obj);
      } else {
        console.log("Unknown event: ", obj);
      }
    };
  }

  playerUpdate(e: PlayerUpdate) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      newEntities.addPlayer({ player: e.player, position: e.position });
      return { entities: newEntities };
    });
  }

  playerDisappears(e: PlayerDisappears) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      newEntities.removePlayer(e.playerName);
      return { entities: newEntities };
    });
  }

  enemyAppears(e: EnemyAppears) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      newEntities.addEnemy(e.enemy);
      return { entities: newEntities };
    });
  }

  enemiesAppear(e: EnemiesAppear) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      for (const enemy of e.enemies) newEntities.addEnemy(enemy);
      return { entities: newEntities };
    });
  }

  enemiesDisappear(e: EnemiesDisppear) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      for (const enemyId of e.enemies) newEntities.removeEnemy(enemyId);
      return { entities: newEntities };
    });
  }

  enemyDisappears(e: EnemyDisappears) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      newEntities.removeEnemy(e.enemyId);
      return { entities: newEntities };
    });
  }

  render() {
    return (
      <div className="map-wrapper">
        <MapSearch
          entities={this.state.entities}
          zoomOn={(p) => this.zoomOn(p)}
          active={this.state.searchActive}
          openSearch={() => this.openSearch()}
          closeSearch={() => this.closeSearch()}
        />
        <MapComponent
          entities={this.state.entities}
          setMap={(m) => this.setMap(m)}
          selectEnemy={(e) => this.selectEnemy(e)}
          selectPlayer={(p) => this.selectPlayer(p)}
        />
        {this.state.selectedEntity && (
          <EntityInfo
            type={this.state.selectedEntity.type}
            entity={this.state.selectedEntity.entity}
            onClose={() => this.unselectEntity()}
          />
        )}
      </div>
    );
  }

  setMap(m: LeafletMap) {
    this.map = m;
  }

  openSearch() {
    this.setState({
      ...this.state,
      searchActive: true,
    });
  }

  closeSearch() {
    this.setState({
      ...this.state,
      searchActive: false,
    });
  }

  zoomOn(position: Position) {
    if (this.map !== undefined) {
      this.map.setView([position.latitude, position.longitude]);
      this.map.setZoom(20);
    }

    this.setState({
      ...this.state,
      searchActive: false,
    });
  }

  selectEnemy(e: Enemy) {
    console.log(e);
    this.setState({
      ...this.state,
      selectedEntity: { type: "Enemy", entity: e },
    });
  }

  selectPlayer(p: PlayerWithPosition) {
    console.log(p);
    this.setState({
      ...this.state,
      selectedEntity: { type: "Player", entity: p },
    });
  }

  unselectEntity() {
    this.setState({
      ...this.state,
      selectedEntity: null,
    });
  }
}

export default MapView;
