# Build context is the repo root: the build generates types from fake-price-service's OpenAPI contract.
FROM node:24-alpine AS build
WORKDIR /repo/fake-price-ui
COPY fake-price-ui/package.json fake-price-ui/package-lock.json ./
RUN npm ci
COPY fake-price-service/src/main/resources/openapi/price-api.yaml /repo/fake-price-service/src/main/resources/openapi/
COPY fake-price-ui/ ./
RUN npm run build

FROM nginx:1.30-alpine
COPY fake-price-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/fake-price-ui/dist /usr/share/nginx/html
