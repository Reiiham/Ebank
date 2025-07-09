# Étape 1 : Build Angular
FROM node:20 AS frontend-builder
WORKDIR /frontend

# Copie uniquement les fichiers nécessaires pour l'installation
COPY FrontEbanking-main/package*.json ./
RUN npm ci

# Copie le reste du code
COPY FrontEbanking-main/ ./

# Build avec configuration production
RUN npm run build -- --configuration=production --base-href=/



# Étape 2 : Build Spring WAR avec Angular inclus
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY eBankingVer1/ .
COPY --from=frontend-builder /frontend/dist/e-banking-app/ /app/src/main/webapp/
RUN mvn clean package -DskipTests

# Étape 3 : Déployer dans Tomcat
FROM tomcat:10-jdk17
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
