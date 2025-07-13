# 💳 Ebank – Application bancaire déployée sur Azure

## 🚀 Accès en ligne : http://98.71.187.176:8090

## Stack technique
Frontend : Angular

Backend : Spring Web MVC

Sécurité : Spring Security + JWT

Persistence : PostgreSQL + Spring Data JPA

Recherche : Elasticsearch + Spring Data Elasticsearch

Infrastructure : Docker, Docker Compose, Azure VM (Ubuntu 22.04)

## 🧑‍💼 Gestion des rôles

L'application propose un système multi-rôle avec interfaces séparées :

Rôle	Permissions principales

👨‍💼 Admin	Gérer les employés, paramétrer l’application

🧑‍🔧 Employé	Créer des clients, gérer les comptes et les transactions

👤 Client	Accéder à son espace, effectuer des virements, payer par QR


## 🐳 Dockerisation (Application monolithique)

Le backend Spring embarque également le frontend Angular (copié dans resources/static).

📦 Dockerfile minimal (dans le backend)

### Étape 1 : Build backend uniquement (le frontend est déjà présent)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

### Étape 2 : Déploiement dans Tomcat
FROM tomcat:10-jdk17
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
🎯 Le build Angular doit être copié dans src/main/resources/static.

## ⚙️ Configuration dynamique Angular

### 📄 assets/config.json :


{
  "production": true,
  "apiUrl": "http://98.71.187.176:8090/api"
}


### 📄 main.ts :

fetch('/assets/config.json')
  .then(res => res.json())
  .then((config) => {
    if (config.production) {
      import('@angular/core').then(({ enableProdMode }) => enableProdMode());
    }

    bootstrapApplication(AppComponent, {
      providers: [{ provide: APP_CONFIG, useValue: config }]
    });
  });
## 🔒 Spring Security – Accès aux fichiers statiques

.requestMatchers(
    AntPathRequestMatcher.antMatcher("/"),
    AntPathRequestMatcher.antMatcher("/index.html"),
    AntPathRequestMatcher.antMatcher("/*.js"),
    AntPathRequestMatcher.antMatcher("/assets/**")
).permitAll()


## 🌍 CORS Configuration

@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("http://98.71.187.176:8090")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
}
## Préchargement des données
DataInitializer.java

@PostConstruct
public void init() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("data.sql"));
    DatabasePopulatorUtils.execute(populator, dataSource);
}
⚠️ Ne pas insérer manuellement des IDs générés automatiquement (@GeneratedValue) dans data.sql.

## docker-compose.yml

version: '3.8'

services:

  postgres:
  
    image: postgres:17
    
    environment:
    
      POSTGRES_USER: postgres
      
      POSTGRES_PASSWORD: root
      
      POSTGRES_DB: ebanking
      
    volumes:
    
      - pgdata:/var/lib/postgresql/data
      
      - ./initdb:/docker-entrypoint-initdb.d
      
    ports:
    
      - "5432:5432"

  elasticsearch:
  
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    
    environment:
    
      - discovery.type=single-node
      
      - xpack.security.enabled=false
      
    ports:
    
      - "9200:9200"
      
    volumes:
    
      - esdata:/usr/share/elasticsearch/data

  backend:
  
    build:
    
      context: .
      
      dockerfile: Dockerfile
      
    ports:
    
      - "8090:8080"
      
    depends_on:
    
      - postgres
      
      - elasticsearch

volumes:

  pgdata:
  
  esdata:
  
## ☁️ Déploiement sur Azure VM


### 1. 🧑‍🎓 Créer un compte étudiant Azure

👉 https://azure.microsoft.com/fr-fr/free/students/

### 2. 💻 Créer une VM Ubuntu

az vm create --resource-group ebank-rg --name ebank-vm --image Ubuntu2204 --admin-username azureuser --generate-ssh-keys --size Standard_B1ms --location westeurope

### 3. 🔐 Connexion SSH

ssh azureuser@<IP_PUBLIQUE>

### 🐳 Installer Docker & Compose sur la VM

sudo apt update
sudo apt install -y docker.io docker-compose
sudo systemctl enable docker
sudo systemctl start docker
📦 Transférer le projet

### Sur votre machine locale

tar -a -c -f ebank.tar.gz projAtlas
scp ebank.tar.gz azureuser@<IP_PUBLIQUE>:~

### Sur la VM

tar -xzf ebank.tar.gz

cd projAtlas

sudo docker-compose build

sudo docker-compose up -d

### 🖥️ Accès à l'application : http://<IP_PUBLIQUE>:8090

### 🔁 Mettre à jour l'application

sudo docker-compose down -v
sudo docker image prune -a
sudo docker-compose build
sudo docker-compose up -d

### 📚 Ressources

  📘 Documentation Azure CLI : https://learn.microsoft.com/fr-fr/cli/azure/install-azure-cli?view=azure-cli-latest
