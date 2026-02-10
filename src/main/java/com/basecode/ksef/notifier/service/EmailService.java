package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.config.KsefClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final KsefClientProperties ksefClientProperties;
    @Value("${spring.mail.username}")
    private final String fromAddress;

    public void sendSuccessNotification(int count) {
        send("KSeF: Nowa faktura", "Znaleziono " + count + " nowych faktur.");
    }

    public void sendErrorNotification(String errorMessage, Exception ex) {
        String content = "Podczas sprawdzania KSeF wystąpił błąd:\n\n" +
            "Komunikat: " + errorMessage + "\n" +
            "Szczegóły: " + ex.toString();
        send("KSeF: Błąd sprawdzania faktur", content);
    }

    private void send(String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(ksefClientProperties.getNotificationEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
