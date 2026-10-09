# Contact Book — Spring Boot application

A simple personal contact manager with a responsive HTML/CSS/JavaScript frontend,
Spring Boot REST API, JDBC queries and a persistent database.

## Features

- Add, view, edit and delete contacts.
- Search by name, phone, email or address, ignoring letter case.
- Required name and phone; optional email and address.
- Server-side validation and clear error messages.
- Delete confirmation and responsive layout.
- H2 file database by default; optional PostgreSQL profile.

## Run in Eclipse

1. Extract this ZIP first.
2. Use JDK 17 or 21 for the project. Spring Boot 3.5 requires Java 17 or later.
3. Select **File → Import → Maven → Existing Maven Projects**.
4. Select the extracted `contactbook` folder containing `pom.xml`, then Finish.
5. Wait for Maven to download dependencies (internet is needed the first time).
6. Open `src/main/java/com/ishaq/contactbook/ContactBookApplication.java`.
7. Right-click it → **Run As → Java Application**. Spring Tools is not required.
8. Open **http://localhost:8080** in your browser.

Do not double-click index.html: the backend must serve the page.
There is no username or password in this basic single-user application.
It listens only on your own computer (127.0.0.1) by default.

## Run from a terminal

Requirements: JDK 17+ and Maven 3.6.3+.
Open a terminal in the folder containing pom.xml:

```shell
mvn spring-boot:run
```

Build and test:

```shell
mvn clean package
java -jar target/contactbook-1.0.0.jar
```

If this ZIP includes `contactbook-1.0.0.jar` at the project root, you can run it
directly with `java -jar contactbook-1.0.0.jar`, without Maven.

Stop the app with Ctrl+C or Eclipse's red Stop button before starting another copy.

## Where contacts are saved

The default H2 database is `data/contactbook.mv.db`, relative to the working
directory from which you start the application. Use the same working directory
each time to keep using the same contacts. Stop the app before backing up the
data folder. The schema is created automatically and does not delete existing data.
No sample contacts are inserted. Duplicate names, phones and emails are allowed.

## Optional: PostgreSQL

H2 works immediately; PostgreSQL is not required.

1. Create a database in pgAdmin or psql:

```sql
CREATE DATABASE contactbook;
```

2. In Eclipse, open **Run → Run Configurations → Java Application →
ContactBookApplication → Environment**, and add:

| Variable | Value |
| --- | --- |
| SPRING_PROFILES_ACTIVE | postgres |
| DB_URL | jdbc:postgresql://localhost:5432/contactbook |
| DB_USERNAME | your PostgreSQL username |
| DB_PASSWORD | your PostgreSQL password |

3. Apply and run. Tables are created automatically in that database.

Or use PowerShell in the project directory:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/contactbook"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_PASSWORD"
mvn spring-boot:run
```

Switching profiles does not transfer contacts between H2 and PostgreSQL.
Remove SPRING_PROFILES_ACTIVE to use H2 again.

## Project structure and request flow

| File | Purpose |
| --- | --- |
| pom.xml | Dependencies, Java version and build configuration |
| ContactBookApplication.java | Starts Spring Boot and embedded Tomcat |
| Contact.java | Represents a saved contact |
| ContactRequest.java | Validates and trims submitted fields |
| ContactController.java | Receives REST requests and returns responses |
| ContactRepository.java | Executes parameterized SQL using JdbcTemplate |
| ApiExceptionHandler.java | Produces readable validation/error responses |
| application.properties | Port and H2 settings |
| application-postgres.properties | Optional PostgreSQL settings |
| schema.sql | Creates the contacts table if missing |
| static/index.html | Page and contact form |
| static/styles.css | Responsive page design |
| static/app.js | Calls API with fetch and renders contacts safely |
| ContactApiTest.java | Integration tests against an isolated H2 database |

Java files are under `src/main/java/com/ishaq/contactbook`.
Configuration and static files are under `src/main/resources`.
Tests are under `src/test/java/com/ishaq/contactbook`.

When you click Save: JavaScript sends JSON → controller validates the fields →
repository writes to the database → API returns the saved contact → page reloads
the contact list. Spring Boot embeds the servlet container; no separate Tomcat
installation or manual servlet class is needed.

## REST API

| Method | URL | Purpose |
| --- | --- | --- |
| GET | /api/contacts | List contacts alphabetically |
| GET | /api/contacts?search=ishaq | Search contacts |
| GET | /api/contacts/1 | Get one contact |
| POST | /api/contacts | Create a contact (201) |
| PUT | /api/contacts/1 | Update a contact (200) |
| DELETE | /api/contacts/1 | Delete a contact (204) |

POST and PUT use `Content-Type: application/json`:

```json
{
  "name": "Mohamed Ishaq",
  "phone": "+91 98765 43210",
  "email": "ishaq@example.com",
  "address": "Chennai"
}
```

Validation errors return 400; missing contacts return 404. Phone values allow
7–25 digits/formatting characters and must include at least one digit; this is
basic format validation, not verification that the number exists.

## Troubleshooting

- **Port 8080 already in use:** stop the previous app, or change `server.port`
  to 8081 and open http://localhost:8081.
- **Imports are red in Eclipse:** right-click project → Maven → Update Project,
  then check that the project uses JDK 17+.
- **Database locked:** stop other copies of the application.
- **Contacts seem missing:** use the same working directory and database profile.
- **Page cannot reach API:** open localhost:8080 after the backend starts;
  do not open the HTML as a file URL.

This is a local learning project without accounts, access control or pagination.
Add authentication and deployment configuration before hosting it for other users.

Reference: https://docs.spring.io/spring-boot/3.5/system-requirements.html
