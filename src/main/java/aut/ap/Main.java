package aut.ap;

import com.milou.framework.SingletonSessionFactory;
import com.milou.model.Email;
import com.milou.model.User;
import com.milou.service.EmailService;
import com.milou.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService();
    private static final EmailService emailService = new EmailService();
    private static User currentUser = null;

    public static void main(String[] args) {
        SingletonSessionFactory.get();
        System.out.println("Welcome to Milou Email Service!");

        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                showUserDashboard();
            }
        }
    }

    private static void showMainMenu() {
        System.out.print("\n[L]ogin, [S]ign up, [E]xit: ");
        String choice = scanner.nextLine().toLowerCase();

        switch (choice) {
            case "l":
            case "login":
                handleLogin();
                break;
            case "s":
            case "sign up":
                handleSignUp();
                break;
            case "e":
            case "exit":
                System.out.println("Goodbye!");
                SingletonSessionFactory.close();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    private static void showUserDashboard() {
        System.out.println("\n--------------------");
        System.out.println("Welcome back, " + currentUser.getName() + "!");

        List<Email> unreadEmails = emailService.getUnreadEmails(currentUser);
        if (!unreadEmails.isEmpty()) {
            System.out.println("You have " + unreadEmails.size() + " unread email(s):");
            for(Email email : unreadEmails) {
                System.out.printf("  + From: %s, Subject: %s (%s)\n",
                        email.getSender().getEmail(), email.getSubject(), email.getEmailCode());
            }
        } else {
            System.out.println("You have no unread emails.");
        }

        System.out.print("\n[S]end, [V]iew, [R]eply, [F]orward, [L]ogout: ");
        String choice = scanner.nextLine().toLowerCase();

        switch (choice) {
            case "s":
                handleSendEmail();
                break;
            case "v":
                handleViewEmails();
                break;
            case "l":
                currentUser = null;
                System.out.println("You have been logged out.");
                break;
            default:
                System.out.println("Invalid command.");
        }
    }

    private static void handleSignUp() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email (e.g., 'yourname' or 'yourname@milou.com'): ");
        String email = normalizeEmail(scanner.nextLine());
        System.out.print("Enter password (min 8 chars): ");
        String password = scanner.nextLine();

        Optional<User> newUser = userService.signUp(name, email, password);
        if (newUser.isPresent()) {
            System.out.println("Your new account is created. Go ahead and login!");
        } else {
            System.out.println("Sign up failed. Please check the errors above and try again.");
        }
    }

    private static void handleLogin() {
        System.out.print("Enter email: ");
        String email = normalizeEmail(scanner.nextLine());
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Optional<User> user = userService.login(email, password);
        if (user.isPresent()) {
            currentUser = user.get();
        } else {
            System.out.println("Login failed. Invalid email or password.");
        }
    }

    private static void handleSendEmail() {
        System.out.print("Recipient(s) (comma-separated): ");
        String recipientsInput = scanner.nextLine();
        List<String> recipientEmails = Arrays.asList(recipientsInput.split("\\s*,\\s*"));

        List<User> recipientUsers = new ArrayList<>();
        boolean allRecipientsValid = true;
        for(String emailStr : recipientEmails) {
            Optional<User> recipientOpt = userService.findUserByEmail(normalizeEmail(emailStr));
            if(recipientOpt.isPresent()) {
                recipientUsers.add(recipientOpt.get());
            } else {
                System.out.println("Error: User '" + emailStr + "' not found.");
                allRecipientsValid = false;
                break;
            }
        }

        if(allRecipientsValid && !recipientUsers.isEmpty()) {
            System.out.print("Subject: ");
            String subject = scanner.nextLine();
            System.out.print("Body: ");
            String body = scanner.nextLine();

            String code = emailService.sendEmail(currentUser, recipientUsers, subject, body);
            System.out.println("Successfully sent your email. Code: " + code);
        }
    }

    private static void handleViewEmails() {
        System.out.print("[A]ll received, [U]nread, [S]ent, Read by [C]ode: ");
        String choice = scanner.nextLine().toLowerCase();

        switch(choice) {
            case "a":
                displayEmailList(emailService.getReceivedEmails(currentUser), false);
                break;
            case "u":
                displayEmailList(emailService.getUnreadEmails(currentUser), false);
                break;
            case "s":
                displayEmailList(emailService.getSentEmails(currentUser), true);
                break;
            case "c":
                handleReadByCode();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void displayEmailList(List<Email> emails, boolean isSentList) {
        if(emails.isEmpty()) {
            System.out.println("No emails to display.");
            return;
        }
        for(Email email : emails) {
            if(isSentList) {
                String recipientsStr = email.getRecipients().stream().map(User::getEmail).collect(Collectors.joining(", "));
                System.out.printf("  + To: %s, Subject: %s (%s)\n", recipientsStr, email.getSubject(), email.getEmailCode());
            } else {
                System.out.printf("  + From: %s, Subject: %s (%s)\n",
                        email.getSender().getEmail(), email.getSubject(), email.getEmailCode());
            }
        }
    }

    private static void handleReadByCode() {
        System.out.print("Enter 6-digit email code: ");
        String code = scanner.nextLine();

        Optional<Email> emailOpt = emailService.readEmailByCode(code, currentUser);

        if(emailOpt.isPresent()) {
            Email email = emailOpt.get();
            System.out.println("\n--- Email Details ---");
            System.out.println("From: " + email.getSender().getEmail());
            String recipientsStr = email.getRecipients().stream().map(User::getEmail).collect(Collectors.joining(", "));
            System.out.println("To: " + recipientsStr);
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Date: " + email.getSentAt().toLocalDate());
            System.out.println("----");
            System.out.println(email.getBody());
            System.out.println("--------------------");
        } else {
            System.out.println("Could not find or access email with code: " + code);
        }
    }

    private static String normalizeEmail(String email) {
        if (!email.contains("@")) {
            return email.toLowerCase() + "@milou.com";
        }
        return email.toLowerCase();
    }
}