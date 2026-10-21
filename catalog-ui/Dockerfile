# Build context is the repo root: the build generates types from catalog-service's OpenAPI contract.
FROM node:24-alpine AS build
WORKDIR /repo/catalog-ui
COPY catalog-ui/package.json catalog-ui/package-lock.json ./
RUN npm ci
COPY catalog-service/src/main/resources/openapi/catalog-api.yaml /repo/catalog-service/src/main/resources/openapi/
COPY catalog-ui/ ./
RUN npm run build

FROM nginx:1.30-alpine
COPY catalog-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/catalog-ui/dist /usr/share/nginx/html
