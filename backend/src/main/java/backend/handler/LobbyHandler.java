package backend.handler;

import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LobbyHandler extends TextWebSocketHandler {
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,@NonNull TextMessage message) throws IOException {
        sendMessage(session, message);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);
        TextMessage message = new TextMessage("New client connected!");
        sendMessage(session, message);
    }

    public void sendMessage(WebSocketSession session, TextMessage message) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
            }
        }
    }
}
