# ADK Hello World for Java

A small Google Agent Development Kit (ADK) project with command-line and Dev UI
launchers, JUnit 6 tests, and a Cloud Run deployment script.

## Requirements

- Java 25
- Maven 3.6.3 or newer
- A Gemini API key, or Google Cloud Application Default Credentials for Vertex AI
- The Google Cloud CLI when using Vertex AI or deploying to Cloud Run

## Quick start

```bash
./init.sh
make test
./cli.sh
```

Run the local ADK Dev UI:

```bash
./devui.sh
```

Then open <http://127.0.0.1:8080>.

Cloud Run deployment uses Vertex AI and is private by default:

```bash
./cloudrun.sh
```

See the full
[Debian Trixie setup and deployment guide](Debian_Trixie_Java_ADK_Starter.md).

## References

- [ADK Java quickstart](https://adk.dev/get-started/java/)
- [google/adk-java](https://github.com/google/adk-java)
