# Agent Instructions

Use the upstream
[ADK for Java Gemini Code Assistant context](https://github.com/google/adk-java/blob/main/GEMINI.md)
as the primary reference for ADK architecture, development workflow, and Java coding
conventions.

For this sample repository:

- Build and test with Maven: `mvn test`.
- Check Google Java Style compliance with `mvn checkstyle:check`.
- Keep agent tools and the public static `ROOT_AGENT` field in
  `src/main/java/agents/multitool/MultiToolAgent.java`.
- Preserve both local authentication modes implemented by `init.sh` and `set_env.sh`:
  Gemini API key and Vertex AI with Application Default Credentials.
- Use Vertex AI authentication for Cloud Run deployments.
