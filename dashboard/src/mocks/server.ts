import { setupWorker } from "msw/browser";
import handlers from "./handlers";

async function enableMocking() {
  if (process.env.NODE_ENV !== "development") {
    return;
  }

  const worker = setupWorker(...handlers);

  return worker.start({
    onUnhandledRequest: "bypass",
  });
}

export default enableMocking;
