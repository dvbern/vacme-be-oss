FROM mariadb:10.5

COPY copy/ /

#RUN chmod +x /docker-entrypoint-initdb.d/*.sh \
#	&& find /install/ -name '*.sh' | while read i; do chmod +x "${i}"; done \
#	&& chmod +x /usr/local/bin/*.sh

RUN chmod +x /usr/local/bin/*.sh