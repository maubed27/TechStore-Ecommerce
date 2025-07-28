🛒 TechStore E-commerce Application
A comprehensive full-stack e-commerce shopping cart application built with Java Servlets, MySQL, and a modern HTML/CSS/JavaScript frontend. This project demonstrates core e-commerce functionalities, database persistence, and a user-friendly interface.

📚 Table of Contents
Introduction

Features

Technologies Used

Live Demo & Screenshots

Getting Started

Prerequisites

Database Setup (MySQL)

Project Setup in Eclipse

Running the Application

Project Structure

API Endpoints

Key Learnings & Troubleshooting

Future Enhancements

Contributing

License

Contact

1. Introduction
The TechStore E-commerce Application is a robust web-based system designed to simulate a modern online shopping experience. It serves as a practical demonstration of integrating a Java Servlet backend with a MySQL database and a dynamic HTML/CSS/JavaScript frontend. The application emphasizes clean architecture, data persistence, and an intuitive user experience, covering essential e-commerce workflows from product browsing to order placement.

2. Features
User Authentication: Secure user registration and login.

User data (email, password, names) persisted in MySQL.

Dynamic Product Catalog:

Products fetched from MySQL database.

Categorized Display: Products grouped under relevant categories on the main product page.

Category Detail View: Clickable category headers lead to a dedicated view showing all products within that category.

Product Detail View: Clicking any product card displays its full details (image, name, description, price, stock) on a dedicated page.

Realistic Images: Products display actual images (hosted locally) for a more authentic feel.

Interactive Shopping Cart:

Add/remove items, update quantities.

Cart state maintained in user session.

Secure Checkout Process:

Validates stock availability.

Collects Delivery Address during checkout.

Processes orders transactionally, saving to database (orders and order_items tables).

Reduces product stock in database.

Personalized Order History:

Logged-in users can view their past orders, including detailed ordered items with images and quantities.

Search Functionality:

Product Search: Search products by name, description, or category on the main products page.

Order Search: Search past orders by Order ID or product name within "My Orders" section.

Responsive UI: Optimized for seamless experience across desktop, tablet, and mobile devices.

Feedback System: Non-intrusive toast messages for user actions (e.g., "Added to cart!").

Clean Architecture: Layered design (DAO, Service, Servlet) for maintainability and scalability.

3. Technologies Used
| Category | Technology / Library | Version | Description |
| Frontend | HTML5 | - | Semantic markup for web pages. |
|  | CSS3 | - | Styling and responsive design. |
|  | JavaScript (ES6+) | - | Dynamic interactions, API calls (fetch), client-side logic. |
| Backend | Java Servlets | javax.servlet-api:4.0.1 | Core API for building web components. |
|  | Java | JDK 17 LTS | Core programming language. |
|  | Apache Tomcat | 9.0.x | Servlet container and web server. |
|  | Maven | 3.8.x | Project automation and dependency management. |
|  | Gson | 2.8.9 | Java library for JSON serialization/deserialization. |
| Database | MySQL | 8.0.x | Relational database for persistent storage. |
|  | MySQL Connector/J (JDBC) | 8.0.33 | Java Database Connectivity driver for MySQL. |
| Other | .gitignore | - | Specifies untracked files to ignore. |

4. Live Demo & Screenshots
(Currently, this project is designed for local deployment.)

To experience the application, please follow the Getting Started instructions to run it on your local machine.

(You can replace this section with actual screenshots or a GIF of your running application once deployed, or a link to a live demo if you host it online.)

5. Getting Started
Follow these steps to set up and run the TechStore E-commerce Application on your local machine.

Prerequisites
Java Development Kit (JDK) 17 LTS:

Download from Adoptium (Eclipse Temurin).

Apache Tomcat 9.0.x:

Download the ZIP distribution from Apache Tomcat 9 Downloads.

MySQL Server 8.0.x:

Download MySQL Community Server from [suspicious link removed].

MySQL Workbench (Optional, Recommended): For easy database management.

Eclipse IDE for Enterprise Java and Web Developers:

Download from Eclipse Packages.

Maven: Usually bundled with Eclipse or installed separately.

