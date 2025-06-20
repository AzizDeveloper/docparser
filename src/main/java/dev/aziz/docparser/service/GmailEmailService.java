package dev.aziz.docparser.service;


import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

@Service
public class GmailEmailService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    public String fetchLatestEmail() {
        try {
            Session session = Session.getInstance(getMailProperties(), null);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("[Gmail]/All Mail");
            inbox.open(Folder.READ_ONLY);

//            Message[] messages = inbox.getMessages();
//            if (messages.length == 0) {
//                System.out.println("No emails found.");
//                return "No emails found.";
//            }
//
//            Message latest = messages[messages.length - 1];

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1); // 1 day ago
            Date oneDayAgo = calendar.getTime();

            SearchTerm newerThanYesterday = new ReceivedDateTerm(ComparisonTerm.GT, oneDayAgo);
            Message[] recentMessages = inbox.search(newerThanYesterday);

            if (recentMessages.length == 0) {
                System.out.println("No recent emails found.");
                return null;
            }

            // Optional: sort by sent date (only among recent ones)
            Arrays.sort(recentMessages, Comparator.comparing(m -> {
                try {
                    return m.getSentDate();
                } catch (MessagingException e) {
                    return new Date(0);
                }
            }));

            Message latest = recentMessages[recentMessages.length - 1];



            System.out.println("Subject: " + latest.getSubject());
            System.out.println("From: " + latest.getFrom()[0]);
            System.out.println("Date: " + latest.getSentDate());
            System.out.println("Body: ");
            System.out.println(getTextFromMessage(latest));

            String response = formatEmailDetails(latest);

            inbox.close(false);
            store.close();

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No emails found.";
    }

    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.ssl.enable", "true");
        return props;
    }

//    private String getTextFromMessage(Message message) throws Exception {
//        Object content = message.getContent();
//        if (content instanceof String) {
//            return (String) content;
//        }
//
//        if (content instanceof Multipart) {
//            Multipart multipart = (Multipart) content;
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart part = multipart.getBodyPart(i);
//                if (part.getContent() instanceof String) {
//                    return (String) part.getContent();
//                }
//            }
//        }
//
//        return "";
//    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain")) {
                    return part.getContent().toString(); // Prefer plain text
                } else if (part.isMimeType("text/html")) {
                    // Optionally fallback to HTML if plain not found
                    String html = part.getContent().toString();
                    return org.jsoup.Jsoup.parse(html).text(); // Strip HTML tags
                }
            }
        }
        return "";
    }

    public String formatEmailDetails(Message message) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(Arrays.toString(message.getFrom())).append("\n");
        sb.append("To: ").append(Arrays.toString(message.getRecipients(Message.RecipientType.TO))).append("\n");
        sb.append("Date: ").append(message.getSentDate()).append("\n");
        sb.append("Subject: ").append(message.getSubject()).append("\n");
        sb.append("Body:\n").append(getTextFromMessage(message)).append("\n");
        return sb.toString();
    }


}

