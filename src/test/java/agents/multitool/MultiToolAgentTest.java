package agents.multitool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.adk.agents.BaseAgent;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MultiToolAgentTest {

    @Test
    @DisplayName("Should initialize agent with correct name")
    void testInitAgent() {
        BaseAgent agent = MultiToolAgent.initAgent();
        assertNotNull(agent);
        assertEquals("multi_tool_agent", agent.name());
    }

    @Test
    @DisplayName("Should return current time for valid city (Tokyo)")
    void testGetCurrentTimeSuccess() {
        Map<String, String> result = MultiToolAgent.getCurrentTime("Tokyo");
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.get("report").contains("The current time in Tokyo is"));
    }

    @Test
    @DisplayName("Should return current time for city alias (San Francisco)")
    void testGetCurrentTimeAliasSuccess() {
        Map<String, String> result = MultiToolAgent.getCurrentTime("San Francisco");
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.get("report").contains("The current time in San Francisco is"));
    }

    @Test
    @DisplayName("Should return error status for unknown city timezone")
    void testGetCurrentTimeUnknownCity() {
        Map<String, String> result = MultiToolAgent.getCurrentTime("UnknownFakeCity123");
        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertTrue(result.get("report").contains("Sorry, I don't have timezone information for UnknownFakeCity123."));
    }

    @Test
    @DisplayName("Should safely handle null city for getCurrentTime")
    void testGetCurrentTimeNullCity() {
        Map<String, String> result = MultiToolAgent.getCurrentTime(null);
        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertEquals("City name cannot be empty.", result.get("report"));
    }

    @Test
    @DisplayName("Should return weather for New York")
    void testGetWeatherNewYork() {
        Map<String, String> result = MultiToolAgent.getWeather("New York");
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.get("report").contains("sunny with a temperature of 25 degrees Celsius"));
    }

    @Test
    @DisplayName("Should return error for unsupported weather city")
    void testGetWeatherUnsupportedCity() {
        Map<String, String> result = MultiToolAgent.getWeather("London");
        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertEquals("Weather information for London is not available.", result.get("report"));
    }

    @Test
    @DisplayName("Should safely handle null city for getWeather")
    void testGetWeatherNullCity() {
        Map<String, String> result = MultiToolAgent.getWeather(null);
        assertNotNull(result);
        assertEquals("error", result.get("status"));
        assertEquals("Weather information for unknown is not available.", result.get("report"));
    }
}
