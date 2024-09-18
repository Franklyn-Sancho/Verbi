# Use a imagem base do OpenJDK
FROM openjdk:17-jdk-alpine

# Diretório de trabalho
WORKDIR /app

# Copie o arquivo JAR para o diretório de trabalho
COPY target/verbi-0.0.1-SNAPSHOT.jar /app/verbi-0.0.1-SNAPSHOT.jar

# Comando para executar o JAR
CMD ["java", "-jar", "target/verbi-0.0.1-SNAPSHOT.jar"]
