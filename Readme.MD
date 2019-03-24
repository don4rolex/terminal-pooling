# Terminals Pooling Microservices

Set of microservices for simulating terminal pooling.
This project uses Spring Cloud, Spring Boot, JDK 10, Maven and JUnit/Mockito/WireMock for testing.

# Build
## What you’ll need
* JDK 10
* Maven 3.5+

## API Usage
1. Open project in IDE of choice. The project is broken down into different sub-projects.
* `naming-server`: This is the Eureka naming server used for registering the different microservices.<br>
Run `NamingServerApplication.java` to start eureka server, the admin console can be found at `localhost:8761`<br>
 
* `terminal-server`: The server microservice as specified in the `Terminal Pooling.pdf`.<br>
Run `TerminalServerApplication.java` to start the `terminal-server`. API documentation will be available at 
`http://localhost:8000/swagger-ui.html`<br>

* `terminal-client`: The client microservice as specified in the `Terminal Pooling.pdf`.<br>
Run `TerminalClientApplication.java` to start the `terminal-client`. API documentation will be available at 
`http://localhost:8100/swagger-ui.html`<br>

2. Run tests with `mvn clean verify` in each sub-project to test each service independently.
3. Standalone jar for each service can be with `mvn clean package`. 

