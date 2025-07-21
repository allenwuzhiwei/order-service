# 🛒 Order MicroService - E-commerce Backend Module

## Overview
The **Order Service** is a core microservice within our e-commerce platform.  
It is responsible for managing the complete lifecycle of order-related data, including orders, order items, payments, and shipment tracking.

It is built with **Spring Boot**, **Spring Data JPA**, and follows a clean architecture with a layered structure.  
The service integrates with other microservices via **Eureka** (service discovery) and **Spring Cloud Config**.

---

## Project Structure

- `config/`    
  ➤ Configuration classes including API response wrappers, Swagger config, Security settings, and global exception handling.

- `controller/`    
  ➤ RESTful API controllers for Order, OrderItem, OrderPayment, and OrderShipment modules.

- `dao/`    
  ➤ JPA repository interfaces for interacting with the database.

- `entity/`    
  ➤ Domain model classes that map to database tables.

- `service/`   and `service/impl/`    
  ➤ Interfaces and implementations of business logic for each module.

- `service/strategy/`   and `strategy/impl/`  
  ➤ Strategy pattern implementation for OrderShipment module, providing flexible handling of shipment status.

- `resources/`    
  ➤ Configuration files (`application.yml`) and static resources.

- `OrderServiceApplication.java`     
  ➤ The main entry point of the Spring Boot application.

- `pom.xml`    
  ➤ Maven configuration for dependencies and build settings.

---

## Implemented Features
### 1️⃣ Order Module

**Basic Functions**
- Create a new order
- Retrieve all orders
- View order by ID
- Update order details
- Delete order

**Extended Features**
- Retrieve all the orders of this user based on the user ID
- Multi-condition order filtering (e.g. status, time range, amount range)
- Pagination and sorting of orders
- Place order directly from product page, with inventory and payment service integration
- Place order from shopping cart, automatically syncing cart items and clearing the cart after success
- Support for multiple payment methods (e.g. `WeChat`, `PayNow`) via Factory Method Pattern
- Inventory validation and deduction before order confirmation
- Auto payment verification via `payment-service` before saving order


---

### 2️⃣ OrderItem Module
**Basic Functions**
- Add items to an order
- Retrieve all order items under a certain order based on the orderId
- Update order item details
- Delete order item

**Extended Features**
- Calculate the total amount of items in an order
- Bulk insertion of order items
- Bulk deletion of order items

---

### 3️⃣ OrderPayment Module
**Basic Functions**
- Create a payment record for an order
- Retrieve all payment records
- View payment by ID
- Update payment details
- Delete payment record

**Extended Features**
- Retrieve all payment records of this order based on the order ID
- Multi-condition payments filtering
- Get the total payment amount for an order

---

### 4️⃣ OrderShipment Module
Basic Functions
- Create a shipment record for an order
- Retrieve all shipment records
- View shipment by ID
- Update shipment status
- Delete shipment record

**Extended Features (**Strategy Pattern**)**
- Shipment status management using Strategy Pattern:

  - PendingShipmentStrategy: Handles shipments in PENDING status.

  - ShippedShipmentStrategy: Handles shipments in SHIPPED status.

  - DeliveredShipmentStrategy: Handles shipments in DELIVERED status.

      - Automatically apply the correct shipment strategy based on status.
      - Flexible design for future extension of shipment statuses.

---
## 👥 Collaborators 

| Name         | Role               | Description                                                                                          |
|--------------|--------------------|------------------------------------------------------------------------------------------------------|
| **Song Yinrui** | Backend Developer  | Responsible for core backend development, including Order, OrderItem, OrderPayment,  and OrderShipment modules.  Integrate the use of design patterns.  Implemented RESTful APIs, business logic, and API documentation/testing. |
| **Wu Zhiwei**   | System Architect    | Setting up the microservice architecture.    Creating the database tables for the Order microservice. |

---
## Author
- Song Yinrui
- National University of Singapore, Institute of Systems Science (NUS-ISS)
- Development Start: April 2025
---