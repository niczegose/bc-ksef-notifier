package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.config.KsefClientProperties;
import com.basecode.ksef.notifier.model.InvoiceSummary;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final KsefClientProperties ksefClientProperties;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private final String fromAddress;

    public void sendSuccessNotification(List<InvoiceSummary> invoices, Map<String, byte[]> attachments)
        throws MessagingException, UnsupportedEncodingException {
        log.info("Wysyłanie powiadomienia e-mail o nowych fakturach");
        var message = mailSender.createMimeMessage();

        var helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress, "BC KSeF Notifier");
        helper.setTo(ksefClientProperties.getNotificationEmail());
        helper.setSubject("KSeF: Nowa faktura");

        var context = new Context();
        context.setVariable("invoices", invoices);
        var htmlBody = templateEngine.process("mailTemplate", context);
        helper.setText(htmlBody, true);

        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
        }

        mailSender.send(message);
    }

    public void sendErrorNotification(String errorMessage, Exception ex) {
        String content = "Podczas sprawdzania KSeF wystąpił błąd:\n\n" +
            "Komunikat: " + errorMessage + "\n" +
            "Szczegóły: " + ex.toString();

        var message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(ksefClientProperties.getNotificationEmail());
        message.setSubject("KSeF: Błąd sprawdzania faktur");
        message.setText(content);
        mailSender.send(message);
    }
}
