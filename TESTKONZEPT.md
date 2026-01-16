# Testkonzept für Noteboard Application

## 1. Software-Architektur (Skizze)

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Application  │  │  Notebook    │  │    Note      │       │
│  │ UserController│ │ Controller  │  │ Controller   │        │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                  │                  │             │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
┌─────────┼──────────────────┼──────────────────┼─────────────┐
│         │                  │                  │             │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐       │
│  │ Application  │  │  Notebook    │  │    Note      │       │
│  │ UserService  │  │  Service     │  │  Service     │       │
│  │ AuthService  │  │              │  │  TagService  │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                  │                  │             │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
┌─────────┼──────────────────┼──────────────────┼─────────────┐
│         │                  │                  │             │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐     │
│  │ Application  │  │  Notebook    │  │    Note      │     │
│  │ User         │  │  Repository  │  │ Repository  │     │
│  │ Repository   │  │              │  │ TagRepository│    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │             │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                    ┌────────▼────────┐
                    │  PostgreSQL     │
                    │   Database      │
                    └─────────────────┘
```

### Architektur-Beschreibung

Die Noteboard-Anwendung folgt einer **3-Schichten-Architektur**:

1. **Controller Layer (REST API)**

    - RESTful Endpoints für CRUD-Operationen
    - JWT-basierte Authentifizierung
    - HTTP Request/Response Handling

2. **Service Layer (Business Logic)**

    - Geschäftslogik und Validierung
    - Transaktionsmanagement
    - Fehlerbehandlung

3. **Repository Layer (Data Access)**

    - Datenbankzugriff über Hibernate/Panache
    - Entity-Management

4. **Model Layer**
    - JPA Entities: ApplicationUser, Notebook, Note, Tag
    - Beziehungen: User → Notebooks → Notes → Tags

## 2. Zu testende Komponenten

### 2.1 Service Layer (Unit Tests)

-   `ApplicationUserService` - Benutzerverwaltung
-   `AuthService` - Authentifizierung und JWT-Generierung
-   `NotebookService` - Notebook-Verwaltung
-   `NoteService` - Notiz-Verwaltung
-   `TagService` - Tag-Verwaltung

### 2.2 Controller Layer (Integration Tests)

-   `ApplicationUserController` - REST Endpoints für Benutzer
-   `NotebookController` - REST Endpoints für Notebooks
-   `NoteController` - REST Endpoints für Notizen
-   `TagController` - REST Endpoints für Tags

### 2.3 Helper Classes

-   Exception Mapper (ResourceNotFoundException, ValidationException)
-   Generic Exception Handling

### 2.4 End-to-End Tests

-   `EndToEndNotebookFlowTest` - Komplette Workflows

## 3. Zu testende Features

### 3.1 Benutzerverwaltung (ApplicationUser)

-   Benutzer-Registrierung
-   Benutzer-Login mit JWT-Generierung
-   Benutzer-Auflistung
-   Benutzer-Abfrage nach ID
-   Benutzer-Update
-   Benutzer-Löschung
-   Passwort-Hashing (BCrypt)
-   Validierung (Username, Password, Role)

### 3.2 Notebook-Verwaltung

-   Notebook-Erstellung (mit User-Zuordnung)
-   Notebook-Auflistung (alle / für aktuellen User)
-   Notebook-Abfrage nach ID
-   Notebook-Update
-   Notebook-Löschung
-   Validierung (Title, Description)

### 3.3 Notiz-Verwaltung

-   Notiz-Erstellung (mit Notebook-Zuordnung)
-   Notiz-Auflistung
-   Notiz-Abfrage nach ID
-   Notiz-Update
-   Notiz-Löschung
-   Tag-Zuordnung zu Notizen
-   Tag-Entfernung von Notizen
-   Validierung (Title, Content, Notebook)

### 3.4 Tag-Verwaltung

-   Tag-Erstellung
-   Tag-Auflistung
-   Tag-Abfrage nach ID
-   Tag-Update
-   Tag-Löschung
-   Validierung (Name, Eindeutigkeit)

### 3.5 Authentifizierung & Autorisierung

-   JWT-Token-Generierung
-   JWT-Token-Validierung
-   Rollenbasierte Zugriffskontrolle (user, admin)
-   Authentifizierte Endpoints

### 3.6 Fehlerbehandlung

-   ResourceNotFoundException (404)
-   ValidationException (400)
-   Generic Exception Mapping (500)

## 4. Gewählte Testumgebung

### 4.1 Test-Frameworks

-   **JUnit 5** - Test-Framework
-   **Mockito** - Mocking-Framework für Unit Tests
-   **REST Assured** - API-Testing für Integration Tests
-   **QuarkusTest** - Quarkus-spezifische Test-Unterstützung

### 4.2 Test-Datenbank

-   **PostgreSQL** - In-Memory oder Test-Datenbank
-   **Hibernate ORM** - JPA-Implementierung
-   **Panache** - Active Record Pattern

### 4.3 Build-Tool

-   **Maven** - Dependency Management und Build
-   **Maven Surefire Plugin** - Unit Tests
-   **Maven Failsafe Plugin** - Integration Tests

### 4.4 Test Coverage

-   **JaCoCo** - Code Coverage Tool
-   **Ziel: Mindestens 80% Coverage**

### 4.5 CI/CD

-   **GitHub Actions** - Automatisierte Test-Ausführung
-   **Automatische Coverage-Reports**

## 5. Test-Planung

### 5.1 Test-Strategie

#### Unit Tests (Service Layer)

-   **Ziel**: Isolierte Tests der Business-Logik
-   **Mocking**: Alle Repository-Abhängigkeiten werden gemockt
-   **Coverage**: Alle Service-Methoden und Validierungen
-   **Ausführung**: Schnell, ohne Datenbank

#### Integration Tests (Controller Layer)

-   **Ziel**: Test der REST-API-Endpoints
-   **Umgebung**: QuarkusTest mit echter Datenbank
-   **Authentifizierung**: JWT-Token für geschützte Endpoints
-   **Coverage**: Alle HTTP-Methoden und Status-Codes

#### End-to-End Tests

-   **Ziel**: Komplette Workflows testen
-   **Szenarien**: Benutzer-Registrierung → Notebook-Erstellung → Notiz-Erstellung → Tag-Zuordnung

### 5.2 Test-Daten-Management

-   **Isolation**: Jeder Test erstellt eigene Test-Daten
-   **Cleanup**: Automatisches Rollback nach jedem Test
-   **Eindeutigkeit**: Zeitstempel-basierte Usernames für Parallelität

### 5.3 Test-Ausführung

#### Lokale Ausführung

```bash
# Unit Tests
./mvnw test

