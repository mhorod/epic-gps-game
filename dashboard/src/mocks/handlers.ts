import { http, HttpResponse } from "msw";
import { Enemy } from "../model/model";
import spawnAreasHandlers from "./spawn-areas";

const enemiesHandlers = [
  http.get("https://soturi.online/v1/enemies", () => {
    const enemy: Enemy = {
      name: "Pomidor",
      lvl: 2,
      position: {
        latitude: 50.03028264463553,
        longitude: 19.907693170114893,
      },
      enemyId: {
        id: "1",
      },
      gfxName: "frog.png",
    };
    return HttpResponse.json([enemy]);
  }),
];

const handlers = [...spawnAreasHandlers, ...enemiesHandlers];

export default handlers;
