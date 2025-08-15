package com.milou.service;

import com.milou.framework.SingletonSessionFactory;
import com.milou.model.Email;
import com.milou.model.EmailRecipient;
import com.milou.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EmailService {

    public String sendEmail(User sender, List<User> recipients, String subject, String body) {
        String code = generateUniqueCode();

        Email email = new Email();
        email.setSender(sender);
        email.setRecipients(recipients);
        email.setEmailCode(code);
        email.setSubject(subject);
        email.setBody(body);
        email.setSentAt(LocalDateTime.now());

        SingletonSessionFactory.get().inTransaction(session -> {
            session.persist(email);

            for (User recipientUser : recipients) {
                EmailRecipient link = new EmailRecipient(email, recipientUser);
                session.persist(link);
            }
        });
        return code;
    }

    public List<Email> getReceivedEmails(User user) {
        return SingletonSessionFactory.get().fromTransaction(session -> {
            List<Email> emails = session.createNativeQuery(
                            "SELECT e.* FROM emails e " +
                                    "JOIN email_recipients er ON e.id = er.email_id " +
                                    "WHERE er.recipient_id = :userId ORDER BY e.sent_at DESC", Email.class)
                    .setParameter("userId", user.getId())
                    .getResultList();

            if (emails.isEmpty()) {
                return emails;
            }


            session.createNativeQuery("SELECT * FROM users WHERE id IN (:ids)", User.class)
                    .setParameter("ids", emails.stream().map(e -> e.getSender().getId()).collect(Collectors.toList()))
                    .getResultList();

            return emails;
        });
    }

    public List<Email> getUnreadEmails(User user) {
        return SingletonSessionFactory.get().fromTransaction(session -> {
            List<Email> emails = session.createNativeQuery(
                            "SELECT e.* FROM emails e " +
                                    "JOIN email_recipients er ON e.id = er.email_id " +
                                    "WHERE er.recipient_id = :userId AND er.is_read = false ORDER BY e.sent_at DESC", Email.class)
                    .setParameter("userId", user.getId())
                    .getResultList();

            if (emails.isEmpty()) {
                return emails;
            }

            session.createNativeQuery("SELECT * FROM users WHERE id IN (:ids)", User.class)
                    .setParameter("ids", emails.stream().map(e -> e.getSender().getId()).collect(Collectors.toList()))
                    .getResultList();

            return emails;
        });
    }


    public List<Email> getSentEmails(User user) {
        List<Email> sentEmails = SingletonSessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("SELECT * FROM emails WHERE sender_id = :senderId ORDER BY sent_at DESC", Email.class)
                        .setParameter("senderId", user.getId())
                        .getResultList()
        );

        sentEmails.forEach(email -> {
            List<User> recipients = getRecipientsForEmail(email.getId());
            email.setRecipients(recipients);
        });

        return sentEmails;
    }

    public Optional<Email> readEmailByCode(String code, User currentUser) {
        Optional<Email> emailOpt = findByCode(code);
        if (emailOpt.isEmpty()) {
            return Optional.empty();
        }

        Email email = emailOpt.get();
        List<User> recipients = getRecipientsForEmail(email.getId());

        boolean isSender = email.getSender().getId().equals(currentUser.getId());
        boolean isRecipient = recipients.stream().anyMatch(r -> r.getId().equals(currentUser.getId()));

        if (!isSender && !isRecipient) {
            return Optional.empty();
        }

        if (isRecipient) {
            markAsRead(email.getId(), currentUser.getId());
        }

        email.setRecipients(recipients);
        return Optional.of(email);
    }


    private Optional<Email> findByCode(String code) {
        return SingletonSessionFactory.get().fromTransaction(session -> {
            Email email = session.createNativeQuery("SELECT * FROM emails WHERE email_code = :code", Email.class)
                    .setParameter("code", code)
                    .getSingleResultOrNull();

            if (email != null) {
                email.getSender().getName();
            }

            return Optional.ofNullable(email);
        });
    }

    private void markAsRead(int emailId, int userId) {
        SingletonSessionFactory.get().inTransaction(session ->
                session.createNativeMutationQuery("UPDATE email_recipients SET is_read = true WHERE email_id = :emailId AND recipient_id = :userId")
                        .setParameter("emailId", emailId)
                        .setParameter("userId", userId)
                        .executeUpdate()
        );
    }

    private List<User> getRecipientsForEmail(int emailId) {
        return SingletonSessionFactory.get().fromTransaction(session ->
                session.createNativeQuery(
                                "SELECT u.* FROM users u " +
                                        "JOIN email_recipients er ON u.id = er.recipient_id " +
                                        "WHERE er.email_id = :emailId", User.class)
                        .setParameter("emailId", emailId)
                        .getResultList()
        );
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
        } while (findByCode(code).isPresent());
        return code;
    }

    public String replyToEmail(Email originalEmail, User replyingUser, String replyBody) {
        String newSubject = originalEmail.getSubject();
        if (!newSubject.toLowerCase().startsWith("[re]")) {
            newSubject = "[Re] " + newSubject;
        }

        List<User> newRecipients = new ArrayList<>();
        newRecipients.add(originalEmail.getSender());
        for (User recipient : originalEmail.getRecipients()) {
            if (!recipient.getId().equals(replyingUser.getId())) {
                newRecipients.add(recipient);
            }
        }
        List<User> finalRecipients = newRecipients.stream().distinct().collect(Collectors.toList());

        return sendEmail(replyingUser, finalRecipients, newSubject, replyBody);
    }

    public String forwardEmail(Email originalEmail, User forwardingUser, List<User> newRecipients) {
        String newSubject = originalEmail.getSubject();
        if (!newSubject.toLowerCase().startsWith("[fw]")) {
            newSubject = "[Fw] " + newSubject;
        }

        String body = originalEmail.getBody();

        return sendEmail(forwardingUser, newRecipients, newSubject, body);
    }
}