# Integration Tests
./mvnw verify

# Mit Coverage
./mvnw clean test jacoco:report
```

#### CI/CD Ausführung

-   Automatisch bei jedem Push/PR
-   Coverage-Report wird generiert und hochgeladen
-   Build schlägt fehl bei Coverage < 80%

### 5.4 Test-Priorisierung

**High Priority:**

-   Service-Layer Validierungen
-   Authentifizierung und Autorisierung
-   CRUD-Operationen
-   Fehlerbehandlung

**Medium Priority:**

-   Edge Cases (leere Strings, null-Werte)
-   Beziehungen zwischen Entities
-   Transaktionsmanagement

**Low Priority:**

-   Performance-Tests
-   Last-Tests

### 5.5 Erfolgskriterien

-   Alle Unit Tests bestehen
-   Alle Integration Tests bestehen
-   Test Coverage ≥ 80%
-   Keine kritischen Bugs
-   Alle Features getestet
-   CI/CD Pipeline erfolgreich

## 6. Mocking-Strategie

### 6.1 Gemockte Komponenten

**In Service-Tests:**

-   `ApplicationUserRepository` - User-Datenbankzugriff
-   `NotebookRepository` - Notebook-Datenbankzugriff
-   `NoteRepository` - Notiz-Datenbankzugriff
-   `TagRepository` - Tag-Datenbankzugriff
-   `JsonWebToken` - JWT-Token für Authentifizierung

### 6.2 Mocking-Patterns

**Verwendete Mockito-Features:**

-   `mock()` - Mock-Objekte erstellen
-   `when().thenReturn()` - Verhalten definieren
-   `verify()` - Aufrufe verifizieren
-   `@BeforeEach` - Setup für jeden Test

### 6.3 Beispiel Mocking

```java
@BeforeEach
void setUp() {
    noteRepository = mock(NoteRepository.class);
    notebookRepository = mock(NotebookRepository.class);
    when(notebookRepository.findById(1L)).thenReturn(notebook);
}
```

## 7. Test-Abdeckung

### 7.1 Aktuelle Coverage (Ziel: ≥80%)

**Service Layer:**

-   ApplicationUserService: ~95%
-   AuthService: ~90%
-   NotebookService: ~90%
-   NoteService: ~95%
-   TagService: ~90%

**Controller Layer:**

-   ApplicationUserController: ~85%
-   NotebookController: ~85%
-   NoteController: ~90%
-   TagController: ~85%

### 7.2 Coverage-Metriken

-   **Line Coverage**: Anteil ausgeführter Codezeilen
-   **Branch Coverage**: Anteil ausgeführter Verzweigungen
-   **Method Coverage**: Anteil ausgeführter Methoden
-   **Class Coverage**: Anteil getesteter Klassen

## 8. Risiken und Mitigation

### 8.1 Risiken

-   **Datenbank-Abhängigkeiten**: Gelöst durch Mocking
-   **Parallele Test-Ausführung**: Gelöst durch eindeutige Test-Daten
-   **JWT-Token-Gültigkeit**: Gelöst durch pro-Test Token-Generierung

### 8.2 Best Practices

-   Tests sind unabhängig voneinander
-   Tests sind wiederholbar (deterministisch)
-   Tests sind schnell ausführbar
-   Tests haben klare Namen und Dokumentation
