package com.lapluma.knowledg.model;

import java.io.Serializable;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable{
    private long id;
    private String date;
    private String content;
    private boolean fromMe;
    private boolean showTime = true;

    public ChatMessage(long id, String content, boolean fromMe, String date) {
        this.id = id;
        this.date = date;
        this.content = content;
        this.fromMe = fromMe;
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public boolean isShowTime() {
        return showTime;
    }
}
