# 10. Dockerfile, docker-compose and GitHub Actions

**Status:** Accepted

## Context
The service must be ready to deploy, and a reviewer must be able to run everything with as little setup as possible.

## Decision
- **A Dockerfile per app:** built in two steps (first built with the JDK, then run on a smaller JRE), and it doesn't run as root.
- **Settings from outside:** the database address and password and the price system's URL come from environment variables. So the same image runs everywhere.
- **Health checks:** Actuator's liveness and readiness endpoints. docker-compose uses them to show when a copy is ready; Kubernetes would use them too.
- **docker-compose:** one command starts PostgreSQL, two copies of the catalog, the stand-in systems, and Prometheus with Grafana.
- **GitHub Actions:** on every pull request and push to `main`, it builds and tests the Java apps and builds the frontends.

## Why
- You only need Docker to run everything: no Java, Maven or Node.js on your machine.
- Nothing environment-specific is inside the image: only the settings change per environment.
- Every change is tested automatically, and the CI setup is in the repo itself.

## Not chosen
- **Buildpacks:** makes an image without a Dockerfile, but it's less clear what ends up inside.
- **Kubernetes files in the repo:** you need a cluster to use them, and a reviewer may not have one. The health checks and the settings from environment variables are already ready for it.
- **Jenkins:** a server you have to set up and maintain. GitHub Actions comes with the repo.

## Consequences
- docker-compose is for running locally: the database password has a demo default, and the data is gone after `docker compose down`. In production the password comes from a secret store and the database is managed by the cloud provider (see the [production diagram](../diagrams/production-diagram.html)).
