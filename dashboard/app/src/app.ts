import { opine, Router, serveStatic } from "../deps.ts";
import { dirname, join } from "../deps.ts";
import { renderFileToString } from "../deps.ts";

const app = opine();
const router = Router();

router.get("/", async (req, res, next) => {
  let response = await fetch("http://host.docker.internal:8080/v1/hello-world");
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
