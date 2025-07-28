# TechStore E-commerce Application

TechStore is a full-stack e-commerce shopping cart application built with Java Servlets, MySQL, and a modern HTML/CSS/JavaScript frontend. It provides a comprehensive simulation of a modern online shopping experience, emphasizing clean architecture, data persistence, and an intuitive user interface.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [Live Demo & Screenshots](#live-demo--screenshots)
5. [Getting Started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Database Setup](#database-setup-mysql)
   - [Project Setup in Eclipse](#project-setup-in-eclipse)
   - [Running the Application](#running-the-application)
6. [Project Structure](#project-structure)
7. [API Endpoints](#api-endpoints)
8. [Key Learnings & Troubleshooting](#key-learnings--troubleshooting)
9. [Future Enhancements](#future-enhancements)
10. [Contributing](#contributing)
11. [Contact](#contact)

## Introduction

The TechStore E-commerce Application simulates a robust web-based system designed for online shopping. It integrates a Java Servlet backend with MySQL for data persistence and a dynamic frontend built with HTML, CSS, and JavaScript.

## Features

- **User Authentication:** Secure registration and login functionalities.
- **Dynamic Product Catalog:** Categorized and detailed product listings.
- **Interactive Shopping Cart:** Add/remove items, update quantities, session-based cart.
- **Secure Checkout Process:** Validates stock, collects delivery details, processes orders transactionally.
- **Personalized Order History:** Users can view past orders and order details.
- **Search Functionality:** Search products and orders.
- **Responsive UI:** Optimized for desktop, tablet, and mobile devices.
- **Feedback System:** Non-intrusive toast messages for user actions.

## Technologies Used

### Frontend

- HTML5
- CSS3
- JavaScript (ES6+)

### Backend

- Java Servlets (javax.servlet-api:4.0.1)
- Java JDK 17 LTS
- Apache Tomcat 9.0.x
- Maven 3.8.x
- Gson 2.8.9

### Database

- MySQL 8.0.x
- MySQL Connector/J (JDBC) 8.0.33

## Live Demo & Screenshots

(Currently, this project is designed for local deployment.)

To experience the application, follow the [Getting Started](#getting-started) instructions to run it on your local machine.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 LTS
- Apache Tomcat 9.0.x
- MySQL Server 8.0.x
- MySQL Workbench (Optional, Recommended)
- Eclipse IDE for Enterprise Java and Web Developers
- Maven

### Database Setup (MySQL)

1. Start MySQL Server and open MySQL Workbench.
2. Disable Safe Update Mode if using Workbench.
3. Execute the SQL schema from `database/schema.sql` to set up the database and sample data.

### Project Setup in Eclipse

1. Import the Maven project into Eclipse.
2. Configure JDK 17 and Apache Tomcat in Eclipse.
3. Update Maven project dependencies and clean the project.

### Running the Application

1. Start Apache Tomcat server in Eclipse.
2. Access the application in your web browser at `http://localhost:8080/techstore-ecommerce/`.

## Project Structure

The project follows a standard Maven web application structure with clear separation of concerns (Model, DAO, Service, Servlet layers).

techstore-ecommerce/
├── src/
│ ├── main/
│ │ ├── java/
│ │ ├── webapp/
│ │ └── WEB-INF/
├── database/
├── .gitignore
└── pom.xml

## API Endpoints

The backend exposes RESTful API endpoints for various functionalities including product management, cart operations, user authentication, and checkout.

For detailed API documentation, refer to the project's API documentation or inspect the servlets under `src/main/java/com/techstore/servlet/`.

## Key Learnings & Troubleshooting

This section covers the key challenges encountered during development and their solutions, including Java EE migration, database interactions, and frontend-backend integration.

## Future Enhancements

Suggestions for future development and improvements to the application, including admin panel integration, payment gateway, advanced filtering, and more.

## Contributing

Contributions are welcome! Fork the repository, make your changes, and submit a pull request with your improvements.

## Contact

For questions or feedback, contact:

- Email: maubed27@gmail.com
- GitHub: [maubed27](https://github.com/maubed27)


