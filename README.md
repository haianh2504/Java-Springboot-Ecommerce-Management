# Java E-Commerce Management System

A backend e-commerce practice project built with **Java Core, JDBC, PostgreSQL, and Maven**.

This project was started during my **second year studying Software Engineering** as a way to move from learning individual Java concepts to understanding how a backend application is actually organized.

The main purpose of this repository is **learning**, not building a production-ready e-commerce platform.

Instead of relying on frameworks such as Spring Boot, I intentionally implement most of the application manually so that I can understand what happens underneath higher-level frameworks.

---

## About This Project

Before starting this project, I had studied programming fundamentals, basic Data Structures & Algorithms, and Object-Oriented Programming.

I chose an e-commerce backend because it contains many related domains and business flows, such as:

* Users
* Products
* Shopping carts
* Cart items
* Orders
* Order items
* Discounts
* Shipping
* Checkout
* Payments and transactions

Working with these domains gives me a practical environment for learning how objects communicate with each other and how backend responsibilities should be separated.

My main goal is not simply to make the system "work".

The goal is to understand **why the system should be designed in a particular way**.

---

## Learning Objectives

Through this project, I am practicing and improving my understanding of:

### Java Core

Getting comfortable with Java syntax and commonly used Java features, including:

* Classes and objects
* Interfaces
* Abstract classes
* Inheritance and polymorphism
* Encapsulation
* Enums
* Generics
* Collections
* `Optional`
* Exception handling
* Custom exceptions
* Functional interfaces and lambdas
* `BigDecimal`
* Date and time APIs

### Object-Oriented Programming

I use the e-commerce domain to practice modeling real concepts as objects.

For example:

```text
User
  │
  └── Cart
       │
       └── CartItem
              │
              └── Product

Cart
  │
  └── Checkout
        │
        ├── Order
        │    └── OrderItem
        │
        ├── Discount
        └── Shipping
```

This helps me understand where data and behavior should belong instead of putting everything inside one large class.

---

## Backend Architecture

One of the most important goals of this project is learning how data moves through different backend responsibilities.

A simplified flow is:

```text
Application / Use Case
        │
        ▼
     Service
        │
        ▼
   Repository
        │
        ▼
      JDBC
        │
        ▼
   PostgreSQL
```

When data is read:

```text
PostgreSQL
    │
    ▼
   JDBC
    │
    ▼
Repository
    │
    ▼
 Entity
    │
    ▼
 Service
```

Each part has a different responsibility.

### Entity

Entities represent the core objects of the application.

Examples:

```text
User
Product
Cart
CartItem
Order
OrderItem
Discount
```

They contain the data and domain behavior related to those concepts.

### Repository

Repositories are responsible for persistence.

They define operations such as:

```text
findById(...)
save(...)
update(...)
delete(...)
findByUserId(...)
findByCartId(...)
```

The JDBC implementations communicate directly with PostgreSQL.

This layer allows business logic to work with repository abstractions instead of writing SQL everywhere in the application.

### Service

Services contain application and business logic.

For example, a service may:

```text
validate input
      ↓
find required entities
      ↓
check business rules
      ↓
perform calculations
      ↓
call repositories
      ↓
return the result
```

This separation helps prevent database code, validation rules, and business logic from becoming mixed together.

---

## Package Organization

The project currently uses a **feature-based package structure with layered responsibilities inside each feature**.

Instead of putting every repository in one global repository folder and every service in one global service folder, related classes are grouped around their business domain.

A simplified representation of the project is:

```text
Java_ECommerceSystem_ManualPractice/
│
├── pom.xml
├── README.md
│
└── src/
    └── main/
        └── java/
            │
            ├── user/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── product/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── cart/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── cart_item/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── order/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── order_item/
            │   ├── entities/
            │   ├── repository/
            │   └── service/
            │
            ├── checkout/
            │   ├── entities/
            │   └── service/
            │
            ├── discount/
            ├── shipping/
            ├── payment_method/
            ├── transaction/
            ├── common/
            │
            └── Main.java
```

The exact structure may continue to change as I learn better ways to organize the application.

That evolution is intentional.

One of the purposes of this repository is to document how my understanding of software architecture develops over time.

---

## Why Feature-Based Packaging?

Originally, layered architecture can be imagined as:

```text
entities/
repositories/
services/
```

However, when the application grows, those folders can contain many unrelated classes.

This project therefore groups code primarily by domain:

```text
user/
product/
cart/
order/
checkout/
```

Then each feature can contain its own layers:

```text
product/
├── entities/
├── repository/
└── service/
```

This makes it easier for me to understand which classes belong to the same business concept while still practicing separation of responsibilities.

---

## Example: Checkout Flow

Checkout is one of the more complex workflows in the project because it requires several parts of the system to cooperate.

A simplified checkout flow is:

```text
User requests checkout
        │
        ▼
Find Cart
        │
        ▼
Verify Cart ownership
        │
        ▼
Load CartItems
        │
        ▼
Load Products
        │
        ▼
Validate product availability
and stock quantities
        │
        ▼
Calculate subtotal
        │
        ▼
Calculate shipping fee
        │
        ▼
Apply discount
        │
        ▼
Calculate final price
        │
        ▼
Create Order
        │
        ▼
Create OrderItems
        │
        ▼
Update required data
        │
        ▼
Commit transaction
```

