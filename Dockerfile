####
# Build multiestágio, autocontido — ao contrário de src/main/docker/Dockerfile.jvm
# (que espera um `./mvnw package` já rodado no host), este builda dentro do
# próprio Docker. É o formato que plataformas como Render esperam: elas rodam
# só `docker build .`, sem etapa de build separada.
#
# Build local:
#   docker build -t agro-back .
#   docker run -p 8090:8090 \
#     -e DB_HOST=<host> -e DB_PORT=5432 -e DB_NAME=<nome> \
#     -e DB_USERNAME=<usuario> -e DB_PASSWORD=<senha> \
#     agro-back
####

FROM registry.access.redhat.com/ubi9/openjdk-25:1.24 AS build

WORKDIR /home/default/build

# Maven do próprio sistema, não o wrapper: esta imagem não tem `unzip`, e o
# `mvnw` cai para baixar o `.tar.gz` do Maven quando não acha `unzip` — só que
# o checksum fixado em .mvn/wrapper/maven-wrapper.properties é do `.zip`, e a
# validação sempre falha nesse caminho. O `mvn` já instalado na imagem
# (mesma major.minor) evita esse download e essa validação por completo.
#
# pom.xml antes do src/: camada de dependências só invalida quando o pom
# muda, não a cada alteração de código.
COPY --chown=185 pom.xml ./
RUN mvn -B dependency:go-offline

COPY --chown=185 src src
RUN mvn -B clean package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24

ENV LANGUAGE='en_US:en'

COPY --chown=185 --from=build /home/default/build/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /home/default/build/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /home/default/build/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /home/default/build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8090
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
