# AGENTS.md

## Layout
- Single Maven module in `backend-turnoApp/`; git root is the parent directory. No commits yet.
- Base package `com.nicosgodoy.backend_turnoApp` — the underscore is intentional (artifact id `backend-turnoApp` is not a valid package name; see `HELP.md`). Keep it for all new classes.
- Entry point: `BackendTurnoAppApplication`. No controllers/entities exist yet.
- No datasource configured; the Postgres driver is on the classpath but unused until `spring.datasource.*` is added.

## Build & test (from WSL)
- There is no JDK inside WSL, so `./mvnw` fails. Use the Windows wrapper instead:
  - Full build + tests: `cmd.exe /c "mvnw.cmd -B test"`
  - Single test: `cmd.exe /c "mvnw.cmd -B -Dtest=BackendTurnoAppApplicationTests test"`
  - Run app: `cmd.exe /c "mvnw.cmd spring-boot:run"`
- From a Windows shell, `mvnw.cmd` works directly. JDK 21 is at `C:\Program Files\Java\jdk-21`.

## Framework quirks
- Spring Boot 4.1.2 / Java 21. Boot 4 renamed starters: use `spring-boot-starter-webmvc` (not `-web`) and `spring-boot-starter-webmvc-test` (not `-test`). Do not add Boot 3 starter names.
- No CI, lint, or formatter is configured; don't invent such commands.
- `target/` and `.idea/` are gitignored.
