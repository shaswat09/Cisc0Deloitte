FROM containers.cisco.com/annuity/java:8u201
# Maintainer
MAINTAINER BSL Dev Team

# Expose PORT
EXPOSE 8080

VOLUME /tmp

COPY pre_start_config pre_start_config

ADD **/brim-sol-net-change-service-0.0.1-SNAPSHOT.jar brim-sol-net-change-service-0.0.1-SNAPSHOT.jar

RUN mkdir -p /apps/logs

RUN sh -c 'touch /brim-sol-net-change-service-0.0.1-SNAPSHOT.jar'

RUN chmod +x pre_start_config

RUN chmod -R 777 /apps

CMD ["./pre_start_config"]
