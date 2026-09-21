# Java Spring Boot E-Commerce Management System

A backend e-commerce learning project built with **Java 21, Spring Boot, PostgreSQL, Maven, JUnit Jupiter, and Mockito**.

This project started as a Java Core + JDBC practice project during my second year studying Software Engineering. It is now being migrated into a **Spring Boot backend application** so I can learn how the concepts I previously implemented manually are handled by a modern Java backend framework.

The main purpose of this repository is still **learning** rather than presenting a production-ready e-commerce platform.

---

## About This Project

I originally built this project manually with Java Core and JDBC to understand the responsibilities of entities, repositories, services, transactions, validation, and database access.

After building that foundation, I started migrating the same e-commerce domain into Spring Boot.

This allows me to compare:

```text
Manual Java backend
        ↓
Spring-managed backend
```

and understand what Spring Boot, Spring MVC, Spring Data JPA, and Spring Security automate or standardize.

The project contains domains such as:

- Users
- Products
- Shopping carts
- Cart items
- Orders
- Order items
- Discounts
- Shipping
- Checkout
- Payments and transactions

My goal is not only to make the application work, but to understand **why each layer exists, how data moves between layers, and how Spring manages those responsibilities**.

---

## Current Learning Objectives

The project currently focuses on the transition from a manually organized Java backend into a Spring Boot application.

### Spring Boot

I am learning how Spring Boot provides the application foundation through:

- Dependency Injection
- Component scanning
- Auto-configuration
- Spring-managed beans
- Embedded web server
- Application configuration
- Spring Boot Maven integration

### Spring MVC and REST APIs

The controller layer is being developed using Spring MVC.

Important concepts include:

- `@RestController`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@PathVariable`
- `@RequestParam`
- `@RequestBody`
- `ResponseEntity`
- HTTP status codes
- JSON request/response handling

The request flow gradually becomes:

```text
Client
  │
  ▼
REST Controller
  │
  ▼
Service
  │
  ▼
Repository
  │
  ▼
Database
```

### DTOs and Validation

Request and response objects are separated from the domain entities through DTOs.

Input validation uses Jakarta Bean Validation through Spring Boot:

```text
HTTP Request
    │
    ▼
Request DTO
    │
    ▼
Bean Validation
    │
    ▼
Controller
    │
    ▼
Service
```

Examples of validation concepts used or studied include:

- `@Valid`
- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Size`
- DTO-based request validation

### Unit Testing

The project now includes unit testing with:

- **JUnit Jupiter (JUnit 5)**
- **Mockito**

The main purpose of unit tests is to verify business logic in isolation.

For example:

```text
Service under test
      │
      ├── Repository dependency → mocked
      ├── Other service         → mocked
      └── Business behavior     → verified
```

Common testing tools and concepts include:

- `@Test`
- `@ExtendWith(MockitoExtension.class)`
- `@Mock`
- `@InjectMocks`
- `when(...).thenReturn(...)`
- `verify(...)`
- `assertEquals(...)`
- `assertThrows(...)`

Unit testing is currently focused mainly on the **service layer**, where most business rules and orchestration are located.

---

## Backend Architecture

The project continues to use a layered architecture, but Spring Boot now manages the application components.

A simplified request flow is:

```text
HTTP Client
    │
    ▼
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
PostgreSQL
```

Each layer has a separate responsibility.

### Controller

Controllers form the HTTP boundary of the application.

They are responsible for:

- Receiving HTTP requests
- Extracting path/query/body data
- Validating request DTOs
- Calling the appropriate service
- Returning HTTP responses
- Selecting appropriate HTTP status codes

Business logic should remain outside the controller whenever possible.

### DTO

DTOs represent data exchanged through the API.

They help prevent API contracts from becoming tightly coupled to database entities.

Typical direction:

```text
JSON Request
    ↓
Request DTO
    ↓
Controller
    ↓
Service
    ↓
Entity
```

and:

```text
Entity
    ↓
Response DTO
    ↓
Controller
    ↓
JSON Response
```

### Service

Services contain application and business logic.

A service may:

```text
validate business conditions
        ↓
load required data
        ↓
check domain rules
        ↓
perform calculations
        ↓
call repositories
        ↓
return a result
```

This layer is also the main target of the current JUnit + Mockito unit tests.

### Repository

Repositories are responsible for persistence and database access.

