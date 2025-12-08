# Flight Booking System – Gateway Security & Docker Containerization

This project is a fully containerized Flight Booking microservices system secured with JWT-based authentication and role-based authorization (Admin/User).
All services - API Gateway, Eureka Server, Config Server, Flight Service, Booking Service, Kafka, and MongoDB- run in isolated Docker containers orchestrated using a single Docker Compose setup.
The API Gateway enforces security filters, validates JWT tokens, and controls access to protected endpoints.
Spring Cloud Config externalizes configuration, while Eureka enables dynamic service discovery across containers.


[View Summary Document](https://github.com/kanchanraiii/FlightBookingSysWSecurity/blob/main/Project%20Summary%20Doc.pdf)

---

## System Architecture

The system architecture describes all the services, their connections, DBs, and overall layout.

![System Architecture](System%20Architecture.drawio.png)
---

## Docker Containers Running

On running the `docker-compose.yml` all other containers connected to it run successfully

![Docker Containers Running](Docker%20Containers%20Running.png)

---

## All Services - Ports

Below are all the services implemented in this project their ports and dependencies.

| Service Name | Port (External) | Description | Dependencies |
| :--- | :--- | :--- | :--- |
| eureka-service | `8761` | Central service registry for all microservices. | Spring Cloud Eureka |
| config-service | `8888` | Provides centralized configuration management to all services. | Spring Cloud Config |
| api-gateway-service | `8000` | The single entry point for all client requests, handling routing, security, and load balancing. | Spring Cloud Gateway |
| flight-service | `8081` | Manages all airline and flight inventory data (CRUD operations). | Spring Boot, MongoDB |
| booking-service | `8082` | Handles flight reservation, ticket generation, and history tracking. | Spring Boot, MongoDB |

---

## All API Endpoints 
Below are all the endpoints their HTTP method and access role.
| Route | Method | Access |
| :--- | :--- | :--- |
| /api/auth/register | POST | Public |
| /api/auth/login | POST | Public |
| /flight/api/flight/getAllAirlines | GET | Public |
| /flight/api/flight/getAllFlights | GET | Public |
| /flight/api/flight/search | POST | Public |
| /flight/api/flight/addAirline | POST | Admin |
| /flight/api/flight/airline/inventory/add | POST | Admin |
| /booking/api/booking/{flightId} | POST | User |
| /booking/api/booking/ticket/{pnr} | GET | User + Admin |
| /booking/api/booking/history/{email} | GET | User + Admin |
| /booking/api/booking/cancel/{pnr} | DELETE | User |

---

## Things Implemented

These are the features and implementations carried out in this project.

- Dockerization of each service. Creating a single `docker-compose.yml` to run the containers
- JMeter Load Testing with 20,50 and 100 Threads
- Unit testing
- Sonar Cloud Analysis andCoverage
- JWT Authentication with Spring Security for Admin and User Roles
- Validations & Exception Handling 
- Circuit Breaker and Open Feign
- Apache Kafka Msg Broker when flights are booked/cancelled

---

## SonarQube
Below is the SonarQube Report after fixing all the issues (29 Maintainibilty 15 Reliabilty).

![SonarQube](https://github.com/kanchanraiii/FlightBookingSystemMicroservice/blob/main/After2.png)

---
