FROM ubuntu:18.04

COPY jdk1.5.0_22 /opt/jdk1.5.0_22

RUN apt-get update && apt-get install -y make && apt-get clean

ENV JAVA_HOME=/opt/jdk1.5.0_22 \
    PATH=/opt/jdk1.5.0_22/bin:${PATH}
