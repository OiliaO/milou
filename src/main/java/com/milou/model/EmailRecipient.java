package com.milou.model;

import jakarta.persistence.*;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public EmailRecipient() {
    }

    public EmailRecipient(Email email, User recipient) {
        this.email = email;
        this.recipient = recipient;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}