Git Bash: For Git commands (if not using Eclipse's Git integration).

Database Setup (MySQL)
Start MySQL Server: Ensure your MySQL server service (e.g., MySQL80) is running.

Open MySQL Workbench or Command Line Client.

Disable Safe Update Mode (if using Workbench/Client):

In MySQL Workbench: Edit > Preferences > SQL Editor > Uncheck "Safe Updates". Reconnect to the server.

In Command Line Client: After connecting, execute SET SQL_SAFE_UPDATES = 0; for the current session.

Execute the Schema:

Connect to your MySQL server as root (or a user with DDL privileges).

Copy the entire SQL schema from database/schema.sql in this repository.

Paste and execute it in your MySQL client. This will create the techstore database, drop existing tables (if any), create new tables (users, products, orders, order_items), and insert sample data.

Verify DatabaseConnection.java:

Open src/main/java/com/techstore/util/DatabaseConnection.java in your Eclipse project.

Ensure USERNAME and PASSWORD match your MySQL root user credentials. The default is root and password. Update if different.

JDBC_URL should be jdbc:mysql://localhost:3306/techstore?useSSL=false&serverTimezone=UTC.

Project Setup in Eclipse
Launch Eclipse IDE: Choose a new, empty workspace (e.g., C:\Eclipse_Workspaces\techstore_project_workspace).

Configure JDK 17 in Eclipse:

Window > Preferences > Java > Installed JREs.

Click Add... > Standard VM > Next.

Directory... and browse to your JDK 17 installation folder (e.g., C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot). Click Finish.

Ensure JDK 17 is checked.

Go to Java > Compiler. Set "Compiler compliance level" to 1.8. Click Apply and Close.

Import Maven Project:

File > Import... > Maven > Existing Maven Projects.

Browse... and navigate to the root of your techstore-ecommerce project folder (where pom.xml is located).

Ensure techstore-ecommerce is checked. Click Finish.

Update Maven Project:

Right-click techstore-ecommerce project in Project Explorer > Maven > Update Project....

Ensure techstore-ecommerce is checked and "Force Update of Snapshots/Releases" is checked. Click OK.

Configure Project Facets:

Right-click techstore-ecommerce project > Properties > Project Facets.

Ensure "Dynamic Web Module" is checked and set to 4.0.

Ensure "Java" is checked and set to 1.8.

Click Apply. Then, click "Further configuration available..." link.

In the "Runtimes" tab, check your Apache Tomcat v9.0 Server. Click OK.

Click Apply and Close on the main Properties window.

Clean Project:

Project (top menu) > Clean... > Select techstore-ecommerce > Clean.

Add Tomcat Server in Eclipse:

Window > Show View > Servers.

Right-click in Servers view > New > Server.

Expand Apache > Select Tomcat v9.0 Server. Click Next.

For "Tomcat installation directory:", browse to your Tomcat 9.0.x folder (e.g., C:\apache-tomcat-9.0.107).

For "JRE:", explicitly select your configured jdk-17.0.15.6-hotspot. Click Next >.

On the "Add and Remove Deployments" screen, select techstore-ecommerce from "Available" and click Add >. Click Finish.

Running the Application
Start Tomcat Server:

In the Servers view, right-click on Tomcat v9.0 Server at localhost.

Select Start.

Monitor the Eclipse Console for "Server startup in [X] milliseconds" and no SEVERE errors.

Access in Browser:

Once Tomcat is [Started, Synchronized], open your web browser.

Go to: http://localhost:8080/techstore-ecommerce/

The application will initially prompt you to log in.

Default Users:

admin@techstore.com / admin123

user@test.com / password

6. Project Structure
The project follows a standard Maven web application structure with clear separation of concerns (Model, DAO, Service, Servlet layers).

techstore-ecommerce/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── techstore/
│   │   │           ├── dao/                 # Data Access Objects (DB interaction)
│   │   │           │   ├── OrderDAO.java
│   │   │           │   ├── ProductDAO.java
│   │   │           │   └── UserDAO.java
│   │   │           ├── filter/              # Servlet Filters (e.g., CORS)
│   │   │           │   └── CorsFilter.java
│   │   │           ├── model/               # Data Models (POJOs)
│   │   │           │   ├── CartItem.java
│   │   │           │   ├── Order.java
│   │   │           │   ├── OrderItem.java
│   │   │           │   ├── Product.java
│   │   │           │   └── User.java
│   │   │           ├── service/             # Business Logic Layer
│   │   │           │   ├── OrderService.java
│   │   │           │   ├── ProductService.java
│   │   │           │   └── UserService.java
│   │   │           ├── servlet/             # API Endpoints (Servlets)
│   │   │           │   ├── CartServlet.java
│   │   │           │   ├── CheckoutServlet.java
│   │   │           │   ├── ProductServlet.java
│   │   │           │   └── UserServlet.java
│   │   │           └── util/                # Utility classes (e.g., DB Connection)
│   │   │               ├── DatabaseConnection.java
│   │   │               └── TestDBConnection.java (for testing DB connection)
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml                  # Deployment Descriptor
│   │       ├── images/                      # Locally hosted product images
│   │       │   ├── bose_qc45.jpg
│   │       │   ├── dell_xps.png
│   │       │   └── ... (other product images)
│   │       └── index.html                   # Frontend SPA
├── database/                                # SQL schema for database setup
│   └── schema.sql
├── .gitignore                               # Files/folders to ignore in Git
└── pom.xml                                  # Maven Project Object Model



7. API Endpoints
The backend exposes RESTful API endpoints for various functionalities:

Product API (/api/products)
GET /api/products: Get all products.

GET /api/products/{id}: Get product by ID.

POST /api/products: Add new product (Admin only).

PUT /api/products/{id}: Update product (Admin only).

DELETE /api/products/{id}: Delete product (Admin only).

Cart API (/api/cart)
GET /api/cart: Get current cart contents.

POST /api/cart: Add item to cart.

Body: {"productId": 1, "quantity": 1}

PUT /api/cart: Update item quantity in cart.

Body: {"productId": 1, "quantity": 5}

DELETE /api/cart/{productId}: Remove specific item from cart.

DELETE /api/cart: Clear entire cart.

User API (/api/user)
POST /api/user/login: User login.

Body: {"email": "user@test.com", "password": "password"}

POST /api/user/register: User registration.

Body: {"firstName": "John", "lastName": "Doe", "email": "john@example.com", "password": "securepassword"}

POST /api/user/logout: User logout.

GET /api/user/status: Check current login status.

GET /api/user/orders: Get logged-in user's order history.

GET /api/user/searchorders?searchTerm={term}: Search user's orders by ID or product name.

Checkout API (/api/checkout)
POST /api/checkout: Process the checkout.

Body: {"deliveryAddress": "123 Main St, Anytown, State, 12345"} (Frontend prompts for this)

8. Key Learnings & Troubleshooting
This project involved navigating several common and complex challenges in Java web development, providing valuable learning experiences:

Java EE vs. Jakarta EE Migration: Initial ClassCastException errors due to javax.servlet vs. jakarta.servlet namespace conflicts when using JDK 9+ and Tomcat 9.

Solution: Updated pom.xml to javax.servlet-api (Servlet 4.0 / Java EE 8) and ensured all Java imports explicitly used javax.servlet.*. Corrected Eclipse Project Facets to align with Dynamic Web Module 4.0.

Java Module System (JPMS) Reflection Issues: InaccessibleObjectException when Gson tried to reflectively access java.time.LocalDateTime fields in JDK 9+.

Solution: Implemented and registered a custom Gson LocalDateTimeAdapter in init() methods of all servlets that serialize/deserialize objects containing LocalDateTime fields (e.g., Product, Order).

Session Object Serialization: Cannot deserialize session attribute warnings/errors.

Solution: Ensured all custom model classes (Product, CartItem, User, Order, OrderItem) implemented java.io.Serializable.

MySQL Safe Update Mode (Error Code 1175): Preventing DELETE without WHERE clause.

Solution: Temporarily disabled SQL_SAFE_UPDATES via SET SQL_SAFE_UPDATES = 0; in the MySQL client session before executing DELETE FROM products;.

Image Hotlinking Prevention: External product images not loading.

Solution: Downloaded product images and hosted them locally within src/main/webapp/images/, updating MySQL database paths to point to these local resources.

Debugging Empty/Malformed JSON Responses: Persistent Unexpected end of JSON input errors.

Solution: Employed extensive java.util.logging.Logger statements in servlets and DAOs to trace execution flow and identify exact points of failure. Used browser Developer Tools' Network tab to inspect raw server responses. Ensured PrintWriter was correctly used and flushed, and that sendErrorResponse only sent valid JSON.

Eclipse Project Management: Frequent Maven > Update Project... (with Force Update), Project > Clean..., and Tomcat server restarts were essential for synchronizing changes and resolving build path issues.

9. Future Enhancements
This project provides a solid foundation. Here are some ideas for further development:

Admin Panel: Create a dedicated web interface for administrators to manage products (add, edit, delete), view all orders, and manage users.

Payment Gateway Integration: Integrate with a real (or simulated) payment gateway (e.g., Stripe, PayPal) for actual transaction processing.

Advanced Product Filtering: Implement more complex filters (e.g., by price range, stock availability, brand) and sorting options.

Search Optimization: Implement full-text search capabilities in the database for products and orders.

User Profiles: Allow users to update their personal information and manage multiple delivery addresses.

Order Tracking: Add functionality for users to track the status of their orders.

Shopping Cart Persistence: Beyond session, persist the cart in the database for logged-in users so it persists across different devices or sessions.

Password Security: Implement robust password hashing (e.g., BCrypt) for user passwords.

Unit & Integration Testing: Write comprehensive JUnit tests for DAO and Service layers.

Deployment: Explore deploying to a cloud platform (e.g., AWS, Google Cloud) for a live, publicly accessible application.

Frontend Framework: Migrate the frontend to a modern JavaScript framework (e.g., React, Vue, Angular) for a more scalable and component-based UI.

10. Contributing
Contributions are welcome! If you have suggestions for improvements or new features, feel free to fork the repository, make your changes, and submit a pull request.

11. Contact
For any questions or feedback, please contact:

Email: maubed27@gmail.com

GitHub Profile: https://github.com/maubed27

Thank you for exploring the TechStore E-commerce Application!