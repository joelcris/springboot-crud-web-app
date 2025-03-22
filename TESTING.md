# Testing Documentation

This document provides comprehensive information about the testing approach used in the Spring Boot CRUD Web Application.

## Table of Contents

1. [Testing Overview](#testing-overview)
2. [Test Types](#test-types)
3. [Test Structure](#test-structure)
4. [Running Tests](#running-tests)
5. [Test Layers](#test-layers)
   - [Repository Tests](#repository-tests)
   - [Service Tests](#service-tests) 
   - [Controller Tests](#controller-tests)
   - [Exception Handling Tests](#exception-handling-tests)
6. [Best Practices](#best-practices)

## Testing Overview

The application follows a comprehensive testing strategy that includes multiple layers of testing to ensure code quality, reliability, and correctness. The tests are organized according to the application's architecture:

- **Repository Layer**: Tests for data access and persistence operations
- **Service Layer**: Tests for business logic and service operations
- **Controller Layer**: Tests for API endpoints and request/response handling
- **Exception Handling**: Tests for global exception handling and error responses

## Test Types

The application uses two main types of tests:

### 1. Unit Tests

Isolated tests that verify individual components of the application in isolation, using mocks or stubs for dependencies:

- Service layer tests with mocked repositories
- Exception handling tests with mocked request objects
- Response object tests

### 2. Integration Tests

Tests that verify the interaction between multiple components or layers of the application:

- Repository tests using an in-memory H2 database
- Controller integration tests with MockMvc for simulated HTTP requests

## Test Structure

Tests follow a consistent structure using the Arrange-Act-Assert (AAA) pattern:

1. **Arrange**: Set up test data and preconditions
2. **Act**: Execute the code under test
3. **Assert**: Verify the results match expectations

Example:

```java
@Test
void findById_WhenProductExists_ShouldReturnProduct() {
    // Arrange
    Product product = new Product();
    product.setName("Test Product");
    product.setDescription("Test Description");
    product.setPrice(BigDecimal.valueOf(99.99));
    product.setStock(10);
    
    entityManager.persist(product);
    entityManager.flush();
    
    // Act
    Optional<Product> found = productRepository.findById(product.getId());
    
    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Test Product");
    assertThat(found.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    assertThat(found.get().getStock()).isEqualTo(10);
}
```

## Running Tests

You can run tests using Gradle with the following commands:

### Run All Tests

```bash
./gradlew test
```

### Run Tests for a Specific Package

```bash
./gradlew test --tests "com.webapp.springboot_crud_web_app.repository.*"
./gradlew test --tests "com.webapp.springboot_crud_web_app.service.*"
./gradlew test --tests "com.webapp.springboot_crud_web_app.controller.*"
./gradlew test --tests "com.webapp.springboot_crud_web_app.exception.*"
```

### Run a Specific Test Class

```bash
./gradlew test --tests "com.webapp.springboot_crud_web_app.repository.ProductRepositoryTest"
```

### Run a Specific Test Method

```bash
./gradlew test --tests "com.webapp.springboot_crud_web_app.repository.ProductRepositoryTest.findById_WhenProductExists_ShouldReturnProduct"
```

## Test Layers

### Repository Tests

Repository tests verify the data access layer and ORM functionality. These tests use `@DataJpaTest` which:

- Configures an in-memory H2 database
- Sets up Hibernate, Spring Data, and the EntityManager
- Enables transaction management for test isolation

Key features:

- Use `TestEntityManager` for test data setup
- Test basic CRUD operations
- Verify entity relationship behaviors (cascade operations)
- Test edge cases like invalid relationships

Example repository test classes:
- `ProductRepositoryTest`
- `OrderRepositoryTest`
- `OrderItemRepositoryTest`

### Service Tests

Service tests verify the business logic layer. These tests use `MockitoExtension` to mock dependencies:

- Mock repositories to isolate service logic
- Test business rules and validations
- Verify correct handling of success and failure cases

Key features:

- Use Mockito to mock repository behavior
- Test exception handling and business rules
- Verify service interactions with dependencies

Example service test classes:
- `ProductServiceTest`
- `OrderServiceTest`
- `OrderItemServiceTest`

### Controller Tests

Controller tests verify the API endpoints and request/response handling. These tests use:

- `@SpringBootTest` with `@AutoConfigureMockMvc` for integration testing
- Mocked services for unit testing

Key features:

- Use MockMvc to simulate HTTP requests
- Test request validation
- Verify response status codes and content
- Test edge cases and error handling

Example controller test classes:
- `ProductControllerIntegrationTest`
- `OrderControllerIntegrationTest`
- `OrderItemControllerIntegrationTest`

### Exception Handling Tests

Exception handling tests verify the global exception handling mechanism. These tests focus on:

- Exception translation to appropriate HTTP responses
- Consistent error message formatting
- Proper validation error handling

Key features:

- Mock exceptions and web requests
- Test error response structures
- Verify correct HTTP status codes

Example exception handling test classes:
- `GlobalExceptionHandlerTest`
- `ErrorResponseTest`
- `ValidationErrorResponseTest`
- `ResourceNotFoundExceptionTest`
- `BusinessRuleViolationExceptionTest`

## Best Practices

The testing approach follows these best practices:

1. **Test Naming**: Clear and descriptive test names using the pattern `methodName_scenario_expectedBehavior`

2. **Test Independence**: Each test is independent and doesn't rely on the state from other tests

3. **Test Coverage**: Tests cover:
   - Happy paths (successful operations)
   - Edge cases (boundary conditions)
   - Error paths (exception handling)

4. **Use of Assertions**:
   - JUnit Jupiter assertions for basic assertions
   - AssertJ for more expressive and readable assertions

5. **Test Data Management**:
   - Setup and teardown of test data
   - Use of test profiles and in-memory databases for isolation

6. **Test Organization**:
   - Tests follow the same package structure as the application code
   - Test classes have the same name as the class they test with "Test" suffix

7. **Mocking Strategy**:
   - Use mocks for dependencies to isolate the unit under test
   - Use real implementations for the component being tested 