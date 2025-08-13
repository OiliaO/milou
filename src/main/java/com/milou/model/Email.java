package com.milou.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email_code", unique = true, nullable = false)
    private String emailCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public Email() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmailCode() { return emailCode; }
    public void setEmailCode(String emailCode) { this.emailCode = emailCode; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}