# 🚀 AgileGestion - Système de Gestion de Projets Agile

<div align="center">

![Java](https://img.shields.io/badge/Java-23-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-green?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Swagger](https://img.shields.io/badge/Swagger-2.5-success?style=for-the-badge&logo=swagger)
![GitHub stars](https://img.shields.io/github/stars/nouhaila0204/agilegestion?style=for-the-badge)

**Une solution de gestion de projets Agile avec Scrum, conçue pour les équipes de développement modernes**

[📖 Documentation](#-documentation) • [✨ Fonctionnalités](#-fonctionnalités) • [🚀 Démarrage rapide](#-démarrage-rapide) 
</div>

## 📋 Table des matières
- [✨ Fonctionnalités principales](#-fonctionnalités-principales)
- [🏗️ Architecture](#️-architecture)
- [🚀 Démarrage rapide](#-démarrage-rapide)
- [🛠️ Technologies utilisées](#️-technologies-utilisées)
- [📁 Structure du projet](#-structure-du-projet)

## ✨ Fonctionnalités principales

### 🎯 Gestion des Rôles
| Rôle | Responsabilités | Points forts |
|------|----------------|-------------|
| **📋 Product Owner** | Priorisation du backlog, définition des Epics | Dashboard complet, recommandations intelligentes |
| **⚡ Scrum Master** | Gestion des sprints, suivi de l'avancement | Rapports automatiques, rétrospectives détaillées |
| **💻 Développeur** | Cycle de vie des stories, développement | Vue personnalisée, outils de développement |

### 🔥 Fonctionnalités clés

#### 📊 **Backlog Management**
- ✅ Priorisation intelligente (Moscow, Fibonacci, etc.)
- ✅ Organisation par Epics et User Stories
- ✅ Dashboard en temps réel
- ✅ Recommandations automatiques

#### 🏃 **Sprint Management**
- ✅ Création et planification des sprints
- ✅ Sélection des User Stories
- ✅ Suivi de la vélocité
- ✅ Burndown charts automatiques

#### 📈 **Tracking & Reporting**
- ✅ Daily Standup reports
- ✅ Retrospective automatisée
- ✅ Métriques de performance
- ✅ Alertes proactives

#### 👥 **Team Collaboration**
- ✅ Assignation des tâches
- ✅ Suivi du temps
- ✅ Gestion des blocages
- ✅ Communication intégrée

## 🏗️ Architecture

```
AgileGestion/
├── 📱 Presentation Layer (Controllers)
│   ├── 👑 Role-based Controllers
│   └── 🔧 Core Controllers
├── ⚙️ Business Layer (Services)
│   ├── 🎯 Role Services
│   ├── 🛠️ Core Services
│   └── 📊 Statistics Services
├── 💾 Data Layer
│   ├── 🗃️ Entities (JPA)
│   ├── 📁 Repositories
│   └── 🧾 DTOs
└── 🌐 Infrastructure
    ├── 🔐 Security (à venir)
    ├── 📄 Documentation (Swagger)
    └── ⚡ Configuration
```

## 🚀 Démarrage rapide

### Pré-requis
- Java 17+
- MySQL 8.0+
- Maven 3.8+
- IDE recommandé : IntelliJ IDEA

### Installation

1. **Clonez le projet**
```bash
git clone https://github.com/username/AgileGestion.git
cd AgileGestion
```

2. **Configurez la base de données**
```sql
CREATE DATABASE agileProject_db;
CREATE USER 'agile_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON agileProject_db.* TO 'agile_user'@'localhost';
FLUSH PRIVILEGES;
```

3. **Configurez l'application**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/agileProject_db
spring.datasource.username=agile_user
spring.datasource.password=secure_password
spring.jpa.hibernate.ddl-auto=update
```

4. **Lancez l'application**
```bash
mvn spring-boot:run
```

5. **Accédez à l'application**
- 🌐 **Application** : `http://localhost:8080`
- 📚 **API Docs** : `http://localhost:8080/swagger-ui.html`
- 📊 **Health Check** : `http://localhost:8080/actuator/health`

## 🛠️ Technologies utilisées

### Backend
- **Java 23** - Langage principal
- **Spring Boot 4.0** - Framework principal
- **Spring Data JPA** - Persistance des données
- **Spring MVC** - Architecture web
- **Hibernate** - ORM
- **Lombok** - Réduction du code boilerplate

### Base de données
- **MySQL 8.0** - Base de données relationnelle
- **HikariCP** - Pool de connexions
- **Flyway** - Migration (à venir)

### API & Documentation
- **OpenAPI 3.0** - Spécification API
- **Swagger UI** - Documentation interactive
- **Postman** - Tests API

### Outils de développement
- **Maven** - Gestion des dépendances
- **Spring Boot DevTools** - Hot reload
- **Git** - Contrôle de version
- **IntelliJ IDEA** - IDE recommandé

## 📁 Structure du projet

```
src/main/java/com/project/AgileGestion/
├── 📁 controller/
│   ├── 📁 core/          # Contrôleurs génériques
│   └── 📁 role/          # Contrôleurs par rôle
├── 📁 service/
│   ├── 📁 core/          # Services génériques
│   └── 📁 role/          # Services par rôle
├── 📁 entity/            # Entités JPA
├── 📁 repository/        # Repositories Spring Data
└── 📁 config/           # Configurations
```

<div align="center">

### ⭐ N'oubliez pas de donner une étoile au projet !

[![Star History Chart](https://api.star-history.com/svg?repos=username/AgileGestion&type=Date)](https://star-history.com/#username/AgileGestion&Date)


[📧 Contact](mailto:votre@email.com) • [🐛 Issues](https://github.com/username/AgileGestion/issues) • [💡 Suggestions](https://github.com/username/AgileGestion/discussions)

</div>