This workflow is particularly useful for learning:

* Service orchestration
* Repository interaction
* Object relationships
* Database transactions
* Business validation
* Error handling
* Separation of responsibilities

---

## Database Access

The project uses **JDBC directly** instead of an ORM such as Hibernate.

This is intentional.

I want to understand the lower-level process first:

```text
Java Application
      │
      ▼
JDBC API
      │
      ▼
PostgreSQL JDBC Driver
      │
      ▼
PostgreSQL
```

This allows me to practice concepts such as:

```java
Connection
PreparedStatement
ResultSet
executeQuery()
executeUpdate()
commit()
rollback()
```

as well as SQL operations including:

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

Learning JDBC manually gives me a clearer understanding of what higher-level persistence frameworks will eventually automate.

---

## Transaction Management

Some operations involve multiple database changes that should behave as one unit.

Checkout is an example.

Conceptually:

```text
BEGIN TRANSACTION

Create Order
Create OrderItems
Update Product Stock
Update Cart

        │
        ├── everything succeeds → COMMIT
        │
        └── something fails     → ROLLBACK
```

I am using this project to learn why transactions are important and how JDBC transaction management works before relying on framework features such as Spring's `@Transactional`.

---

## Technologies

| Technology              | Purpose                                           |
| ----------------------- | ------------------------------------------------- |
| Java 21                 | Main programming language                         |
| JDBC                    | Direct database communication                     |
| PostgreSQL              | Relational database                               |
| Maven                   | Project build and dependency management           |
| Lombok                  | Reducing repetitive Java boilerplate              |
| Jakarta Bean Validation | Declarative validation for DTO input data         |
| jBCrypt                 | Secure password hashing and password verification |
| JUnit Jupiter & Mockito | Unit testing and mocking service dependencies     |
| IntelliJ IDEA           | Development environment                           |
| Git & GitHub            | Version control and learning history              |


## Maven

This is also my first project where I am getting used to **Maven as a Java project manager**.

Maven helps me manage:

```text
Project structure
Dependencies
Compilation
Testing
Build lifecycle
```

The main configuration is stored in:

```text
pom.xml
```

Using Maven prepares the project for adding libraries and testing tools in a structured way as it grows.

---

## Lombok

The project uses **Lombok** for convenient annotations where appropriate.

For example, Lombok can reduce repetitive code such as constructors, getters, setters, and object utility methods.

However, because this is primarily a learning project, I still try to understand what code Lombok generates instead of treating the annotations as magic.

---

## Error Handling

The project also serves as practice for Java exception handling.

I am experimenting with:

```text
IllegalArgumentException
NullPointerException
Custom RuntimeException classes
Exception chaining
Repository-related exceptions
Resource-not-found exceptions
Business-rule exceptions
```

The goal is to understand where errors should be detected and which layer should be responsible for handling or propagating them.

---

## Current Focus

My current focus is improving the internal architecture rather than adding as many features as possible.

I am especially practicing:

```text
Java syntax
      +
Object-Oriented Programming
      +
Domain modeling
      +
Entity design
      +
Repository pattern
      +
Service layer
      +
JDBC
      +
SQL
      +
Transactions
      +
Exception handling
      +
Unit testing
```

The project will continue evolving as my understanding improves.

---

## Development Philosophy

Most of the implementation is written manually.

I use AI as a **learning assistant**, mainly to:

* Explain unfamiliar concepts
* Review my design decisions
* Point out architectural problems
* Discuss alternatives
* Help me understand errors
* Guide me step by step when learning new concepts

I try not to treat AI-generated code as a finished solution.

When AI suggests an implementation, my goal is to understand:

```text
Why does this class exist?

Why is this method in the service?

Why does the repository own this operation?

Where should validation happen?

How does data move between these objects?

What happens if this operation fails?

How would the design change as the application grows?
```

The purpose of using AI in this project is to accelerate the learning process while still developing my own understanding of the code.

---

## What This Project Is Not

This repository is **not intended to demonstrate a production-ready e-commerce backend**.

It currently does not attempt to solve every concern required by a real commercial system.

Instead, it is a learning environment for understanding backend fundamentals before moving to higher-level tools and frameworks.

Some implementations may therefore be simplified intentionally.

---

## Future Learning Direction

As the project develops, I plan to gradually explore:

```text
Unit Testing
      ↓
JUnit / Mockito
      ↓
Better transaction management
      ↓
Database design improvements
      ↓
Dependency Injection concepts
      ↓
Spring Core
      ↓
Spring Boot
      ↓
Spring Data / JPA
      ↓
REST APIs
      ↓
Authentication & Authorization
```

The knowledge gained from implementing these concepts manually should make it easier to understand what Spring Boot and other frameworks are doing behind the scenes.

---

## Project Status

🚧 **Work in Progress — Learning Project**

The architecture, database design, naming conventions, and implementations may change as I learn new concepts and refactor previous decisions.

Those changes are part of the purpose of this repository.

---

## Author

**Hai Anh Phan**

Software Engineering Student
Backend Engineering Learner

This repository documents part of my journey from learning Java fundamentals toward building structured backend applications.
