package com.lxl.message.websocket;

import lombok.Data;
import org.yeauty.pojo.Session;

@Data
public class WsSession {

    private Session session;

    private Long userId;

    private Long roomId;

    public WsSession(Session session, Long userId, Long roomId) {
        this.session = session;
        this.userId = userId;
        this.roomId = roomId;
    }

    public WsSession(Session session, Long userId) {
        this.session = session;
        this.userId = userId;
    }

    public WsSession(Session session) {
        this.session = session;
    }

    @Override
    public int hashCode() {
        return session.channel().id().asLongText().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WsSession) {
            WsSession ws = (WsSession) obj;
            return ws.getSession().channel().id().asLongText().equals(session.channel().id().asLongText());
        }
        return false;
    }
}
