FROM nginx:stable-alpine

ARG CANTON
ARG COPY_APP_PATH

COPY /docker/${CANTON}/copy /
COPY /dist/${CANTON}/${COPY_APP_PATH} /usr/share/nginx/html/

USER root


RUN apk update \
    && apk add openssl ca-certificates tzdata

RUN sh /install/zip-static-content.sh
RUN chmod 1777 /tmp
RUN chown -R 101:101 /tmp/html
RUN chmod -R 700 /tmp/html

# add dir for variable substitution
RUN mkdir /etc/nginx/conf.d/substituted \
    && chown -R 101:101 /etc/nginx/conf.d/substituted \
    && chmod -R 700 /etc/nginx/conf.d/substituted

ENV NGINX_ENVSUBST_OUTPUT_DIR /etc/nginx/conf.d/substituted
ENV ADDITIONAL_FRAME_ANCESTORS ''

# Reset back to default nginx user defined in upstream Dockerfile
USER 101

HEALTHCHECK --interval=5m --timeout=3s CMD wget --quiet --tries=2  --spider -U DockerHealthCheck http://$(hostname -i || echo '127.0.0.1')/index.html || exit 1

EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
