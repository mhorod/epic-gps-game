import { opine, Router, serveStatic } from "../deps.ts";
import { dirname, join } from "../deps.ts";
import { renderFileToString } from "../deps.ts";

const app = opine();
const router = Router();


router.get("/", async (req, res, next) => {
  res.render("index.ejs", { message: "Hello from dashboard" });
})


const dir = dirname(import.meta.url);
app.set("views", join(dir, "../views"));
app.set("view engine", "ejs");
app.engine("ejs", renderFileToString);
app.engine("html", renderFileToString)
app.use(serveStatic(join(dir, "../public")));

app.use("/", router);


export default app;
