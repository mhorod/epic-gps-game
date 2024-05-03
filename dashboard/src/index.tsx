import * as React from "react";
import * as ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import App from "./App";
import MapView from "./MapView";
import Main from "./Main";

const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <Main>
        <App />
      </Main>
    ),
  },
  {
    path: "/map-view",
    element: (
      <Main>
        <MapView />
      </Main>
    ),
  },
]);

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
