package week1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public class SlidingWindow {

    // 공백과 구두점을 기준으로 토큰 분리
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\s\\p{Punct}]+");

    private final int maxTokenLimit;
    private int currentTokenCount;

    private final Deque<String> messages;
    private final Deque<Integer> tokenCounts;

    public SlidingWindow(int maxTokenLimit) {
        this.maxTokenLimit = maxTokenLimit;
        this.currentTokenCount = 0;
        this.messages = new ArrayDeque<>();
        this.tokenCounts = new ArrayDeque<>();
    }

    /**
     * 텍스트를 공백과 구두점 기준으로 분리해 토큰 수를 반환하는 간이 인코더
     *
     * @param text 토큰 수를 계산할 텍스트
     * @return 토큰 수 (null 또는 빈 문자열이면 0)
     */
    private int countTokens(String text) {
        if (text == null || text.isBlank()) return 0;

        return TOKEN_PATTERN.split(text.trim()).length;
    }

    /**
     * 새 메시지를 슬라이딩 윈도우 큐에 추가
     *
     * 추가 후 총 토큰 수가 maxTokenLimit을 초과하면 오래된 메시지부터 제거한다.
     * 단일 메시지 자체가 한계를 초과하는 경우 추가하지 않는다.
     *
     * @param message 추가할 채팅 메시지
     */
    public void add(String message) {
        int tokenCount = countTokens(message);

        // 단일 메시지 자체가 한계를 초과하면 무한루프 방지를 위해 즉시 스킵
        if (tokenCount > maxTokenLimit) return;

        // 새 메시지를 추가했을 때 한계를 초과하는 동안 오래된 메시지를 앞에서 제거
        while (currentTokenCount + tokenCount > maxTokenLimit && !messages.isEmpty()) {
            currentTokenCount -= tokenCounts.pollFirst(); // 제거되는 메시지의 토큰 수만큼 차감
            messages.pollFirst();                         // 가장 오래된 메시지 제거
        }

        // 새 메시지를 큐의 뒤에 추가
        messages.addLast(message);
        tokenCounts.addLast(tokenCount);
        currentTokenCount += tokenCount;
    }

    /**
     * 현재 윈도우에 있는 메시지 목록을 반환
     *
     * @return 시간 순서로 정렬된 메시지 목록
     */
    public List<String> getWindow() {
        return new ArrayList<>(messages);
    }

    /**
     * @return 현재 윈도우의 총 토큰 수
     */
    public int getCurrentTokenCount() {
        return currentTokenCount;
    }

    /**
     * @return 현재 윈도우에 있는 메시지 수
     */
    public int size() {
        return messages.size();
    }

    public static void main(String[] args) {
        // 최대 20토큰짜리 컨텍스트 윈도우 생성
        SlidingWindow queue = new SlidingWindow(20);

        String[] chatLogs = {
            "안녕하세요 오늘 날씨가 좋네요",
            "네 맞아요 운동하기 좋은 날씨예요",
            "어떤 운동을 주로 하세요?",
            "저는 웨이트 트레이닝을 주로 해요",
            "그게 날씨랑 무슨 상관이죠? 헬스장은 어차피 실내 아닌가요?",
            "그래도 비가 오거나 하면 헬스장 가는 길이 힘들잖아요",
            "그렇긴 하네요"
        };

        for (String chatLog : chatLogs) {
            queue.add(chatLog);
            System.out.println("추가: \"" + chatLog + "\"");
            System.out.println("현재 토큰: " + queue.getCurrentTokenCount() + " / 20");
            System.out.println("윈도우: " + queue.getWindow());
            System.out.println("---");
        }
    }
}
