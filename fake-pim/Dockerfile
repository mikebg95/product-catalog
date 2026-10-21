# Build context is the repo root: the build generates types from catalog-service's OpenAPI contract.
FROM node:24-alpine AS build
WORKDIR /repo/fake-pim
COPY fake-pim/package.json fake-pim/package-lock.json ./
RUN npm ci
COPY catalog-service/src/main/resources/openapi/catalog-api.yaml /repo/catalog-service/src/main/resources/openapi/
COPY fake-pim/ ./
RUN npm run build

FROM nginx:1.30-alpine
COPY fake-pim/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/fake-pim/dist /usr/share/nginx/html
