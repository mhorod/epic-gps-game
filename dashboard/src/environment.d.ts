declare global {
  namespace NodeJS {
    interface ProcessEnv {
      REACT_APP_SOTURI_BACKEND: "mock" | "localhost" | "production";
      NODE_ENV: "development" | "production";
      PWD: string;
    }
  }
}

export {};
