package agents.multitool;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

/** Example ADK agent with weather and local-time tools. */
public class MultiToolAgent {

  private static final String USER_ID = "student";
  private static final String NAME = "multi_tool_agent";

  /** Root agent discovered by the ADK Dev UI. */
  public static final BaseAgent ROOT_AGENT = initAgent();

  private static final Map<String, String> CITY_TIMEZONE_ALIASES =
      Map.of(
          "san francisco", "America/Los_Angeles",
          "beijing", "Asia/Shanghai",
          "mumbai", "Asia/Kolkata");

  /** Builds the sample multi-tool agent. */
  public static BaseAgent initAgent() {
    return LlmAgent.builder()
        .name(NAME)
        .model("gemini-2.5-flash")
        .description("Agent to answer questions about the time and weather in a city.")
        .instruction(
            "You are a helpful agent who can answer user questions about the time and weather"
                + " in a city.")
        .tools(
            FunctionTool.create(MultiToolAgent.class, "getCurrentTime"),
            FunctionTool.create(MultiToolAgent.class, "getWeather"))
        .build();
  }

  /** Returns the current time in a supported city. */
  public static Map<String, String> getCurrentTime(
      @Schema(description = "The name of the city for which to retrieve the current time")
          String city) {
    if (city == null || city.isBlank()) {
      return Map.of("status", "error", "report", "City name cannot be empty.");
    }

    String trimmedCity = city.trim().toLowerCase();
    String targetZoneId = CITY_TIMEZONE_ALIASES.get(trimmedCity);

    if (targetZoneId == null) {
      String normalizedCity =
          Normalizer.normalize(city, Normalizer.Form.NFD)
              .trim()
              .toLowerCase()
              .replaceAll("(\\p{IsM}+|\\p{IsP}+)", "")
              .replaceAll("\\s+", "_");

      targetZoneId =
          ZoneId.getAvailableZoneIds().stream()
              .filter(zoneId -> zoneId.toLowerCase().endsWith("/" + normalizedCity))
              .findFirst()
              .orElse(null);
    }

    if (targetZoneId != null) {
      String currentTime =
          ZonedDateTime.now(ZoneId.of(targetZoneId))
              .format(DateTimeFormatter.ofPattern("HH:mm"));
      return Map.of(
          "status",
          "success",
          "report",
          "The current time in " + city + " is " + currentTime + ".");
    }

    return Map.of(
        "status",
        "error",
        "report",
        "Sorry, I don't have timezone information for " + city + ".");
  }

  /** Returns the sample weather report for New York. */
  public static Map<String, String> getWeather(
      @Schema(description = "The name of the city for which to retrieve the weather report")
          String city) {
    if (city != null && "new york".equalsIgnoreCase(city.trim())) {
      return Map.of(
          "status",
          "success",
          "report",
          "The weather in New York is sunny with a temperature of 25 degrees Celsius"
              + " (77 degrees Fahrenheit).");
    }

    String cityName = city != null ? city : "unknown";
    return Map.of(
        "status",
        "error",
        "report",
        "Weather information for " + cityName + " is not available.");
  }

  /** Runs the agent in an interactive terminal session. */
  public static void main(String[] args) throws Exception {
    InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
    try {
      Session session = runner.sessionService().createSession(NAME, USER_ID).blockingGet();

      try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
        while (true) {
          System.out.print("\nYou > ");
          if (!scanner.hasNextLine()) {
            break;
          }
          String userInput = scanner.nextLine();

          if ("quit".equalsIgnoreCase(userInput)) {
            break;
          }

          Content userMessage = Content.fromParts(Part.fromText(userInput));
          Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMessage);

          System.out.print("\nAgent > ");
          events.blockingForEach(event -> System.out.println(event.stringifyContent()));
        }
      }
    } finally {
      runner.close().blockingAwait();
    }
  }
}
