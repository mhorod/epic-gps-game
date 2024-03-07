import { opine, Router, serveStatic } from "../deps.ts";
import { dirname, join } from "../deps.ts";
import { renderFileToString } from "../deps.ts";

const app = opine();
const router = Router();

const apiProtocol = `http`;
const websocketProtocol = 'ws';
const gameServerUrl = '52.158.44.176:8080'
const websocket = new WebSocket(`${websocketProtocol}://${gameServerUrl}/game`);

websocket.onopen = (e) => {
  console.log("OPEN", e);
  websocket.send(
    JSON.stringify({ "type": ".LoginInfo", "name": "admin", "password": "mud" })
  );
}
websocket.onmessage = (e) => console.log("MESSAGE", e);


router.get("/", async (req, res, next) => {
  let response = await fetch(`${apiProtocol}://${gameServerUrl}/v1/hello-world`);
  let message = await response.text();
  res.render("index.ejs", { message: message })
})


const dir = dirname(import.meta.url);
app.set("views", join(dir, "../views"));
app.set("view engine", "ejs");
app.engine("ejs", renderFileToString);
app.engine("html", renderFileToString)
app.use(serveStatic(join(dir, "../public")));

app.use("/", router);


export default app;
