import L, { Icon, LatLngExpression, Map as LeafletMap } from "leaflet";
import { MapContainer, TileLayer, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";

import "./MapView.css";
import MapSearch from "./MapSearch";
import {
  Component,
  ComponentType,
  MemoExoticComponent,
  memo,
  useMemo,
} from "react";
import { Enemy, EnemyType, PlayerWithPosition, Position } from "../model/model";
import Entities from "./Entities";
import {
  EnemiesAppear,
  EnemiesDisppear,
  PlayerDisappears,
  PlayerUpdate,
} from "../model/messages";
import EntityInfo from "./EntityInfo";
import { http_path, ws_path } from "../backend";
import configManager from "../Config";
import MarkerCluster, { Marker } from "./MarkerCluster";
import SearchSettings from "./SearchSettings";
import SearchResult from "./SearchResult";

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
    iconUrl: http_path("/" + gfxName),
    iconSize: [32, 32],
  });
}

function MapComponent(props: MapComponentProps) {
  const markers: Marker[] = [];
  props.entities.enemies.forEach((enemy, _) => {
    const t = configManager.getEnemyTypeById(enemy.typeId);
    markers.push({
      position: enemy.position,
      gfxName: "/" + t?.gfxName || "undefined",
      onClick: () => props.selectEnemy(enemy),
    });
  });

  props.entities.players.forEach((player) => {
    if (player.position !== null) {
      markers.push({
        position: player.position,
        gfxName: "/dashboard/img/warrior.png",
        onClick: () => props.selectPlayer(player),
      });
    }
  });

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
      <MarkerCluster markers={markers} />
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

type MapComponentMemoProps = {
  entities: Entities;
};

type MapSearchMemoProps = {
  active: boolean;
};

class MapView extends Component<MapViewProps, MapViewState> {
  map: LeafletMap | undefined = undefined;
  MapComponent: MemoExoticComponent<ComponentType<MapComponentMemoProps>>;
  MapSearch: MemoExoticComponent<ComponentType<MapSearchMemoProps>>;
  constructor(props: {}) {
    super(props);

    this.state = {
      entities: new Entities(),
      searchActive: false,
      selectedEntity: null,
    };

    this.MapComponent = memo(({ entities }: { entities: Entities }) => {
      return (
        <MapComponent
          entities={entities}
          setMap={(m) => this.setMap(m)}
          selectEnemy={(e) => this.selectEnemy(e)}
          selectPlayer={(p) => this.selectPlayer(p)}
        />
      );
    });

    this.MapSearch = memo(({ active }: { active: boolean }) => {
      return (
        <MapSearch
          search={(settings) => this.search(settings)}
          zoomOn={(p) => this.zoomOn(p)}
          active={active}
          openSearch={() => this.openSearch()}
          closeSearch={() => this.closeSearch()}
        />
      );
    });
  }

  componentDidMount(): void {
    const websocket = new WebSocket(ws_path("/dashboard"));

    websocket.onmessage = (e) => {
      console.log("RECEIVED: ", e);
      let obj = JSON.parse(e.data);

      if (obj.type === ".PlayerUpdate") {
        this.playerUpdate(obj);
      } else if (obj.type === ".PlayerDisappears") {
        this.playerDisappears(obj);
      } else if (obj.type === ".EnemiesAppear") {
        this.enemiesAppear(obj);
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

  enemiesAppear(e: EnemiesAppear) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      for (const enemy of e.enemies) {
        newEntities.addEnemy(enemy);
      }
      return { entities: newEntities };
    });
  }

  enemiesDisappear(e: EnemiesDisppear) {
    this.setState((state) => {
      let newEntities = new Entities(state.entities);
      for (const enemyId of e.enemyIds) newEntities.removeEnemy(enemyId);
      return { entities: newEntities };
    });
  }

  search(settings: SearchSettings) {
    let re: RegExp;
    try {
      re = new RegExp(settings.searchValue);
    } catch {
      return [];
    }

    const enemyResults = Array.from(this.state.entities.enemies.values())
      .filter((e) => {
        const t = configManager.getEnemyTypeById(e.typeId);
        return re.test(t?.name || "undefined");
      })
      .map((e) => SearchResult.ofEnemy(e));

    const playerResults = Array.from(this.state.entities.players.values())
      .filter((p) => re.test(p.player.name))
      .map((p) => SearchResult.ofPlayer(p));

    return [...enemyResults, ...playerResults];
  }

  render() {
    return (
      <div className="map-wrapper">
        <this.MapSearch active={this.state.searchActive} />
        <this.MapComponent entities={this.state.entities} />
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
    if (this.state.searchActive) return;
    this.setState({
      ...this.state,
      searchActive: true,
    });
  }

  closeSearch() {
    if (!this.state.searchActive) return;
    this.setState({
      ...this.state,
      searchActive: false,
    });
  }

  zoomOn(position: Position) {
    if (this.map !== undefined) {
      this.map.setView([position.latitude, position.longitude], 20);
    }

    this.closeSearch();
  }

  selectEnemy(e: Enemy) {
    this.setState({
      ...this.state,
      selectedEntity: { type: "Enemy", entity: e },
    });
  }

  selectPlayer(p: PlayerWithPosition) {
    this.setState({
      ...this.state,
      selectedEntity: { type: "Player", entity: p },
    });
  }

  unselectEntity() {
    if (this.state.selectedEntity === null) return;
    this.setState({
      ...this.state,
      selectedEntity: null,
    });
  }
}

export default MapView;
