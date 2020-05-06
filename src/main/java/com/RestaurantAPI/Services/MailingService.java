package com.RestaurantAPI.Services;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class MailingService {

    public static void sendMessage(String recipientEmail)
    {

        String host = "localhost";

        Properties properties = System.getProperties();

        String from = "retoranai@gmail.com";

        properties.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(properties);

        try
        {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

            message.setSubject("registration");

            message.setText("This is actual message");

            Transport.send(message);

            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}