The project originally implemented repository behavior manually using JDBC.

The next persistence step is to migrate repository implementations toward **Spring Data JPA**, allowing repository abstractions to work with JPA entities and Hibernate instead of manually handling every SQL operation.

Target direction:

```text
Service
   │
   ▼
Spring Data Repository
   │
   ▼
JPA / Hibernate
   │
   ▼
PostgreSQL
```

### Entity

Entities represent the core business objects of the e-commerce system.

Examples include:

```text
User
Product
Cart
CartItem
Order
OrderItem
Discount
```

As the JPA migration progresses, these domain objects will also become persistence mappings through annotations such as:

- `@Entity`
- `@Id`
- `@GeneratedValue`
- `@OneToOne`
- `@OneToMany`
- `@ManyToOne`
- `@JoinColumn`

---

## Package Organization

The project keeps a **feature-based package structure with layered responsibilities inside each feature**.

Instead of placing every controller, repository, or service for the entire application into one global folder, code is grouped primarily by business domain.

A simplified representation of the current organization is:

```text
Java-Springboot-Ecommerce-Management/
│
├── pom.xml
├── README.md
├── QueryUser.sql
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── ...
    │   │       ├── user/
    │   │       │   ├── controller/
    │   │       │   ├── dto/
    │   │       │   ├── entities/
    │   │       │   ├── repository/
    │   │       │   └── service/
    │   │       │
    │   │       ├── product/
    │   │       │   ├── controller/
    │   │       │   ├── dto/
    │   │       │   ├── entities/
    │   │       │   ├── repository/
    │   │       │   └── service/
    │   │       │
    │   │       ├── cart/
    │   │       │   ├── controller/
    │   │       │   ├── dto/
    │   │       │   ├── entities/
    │   │       │   ├── repository/
    │   │       │   └── service/
    │   │       │
    │   │       ├── cart_item/
    │   │       ├── order/
    │   │       ├── order_item/
    │   │       ├── checkout/
    │   │       ├── discount/
    │   │       ├── shipping/
    │   │       ├── payment_method/
    │   │       ├── transaction/
    │   │       └── common/
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        └── java/
            └── ...
                ├── user/
                ├── product/
                ├── cart/
                ├── order/
                └── checkout/
```

The exact folders continue to evolve while Spring-specific responsibilities are introduced.

The main architectural rule remains:

```text
feature
 ├── controller
 ├── dto
 ├── entity/entities
 ├── repository
 └── service
```

This lets each feature stay understandable while still preserving separation between HTTP handling, business logic, persistence, and domain data.

---

## Why Feature-Based Packaging?

A traditional layered layout can look like:

```text
controller/
service/
repository/
entity/
dto/
```

That layout works, but as an application grows each folder may contain classes from many unrelated domains.

This project instead groups code primarily around business capabilities:

```text
user/
product/
cart/
order/
checkout/
```

Each feature can then contain its own layers:

```text
product/
├── controller/
├── dto/
├── entities/
├── repository/
└── service/
```

This makes it easier to understand which classes belong to one business concept while still practicing layered architecture.

---

## Example Request Flow

A typical Spring Boot request is expected to follow this direction:

```text
Client
  │
  ▼
HTTP Request
  │
  ▼
Controller
  │
  ▼
Request DTO + Validation
  │
  ▼
Service
  │
  ▼
Repository
  │
  ▼
PostgreSQL
```

The result returns in the opposite direction:

```text
PostgreSQL
    │
    ▼
Repository
    │
    ▼
Entity
    │
    ▼
Service
    │
    ▼
Response DTO
    │
    ▼
Controller
    │
    ▼
HTTP Response
```

---

## Example: Checkout Flow

Checkout remains one of the most important workflows because multiple domains must cooperate.

A simplified flow is:

```text
Checkout Request
       │
       ▼
Validate User / Cart
       │
       ▼
Load Cart Items
       │
       ▼
Load Products
       │
       ▼
Validate Stock
       │
       ▼
Calculate Subtotal
       │
       ▼
Calculate Shipping
       │
       ▼
Apply Discount
       │
       ▼
Calculate Total
       │
       ▼
Create Order
       │
       ▼
Create Order Items
       │
       ▼
Update Product Stock
       │
       ▼
Clear Cart
       │
       ▼
Commit Transaction
```

This flow is useful for practicing:

