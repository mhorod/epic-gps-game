function get_url(): [string, string] {
  const backend = process.env.REACT_APP_SOTURI_BACKEND;
  if (backend === "localhost") {
    return [
      `http://${window.location.hostname}:8080`,
      `ws://${window.location.hostname}:8080`,
    ];
  } else if (backend === "production") {
    return [`https://soturi.online`, `wss://soturi.online`];
  } else {
    const protocol = window.location.protocol || "http:";
    const host = window.location.host;

    const ws_protocol = protocol === "http:" ? "ws" : "wss";
    return [`${protocol}//${host}`, `${ws_protocol}://${host}`];
  }
}

const [HTTP_URL, WS_URL] = get_url();

export function http_path(path: string) {
  return HTTP_URL + path;
}

export function ws_path(path: string) {
  return WS_URL + "/ws" + path;
}

export async function get_json(path: string) {
  return fetch(http_path(path)).then((res) => res.json());
}
