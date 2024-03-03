// App and router
export {
  opine,
  Router,
  serveStatic,
  json,
  urlencoded
} from "https://deno.land/x/opine@2.3.4/mod.ts";
export type { IRouter, OpineRequest, OpineResponse, NextFunction } from "https://deno.land/x/opine@2.3.4/mod.ts";
export { dirname, join } from "https://deno.land/std/path/mod.ts";
export { renderFileToString } from "https://deno.land/x/dejs@0.10.3/mod.ts";