- Service orchestration
- Repository interaction
- Object relationships
- DTO boundaries
- Business validation
- Exception handling
- Database transactions
- Unit testing
- Future Spring transaction management

---

## Persistence: JDBC to Spring Data JPA

The original version of this project used JDBC directly.

That stage helped me understand:

```java
Connection
PreparedStatement
ResultSet
executeQuery()
executeUpdate()
commit()
rollback()
```

and SQL concepts such as:

```sql
SELECT
INSERT
UPDATE
DELETE
JOIN
FOREIGN KEY
UNIQUE
TRANSACTION
```

The next stage is migrating persistence to:

```text
Spring Data JPA
      ↓
JPA / Hibernate
      ↓
PostgreSQL
```

The goal is not to forget JDBC, but to understand what JPA and Hibernate abstract away.

Planned repository concepts include:

- `JpaRepository`
- Derived query methods
- Entity relationships
- Persistence context
- Dirty checking
- Transaction boundaries
- JPQL where necessary

---

## Transaction Management

The original application handled transaction boundaries manually.

For example:

```text
BEGIN TRANSACTION

Create Order
Create OrderItems
Update Product Stock
Clear Cart

        │
        ├── success → COMMIT
        │
        └── failure → ROLLBACK
```

As the project moves further into Spring Boot and JPA, this responsibility will gradually move toward Spring transaction management using:

```java
@Transactional
```

This gives me a useful comparison between manual JDBC transaction control and framework-managed transactions.

---

## Testing Strategy

### Unit Tests — Current

Unit testing is already part of the project.

Current tools:

- JUnit Jupiter
- Mockito
- Spring Boot Test dependencies

Main testing target:

```text
Service Layer
```

Typical unit-test structure:

```text
Arrange
  ↓
Mock dependencies
  ↓
Act
  ↓
Call service method
  ↓
Assert
  ↓
Verify result and interactions
```

The purpose is to test business logic without requiring the real database or full Spring application context.

### Controller Integration Testing — Planned

The next testing step is integration/controller testing using **MockMvc**.

Target flow:

```text
Mock HTTP Request
       │
       ▼
MockMvc
       │
       ▼
Spring MVC
       │
       ▼
Controller
       │
       ▼
Validation / Exception Handling
       │
       ▼
HTTP Response Assertions
```

Planned concepts include:

- `@SpringBootTest`
- `@WebMvcTest`
- `@AutoConfigureMockMvc`
- `MockMvc`
- JSON request bodies
- HTTP status assertions
- Response body assertions
- Validation testing
- Controller/exception-handler testing

Repository integration tests may later be added when the JPA migration is complete.

---

## Authentication and Security — Planned

Authentication and authorization are planned as part of the Spring Boot migration.

The project will use **Spring Security** to learn how backend authentication is integrated into the request lifecycle.

Target architecture:

```text
Client
  │
  ▼
Authentication Request
  │
  ▼
Spring Security
  │
  ▼
Authentication
  │
  ▼
Authorization
  │
  ▼
Protected Controller
  │
  ▼
Service
```

Planned topics include:

- Spring Security configuration
- Password encoding
- Authentication
- Authorization
- User identity
- Roles and permissions
- Protected endpoints
- Security filters
- `SecurityContext`
- `UserDetails`
- `UserDetailsService`

The exact authentication mechanism will be implemented progressively as the project develops.

The earlier password-hashing work remains useful background for understanding how Spring Security handles passwords.

---

## Error Handling

The application continues to use custom exceptions to represent domain and business failures.

Examples include:

```text
ResourceNotFound
InvalidInput
DuplicateResource
StockNotEnough
BusinessRuleViolation
```

With Spring MVC, error handling can be centralized through:

```java
@RestControllerAdvice
@ExceptionHandler
```

Target flow:

```text
Service throws exception
        │
        ▼
Controller layer
        │
        ▼
Global Exception Handler
        │
        ▼
Structured HTTP error response
```

This keeps repetitive `try/catch` logic out of individual controllers.

---

## Technologies

### Current Stack

| Technology | Purpose |
| --- | --- |
| Java 21 | Main programming language |
| Spring Boot | Application framework and dependency management |
| Spring MVC | REST controller and HTTP request handling |
| Jakarta Bean Validation | DTO input validation |
| PostgreSQL | Relational database |
| JDBC | Existing/manual persistence foundation during migration |
| Maven | Build and dependency management |
| Lombok | Reduce repetitive Java boilerplate |
| jBCrypt | Password hashing foundation |
| JUnit Jupiter | Unit testing |
| Mockito | Mocking dependencies in unit tests |
| Spring Boot Test | Spring testing infrastructure |
| IntelliJ IDEA | Development environment |
| Git & GitHub | Version control and project history |

