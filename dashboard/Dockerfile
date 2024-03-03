FROM denoland/deno:1.40.2 AS backend

ENV TZ Europe/Warsaw
WORKDIR /app
COPY app /app

USER deno

COPY app/deps.ts .
RUN deno cache deps.ts

COPY app /app
RUN deno cache main.ts


EXPOSE 8000
CMD ["run", "--allow-env", "--allow-write", "--allow-net", "--allow-read", "--watch", "main.ts"]
