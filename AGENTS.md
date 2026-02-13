# AGENTS.md

# 지시 사항

- 모든 대화와 보고, 코드 설명은 **한국어(Korean)**로 진행한다.
- **무슨 일이 있어도 기존 코드를 함부로 수정하지 않는다.** 코드를 커맨드에 띄우지 않고 파일에 작성해야만 하는 상황이 온다면 새로운 파일을 파서 전체를 다 만들든가, 해당 파일 아래 주석으로 따로 작성해서 사용자가 결정할 수 있도록 한다.

# 본문

Welcome to the PorTrackerBE repository. This document provides essential information for AI agents and developers working on this Spring Boot project.

## 🛠 Build, Lint, and Test Commands

The project uses **Gradle** as the build tool.

| Action                     | Command                                                                                          |
| :------------------------- | :----------------------------------------------------------------------------------------------- |
| **Build Project**          | `./gradlew build`                                                                                |
| **Clean Build**            | `./gradlew clean build`                                                                          |
| **Run Application**        | `./gradlew bootRun`                                                                              |
| **Run All Tests**          | `./gradlew test`                                                                                 |
| **Run Single Test Class**  | `./gradlew test --tests "com.PorTracker.PorTrackerBE.PorTrackerBeApplicationTests"`              |
| **Run Single Test Method** | `./gradlew test --tests "com.PorTracker.PorTrackerBE.PorTrackerBeApplicationTests.contextLoads"` |
| **Check Dependencies**     | `./gradlew dependencies`                                                                         |

_Note: Use `gradlew.bat` instead of `./gradlew` on Windows if not using a POSIX-compliant shell._

---

## 🎨 Code Style Guidelines

### 1. Naming Conventions

- **Classes:** `PascalCase` (e.g., `GoogleSheetService`, `TestController`).
- **Methods & Variables:** `camelCase` (e.g., `getSheetData`, `spreadsheetId`).
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `APPLICATION_NAME`).
- **Packages:** Currently uses `PascalCase` (e.g., `com.PorTracker.PorTrackerBE`). While not standard for Java, **follow this existing pattern** for consistency unless a refactor is explicitly requested.
- **Files:** Must match the public class name (e.g., `GoogleSheetService.java`).

### 2. Imports

- Use **explicit, non-wildcard imports**.
- Group imports: Standard libraries (`java.*`), then third-party libraries (`com.google.*`, `org.springframework.*`), then project-specific classes.
- Avoid unused imports.

### 3. Error Handling

- **Checked Exceptions:** Services and controllers currently propagate checked exceptions (e.g., `throws IOException, GeneralSecurityException`).
- **Generic Exceptions:** Avoid `throws Exception` in production code; prefer specific exceptions.
- **Pattern:** Errors should be bubbled up to a global handler (e.g., `@ControllerAdvice`) or handled with clear logging.

### 4. Types and Immutability

- **Strictness:** Prefer strong typing (e.g., `List<String>`) over raw types (`List`) where possible.
- **Immutability:** Use `final` for class fields and local variables that do not change.
- **Dependency Injection:** Use **Constructor Injection** via Lombok's `@RequiredArgsConstructor`. Annotate classes with `@RestController` or `@Service`.

### 5. Testing Patterns

- Use **JUnit 5** and **Spring Boot Test**.
- Test classes should be located in `src/test/java/com/PorTracker/PorTrackerBE/`.
- Aim for descriptive test names (e.g., `shouldReturnSheetDataWhenValidIdProvided`).

### 6. Lombok Usage

- Use `@Getter`, `@Setter`, `@ToString`, and `@RequiredArgsConstructor` to reduce boilerplate.
- Ensure Lombok is correctly configured in your IDE/agent environment.

---

## 🏗 Project Architecture

- **Controllers:** Handle HTTP requests and map them to service calls (`TestController`).
- **Services:** Contain business logic and external integrations (`GoogleSheetService`).
- **Resources:** Configuration files (`application.properties`) and keys (`google-key.json`) are located in `src/main/resources`.

## 🛡 Security & Environment

- Do NOT commit secrets (like `google-key.json`) if they are sensitive.
- Use `.env` or environment variables for sensitive configuration where applicable.

---

_This file is generated for agentic use. Maintain these standards to ensure codebase consistency._
