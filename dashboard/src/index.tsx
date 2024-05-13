import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import Main from "./Main";
import MapView from "./map/MapView";
import SpawnAreas from "./SpawnAreas";

import enableMocking from "./mocks/server";
import Players from "./Players";

const router = createBrowserRouter([
  {
    path: "/dashboard",
    element: <Main title="Dashboard"></Main>,
  },
  {
    path: "/dashboard/players",
    element: (
      <Main title="Players">
        <Players />
      </Main>
    ),
  },
  {
    path: "/dashboard/enemies",
    element: <Main title="Enemies"></Main>,
  },
  {
    path: "/dashboard/map-view",
    element: (
      <Main title="Map View">
        <MapView />
      </Main>
    ),
  },
  {
    path: "/dashboard/spawn-areas",
    element: (
      <Main title="Spawn Areas">
        <SpawnAreas />
      </Main>
    ),
  },
]);

function startApp() {
  ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
      <RouterProvider router={router} />
    </React.StrictMode>,
  );
}

enableMocking().then(() => startApp());