### Planned / Next Stack

| Technology | Purpose |
| --- | --- |
| Spring Data JPA | Repository abstraction |
| Hibernate | JPA implementation / ORM |
| Spring Security | Authentication and authorization |
| MockMvc | Controller and HTTP integration testing |
| Spring Transaction Management | Framework-managed transaction boundaries |

---

## Maven

Maven manages the project build and dependencies.

The main configuration is stored in:

```text
pom.xml
```

Useful commands include:

```bash
mvn clean
mvn compile
mvn test
mvn clean test
mvn clean package
mvn spring-boot:run
```

The project now uses the Spring Boot parent and Spring Boot Maven plugin so dependency and build configuration can be managed consistently by Spring Boot.

---

## Current Development Direction

The project is no longer focused only on Java Core syntax and manual JDBC architecture.

The current progression is:

```text
Java Core / OOP Foundation
          ↓
Layered Architecture
          ↓
JDBC / PostgreSQL
          ↓
DTOs + Validation
          ↓
JUnit Jupiter + Mockito
          ↓
Spring Boot
          ↓
Spring MVC Controllers
          ↓
Spring Data JPA
          ↓
MockMvc Integration Testing
          ↓
Spring Security
          ↓
Authentication & Authorization
```

The earlier Java Core and JDBC implementation remains important because it provides the foundation for understanding what Spring now manages automatically.

---

## Development Philosophy

Most implementation decisions in this repository are made as part of the learning process.

I use AI as a learning assistant mainly to:

- Explain unfamiliar concepts
- Review architecture
- Point out design problems
- Compare alternative approaches
- Help diagnose errors
- Generate questions for code review
- Accelerate repetitive implementation while I study the underlying behavior

I do not want AI-generated code to become a black box.

When a framework or AI introduces something new, I try to understand questions such as:

```text
Why does this annotation exist?

What object does Spring create here?

Which layer should own this logic?

Why should this dependency be injected?

What does JPA replace from my JDBC implementation?

What exactly does Mockito mock?

What happens inside a MockMvc test?

Where does authentication happen before the controller?

What happens if this transaction fails?
```

The goal is to use modern tools without losing understanding of the fundamentals beneath them.

---

## What This Project Is Not

This repository is **not intended to represent a production-ready commercial e-commerce backend yet**.

Some parts are intentionally simplified because the repository is being used as a progressive learning environment.

Features such as complete security hardening, production deployment, observability, caching, distributed architecture, payment-provider integration, and advanced infrastructure are outside the current scope.

---

## Roadmap

### Completed / Currently Practicing

- [x] Java Core fundamentals
- [x] Object-Oriented Programming
- [x] Layered architecture fundamentals
- [x] Feature-based package organization
- [x] JDBC
- [x] PostgreSQL
- [x] Maven
- [x] DTOs
- [x] Jakarta Bean Validation
- [x] Password hashing fundamentals
- [x] JUnit Jupiter
- [x] Mockito
- [x] Spring Boot project setup
- [x] Spring MVC foundation

### Next

- [ ] Complete Spring MVC controllers
- [ ] Migrate repositories to Spring Data JPA
- [ ] Map entities with JPA/Hibernate
- [ ] Move transaction boundaries to Spring `@Transactional`
- [ ] Add repository/integration tests
- [ ] Add controller tests with MockMvc
- [ ] Add Spring Security
- [ ] Implement authentication
- [ ] Implement authorization and role-based access
- [ ] Improve API error responses
- [ ] Continue refactoring architecture as the project grows

---

## Project Status

🚧 **Work in Progress — Spring Boot Learning Project**

This repository is actively evolving from a manually implemented Java/JDBC backend into a Spring Boot application.

The architecture, persistence strategy, tests, package structure, and security design may continue to change as I learn and refactor previous implementations.

Those changes are an intentional part of the project.

---

## Author

**Hai Anh Phan**

Software Engineering Student  
Backend Engineering Learner

This repository documents my progression from Java backend fundamentals toward building structured Spring Boot applications.
