---
title: Build and Deploy Java AI Agents with Google ADK on Debian Trixie and Cloud Run
published: false
description: Set up Java and Google ADK on Debian 13, run an agent locally, test it with JUnit, and deploy it to Cloud Run.
tags: java, ai, googlecloud, debian
cover_image: https://raw.githubusercontent.com/xbill9/adk-hello-world-java/main/cover-image-debian.png
---

Debian 13 “Trixie” provides a clean, stable base for Java agent development. On a
workstation, virtual machine, or cloud instance, you can compile Java projects, run
local web servers, use the Google Agent Development Kit (ADK) Dev UI, and deploy an
agent to Google Cloud Run.

This guide builds a small ADK agent with time and weather tools. The complete project
is available in the
[sample repository](https://github.com/xbill9/adk-hello-world-java).

## 1. Prepare Debian Trixie

Start with a Debian 13 installation and a user account that can run `sudo`. Confirm
the operating-system version:

```bash
. /etc/os-release
printf '%s %s (%s)\n' "$NAME" "$VERSION_ID" "$VERSION_CODENAME"
```

## 2. Install the required tools

Update the package index and install the base development tools:

```bash
sudo apt-get update
sudo apt-get install -y curl git maven unzip zip
```

This project compiles with Java 25. One convenient way to install a matching JDK is
[SDKMAN!](https://sdkman.io/install/):

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk list java
```

Select an available Java 25 identifier from the list and install it (for example, `25-open`):

```bash
sdk install java 25-open
java --version
mvn --version
```

Both commands should report Java 25, and Maven must be version 3.6.3 or newer. The
build enforces this minimum Maven version.

### Install the Google Cloud CLI

Add Google's Debian package repository:

```bash
sudo apt-get install -y apt-transport-https ca-certificates gnupg
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg \
  | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" \
  | sudo tee /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update
sudo apt-get install -y google-cloud-cli
```

Verify the installation:

```bash
gcloud version
```

The Google Cloud CLI is required for Vertex AI authentication and Cloud Run
deployment. It is not required if you only run the agent locally with a Gemini API
key.

## 3. Clone and inspect the project

```bash
git clone https://github.com/xbill9/adk-hello-world-java
cd adk-hello-world-java
```

The agent lives at
`src/main/java/agents/multitool/MultiToolAgent.java`. Its public
`ROOT_AGENT` field lets the ADK Dev UI discover it:

```java
public static final BaseAgent ROOT_AGENT = initAgent();

public static BaseAgent initAgent() {
  return LlmAgent.builder()
      .name("multi_tool_agent")
      .model("gemini-2.5-flash")
      .description("Agent to answer questions about the time and weather in a city.")
      .tools(
          FunctionTool.create(MultiToolAgent.class, "getCurrentTime"),
          FunctionTool.create(MultiToolAgent.class, "getWeather"))
      .build();
}
```

The tools return structured maps with a `status` and a human-readable `report`. The
time tool uses IANA time zones and includes aliases for cities such as San Francisco,
Beijing, and Mumbai.

### Current ADK Java baseline

This sample uses **ADK for Java 1.7.0**, the latest release available from Maven
Central as of July 27, 2026. Both runtime dependencies use the same property so the
core library and Dev UI cannot drift to different versions:

```xml
<properties>
  <google-adk.version>1.7.0</google-adk.version>
</properties>

<dependency>
  <groupId>com.google.adk</groupId>
  <artifactId>google-adk</artifactId>
  <version>${google-adk.version}</version>
</dependency>
<dependency>
  <groupId>com.google.adk</groupId>
  <artifactId>google-adk-dev</artifactId>
  <version>${google-adk.version}</version>
</dependency>
```

Recent changes relevant to this project include:

- **Java 25 build support.** ADK 1.6 updated its Spring AI integration and build so it
  works with Java 25.
- **More reliable function-tool streaming.** ADK 1.6 aligned Gemini streaming
  function-call handling with the Python ADK, and 1.7 fixed reassembly of streamed
  function-call arguments.
- **Cleaner command-line shutdown.** ADK now uses daemon threads for its shared HTTP
  client, preventing idle HTTP workers from keeping the JVM alive after the CLI exits.
- **Safer loading and sessions.** Recent fixes restrict dynamic class loading and
  skill paths and prevent cross-user disclosure in `VertexAiSessionService`.
- **More accurate observability.** ADK 1.7 includes tool-related tokens in
  `gen_ai.usage.input_tokens`.
- **Gemini 3 flow compatibility.** ADK 1.7 can reorder forced function calls when a
  Gemini 3 model requires it. This sample remains on `gemini-2.5-flash` for a stable,
  broadly available tutorial baseline.

These are framework improvements; the sample does not reimplement them. Keeping the
ADK dependencies pinned to 1.7.0 is what brings them into the application.

## 4. Choose an authentication mode

Run the setup script:

```bash
./init.sh
```

It offers two modes:

- **Gemini API key** for local development. Create a key in
  [Google AI Studio](https://aistudio.google.com/apikey). The script stores it in
  `~/gemini.key` with user-only permissions.
- **Vertex AI** for local development and Cloud Run. Enter a Google Cloud project ID;
  the script then configures gcloud and Application Default Credentials (ADC).

The selected mode is stored in `~/.adk-hello-world-java-auth`. Run `./init.sh` again
whenever you want to switch modes. The launch scripts source `set_env.sh`
automatically, so you do not need to export the variables by hand.

For Vertex AI, your account and the Cloud Run service identity must have the required
Vertex AI permissions. Cloud Run uses its service identity at runtime rather than a
downloaded credential file.

## 5. Build, test, and lint

Compile the project and run its eight JUnit Jupiter tests:

```bash
make build
make test
```

The tests cover agent initialization, supported and unsupported cities, time-zone
aliases, and null input. Run Checkstyle separately:

```bash
make lint
```

The lint target fails when Google Java Style warnings are found, which makes it useful
in local development and continuous integration.

## 6. Run the command-line agent

```bash
./cli.sh
```

Example session:

```text
You > What is the current time in Tokyo?
Agent > The current time in Tokyo is 08:24.

You > What is the weather in New York?
Agent > The weather in New York is sunny with a temperature of 25 degrees Celsius
        (77 degrees Fahrenheit).

You > quit
```

## 7. Use the ADK Dev UI

Start the local server:

```bash
./devui.sh
```

Open [http://127.0.0.1:8080](http://127.0.0.1:8080) in your browser. ADK scans the Maven
output under `target/classes` and makes `multi_tool_agent` available in the UI.

![ADK Dev UI](https://miro.medium.com/v2/resize:fit:700/1*46AzhFROo3xJMZOda0HZaw.png)

The `web.sh` script is retained as an alias for `devui.sh`.

ADK 1.6 tightened WebSocket origin handling and warns when the Dev UI uses the `*`
CORS default. That default is convenient for local development but should not be
treated as an access-control mechanism for a public deployment.

## 8. Deploy to Cloud Run

Cloud Run deployment uses Vertex AI rather than copying a local API key into the
service. If you selected API-key mode, run `./init.sh` again and choose Vertex AI.

Then deploy:

```bash
./cloudrun.sh
```

The script runs one source deployment for the `adk-hello-world-java` service in
`us-central1`. Because the repository contains a Dockerfile, Cloud Run builds that
Dockerfile remotely; Docker does not need to be installed on the Debian system.

The container reads Cloud Run's `PORT` environment variable and starts the ADK web
server. When deployment completes, gcloud prints the service URL.

![Cloud Run Console](https://miro.medium.com/v2/resize:fit:700/1*jNyZKFeamxBcUCZWuuN4dA.png)

The deployment is private by default because the service includes the ADK Dev UI and
API. Configure authenticated callers with Cloud Run IAM. For a disposable public
demonstration, replace `--no-allow-unauthenticated` with `--allow-unauthenticated` in
`cloudrun.sh`; do not rely on CORS as access control.

## Project structure

```text
.
├── .dockerignore
├── .gcloudignore
├── Dockerfile
├── Makefile
├── cli.sh
├── cloudrun.sh
├── devui.sh
├── init.sh
├── pom.xml
├── set_env.sh
└── src
    ├── main/java/agents/multitool/MultiToolAgent.java
    └── test/java/agents/multitool/MultiToolAgentTest.java
```

## Summary

Debian Trixie provides everything needed to build and test a Java ADK agent locally.
This sample keeps local authentication explicit, verifies the tool logic with JUnit,
enforces Java style with Checkstyle, and uses Vertex AI service identity when deployed
to Cloud Run.

## Resources

- [ADK Java quickstart](https://adk.dev/get-started/java/)
- [Google ADK for Java](https://github.com/google/adk-java)
- [ADK for Java 1.7.0 release notes](https://github.com/google/adk-java/releases/tag/v1.7.0)
- [Deploy Cloud Run services from source](https://cloud.google.com/run/docs/deploying-source-code)
- [Configure Cloud Run service identity](https://cloud.google.com/run/docs/configuring/services/service-identity)
- [SDKMAN! installation](https://sdkman.io/install/)
- [Sample repository](https://github.com/xbill9/adk-hello-world-java)
