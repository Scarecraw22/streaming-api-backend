FROM openjdk:11

RUN mkdir /usr/share/ffmpeg
COPY conf/ffmpeg-master-latest-linux64-gpl.tar.xz /usr/share/ffmpeg/
RUN tar -xvf /usr/share/ffmpeg/ffmpeg-master-latest-linux64-gpl.tar.xz
RUN chmod 755 /usr/share/ffmpeg -R

RUN mkdir /usr/share/streaming-api-backend
#COPY conf/application.yml /usr/share/streaming-api-backend/
RUN chmod 755 /usr/share/streaming-api-backend/ -R

RUN ls -l /usr/share/streaming-api-backend/

ADD build/libs/*.jar streaming-api-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.config.location=/usr/share/streaming-api-backend/application.yml", "-jar", "streaming-api-backend.jar"]