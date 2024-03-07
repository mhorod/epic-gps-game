import app from "./src/app.ts";

const port = parseInt(Deno.env.get('PORT'));
app.set("port", port);
app.listen(port, () => console.log("App is running on port: " + port));
