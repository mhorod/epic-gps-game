import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import Main from "./Main";
import MapView from "./map/MapView";
import SpawnAreas from "./SpawnAreas";

import enableMocking from "./mocks/server";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Main></Main>,
  },
  {
    path: "/players",
    element: <Main></Main>,
  },
  {
    path: "/enemies",
    element: <Main></Main>,
  },
  {
    path: "/map-view",
    element: (
      <Main>
        <MapView />
      </Main>
    ),
  },
  {
    path: "/spawn-areas",
    element: (
      <Main>
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
