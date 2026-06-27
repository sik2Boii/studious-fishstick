package week1.mission2;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Anthropic Claude API를 호출하는 AI 엔진 구현체
 *
 * ANTHROPIC_API_KEY 환경변수가 설정되어 있어야 한다.
 */
public class ClaudeEngine implements AIEngine {

    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*+)\"");

    private final HttpClient httpClient;
    private final String apiKey;

    public ClaudeEngine() {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
    }

    @Override
    public String chat(String message) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[Claude 오류] ANTHROPIC_API_KEY 환경변수가 설정되어 있지 않습니다.";
        }

        String requestBody = String.format("""
                {
                  "model": "%s",
                  "max_tokens": 1024,
                  "messages": [{"role": "user", "content": "%s"}]
                }
                """, MODEL, message.replace("\"", "\\\""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CLAUDE_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body());

        } catch (Exception e) {
            return "[Claude 오류] " + e.getMessage();
        }
    }

    private String parseResponse(String json) {
        Matcher matcher = RESPONSE_PATTERN.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n");
        }
        return "[응답 파싱 실패]";
    }

    @Override
    public String getEngineName() {
        return "claude";
    }
}
