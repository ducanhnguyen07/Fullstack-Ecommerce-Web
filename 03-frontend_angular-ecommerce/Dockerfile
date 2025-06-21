## build stage ##
FROM node:18.18-alpine as build

WORKDIR /app
COPY . .

RUN npm install --force
RUN npm run build

## run stage ##
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/angular-ecommerce /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]