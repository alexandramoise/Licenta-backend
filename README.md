# Bachelor's Thesis Application - Server Component
A web server for my bloodpressure monitoring app built using the Spring Boot framework. 
It is responsible for processing client side requests through the endpoints of the REST API and performing the desired functionalities.

### Features
- authentication for doctors and patients with Spring Security
- registering doctor and patient accounts
- managing:
      - treatments
      - appointments
      - recommendations
      - medical conditions
      - bloodpressure readings
- computing statistics for list of patients / specific patient
- sending emails as reminders
- clustering patients based on their bloodpressure trackings evolution

### Requirements
- Java Development Kit (JDK) >= 8
- Gradle
- PostgreSQL as DBMS

### Running the app
- clone this repository - copy this command in a terminal: git clone https://github.com/alexandramoise/Licenta-backend
- configure the database connection properties in src/main/resources/application.yaml
- run the app using the command line: ./gradlew bootRun or from IDE running the main class
