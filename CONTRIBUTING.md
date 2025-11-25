# Contributing to ModbusParser

Thank you for your interest in contributing to ModbusParser! We welcome contributions from the community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please be respectful and constructive in your interactions.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/ModbusParser.git`
3. Add upstream remote: `git remote add upstream https://github.com/ORIGINAL/ModbusParser.git`

## Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Building the Project

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Install to local Maven repository
mvn install
```

## How to Contribute

### Reporting Bugs

- Check if the bug has already been reported in Issues
- If not, create a new issue with:
  - Clear, descriptive title
  - Detailed description of the problem
  - Steps to reproduce
  - Expected vs actual behavior
  - Java version, OS, and relevant environment details
  - Code samples if applicable

### Suggesting Enhancements

- Check existing issues and discussions
- Create a new issue describing:
  - Use case and motivation
  - Proposed solution
  - Alternative approaches considered
  - Potential impact on existing functionality

### Pull Requests

1. Create a feature branch: `git checkout -b feature/your-feature-name`
2. Make your changes
3. Write or update tests
4. Ensure all tests pass: `mvn test`
5. Commit with clear, descriptive messages
6. Push to your fork
7. Create a Pull Request

## Coding Standards

### Java Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Keep methods focused and concise (< 50 lines ideally)
- Add JavaDoc for all public APIs
- Use final for immutable fields
- Prefer composition over inheritance

### Code Quality

- **No warnings**: Code should compile without warnings
- **Thread-safety**: Document thread-safety guarantees
- **Null-safety**: Use `Optional<T>` and `Objects.requireNonNull()`
- **Exception handling**: Provide context in exceptions
- **Resource management**: Use try-with-resources for AutoCloseable

### Documentation

- Add JavaDoc for:
  - All public classes
  - All public methods
  - Complex private methods
- Include:
  - Description of purpose
  - Parameter descriptions
  - Return value description
  - Exceptions thrown
  - Usage examples for complex APIs
  - Thread-safety guarantees

### Commit Messages

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, etc.

Example:
```
feat(connection): add connection pooling support

Implemented ModbusConnectionPool with configurable size,
timeouts, and connection validation.

Closes #123
```

## Testing

### Unit Tests

- Write tests for all new functionality
- Aim for high code coverage (> 80%)
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)
- Use AssertJ for fluent assertions

### Integration Tests

- Add integration tests in `src/test/java/.../integration/`
- Mark with `@Disabled` if requiring hardware
- Document hardware requirements

### Running Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=ClassName

# With coverage
mvn clean test jacoco:report
```

## Submitting Changes

### Before Submitting

- [ ] Code compiles without errors or warnings
- [ ] All tests pass
- [ ] New functionality has tests
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Code follows style guidelines
- [ ] Commits follow conventional format

### Pull Request Process

1. Update README.md if adding features
2. Update CHANGELOG.md with your changes
3. Ensure CI passes (if configured)
4. Request review from maintainers
5. Address review feedback
6. Squash commits if requested
7. Wait for approval and merge

### Review Process

- Maintainers will review within 1 week
- Be responsive to feedback
- Be patient and respectful
- Multiple iterations may be needed

## Security Issues

**Do NOT create public issues for security vulnerabilities.**

Instead, email security concerns to: [weber.anoder@protonmail.com]

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

Feel free to ask questions by:
- Creating a Discussion
- Commenting on relevant Issues
- Reaching out to maintainers

Thank you for contributing! 🎉
