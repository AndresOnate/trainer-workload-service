# Spring Training Management System

This project is a training management system that allows the administration of `Trainer`, `Trainee`, and `Training` profiles. The system is developed using the Spring framework and follows best practices in design and implementation.

## Key Features

1. **Services**:
   - **Trainee Service**: Supports creating, updating, deleting, and selecting trainee profiles.
   - **Trainer Service**: Supports creating, updating, and selecting trainer profiles.
   - **Training Service**: Supports creating and selecting training profiles.

2. **In-Memory Storage**:
   - Data is stored in an in-memory `java.util.Map`, with a separate namespace for each entity (`Trainee`, `Trainer`, `Training`).
   - The storage is initialized with prepared data from a file during application startup.

3. **Dependency Injection**:
   - Service beans are injected using constructor-based injection and field-based injection.
   - The remaining injections are done in a setter-based manner.

4. **Credential Generation**:
   - The `username` is generated from the `Trainer` or `Trainee`'s first name and last name, concatenated with a dot (e.g., `John.Smith`).
   - If a profile with the same first and last name already exists, a serial number is appended as a suffix to the username.
   - The password is generated as a random 10-character string.

5. **Logging and Unit Testing**:
   - The code is covered with unit tests.
   - Proper logging is implemented to facilitate debugging and tracking of operations.

## Project Setup

### Prerequisites

- Java 17 or higher.
- Maven 3.x.
- Spring Framework 5.x.

### Configuration File

The project uses an external properties file to configure the path for the data initialization file. Create an `application.properties` file in the root of the project with the following property:
