package com.example.virtualassistant;

public class Message {
    public static String SENT_BY_ME="me" ;
    public static String SENT_BY_BOT="bot" ;
    String message ;
    String sentBy ;

    public String getMessage() {
        return message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}
