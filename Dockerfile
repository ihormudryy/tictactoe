FROM openjdk:20-slim

ARG JAR_FILE

ADD ${JAR_FILE} app.jar

ENV USERNAME userEntry
ENV USERID 1002
ENV GROUPNAME users
ENV GROUPID 1002
RUN groupadd --force -g ${USERID} ${USERNAME} && useradd -m -s /usr/sbin/nologin --gid ${GROUPID} --uid ${USERID} ${USERNAME}

USER ${USERNAME}

EXPOSE 80
EXPOSE 4040

ENTRYPOINT ["java","-jar","/app.jar"]