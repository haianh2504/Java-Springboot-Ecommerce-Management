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
- [x] Add repository/integration tests
- [x] Add controller tests with MockMvc
- [x] Move transaction boundaries to Spring `@Transactional`
- [x] Complete Spring MVC controllers
---

## Project Status in 2026 ( The project has been stopped )

🚧 **Work in Progress — Spring Boot Learning Project**

This repository has evolved from a manually implemented Java/JDBC backend into a Spring Boot application.

The architecture, persistence strategy, tests, package structure, and security design may continue to change as I learn and refactor previous implementations.

Those changes are an intentional part of the project.

**The Weakness & The realization**

This Project was built for personally learning Spring syntax and backend architecture, and the workflow between backend layers only, so:

1. I have met complexity and complications in migrating it from JDBC to Spring Data JPA ( almost had to change everything ), and the domains/entities do not even have enough information for a CRUD project. 

2. The idea of connecting this backend to a frontend to make it full-stack becomes a mess without an API contract built first. 

3. The migration from Java core to SpringBoot is a huge jump, so I learned it with AI assistance, and the knowledge is still unstable.
   
**Acknowledgement & Achievements**

---

## Author

**Hai Anh Phan**

Software Engineering Student  
Backend Engineering Learner

This repository documents my progression from Java backend fundamentals toward building structured Spring Boot applications.
