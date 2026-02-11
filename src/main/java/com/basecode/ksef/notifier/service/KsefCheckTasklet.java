package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.config.KsefClientProperties;
import com.basecode.ksef.notifier.exception.ProcessingException;
import com.basecode.ksef.notifier.model.InvoiceSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import pl.akmf.ksef.sdk.client.model.auth.ContextIdentifier;

@Slf4j
@Component
@RequiredArgsConstructor
public class KsefCheckTasklet implements Tasklet {

    private final KsefService ksefService;
    private final EmailService emailService;
    private final KsefClientProperties ksefClientProperties;
    private final KsefXmlParser ksefXmlParser;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            log.info("Logowanie do KSeF...");
            var loginResponse = ksefService.login(ContextIdentifier.IdentifierType.NIP, ksefClientProperties.getNip());

            log.info("Oczekiwanie na gotowość sesji...");
            ksefService.isAuthStatusReady(loginResponse.getReferenceNumber(), loginResponse.getAuthenticationToken().getToken());

            log.info("Pobieranie tokena sesyjnego...");
            var sessionToken = ksefService.redeemToken(loginResponse.getAuthenticationToken().getToken());

            log.info("Pobieranie listy faktur...");
            var invoices = ksefService.getInvoicesFromYesterday(sessionToken);

            if (!invoices.isEmpty()) {
                log.debug("fakturka {}", invoices.getFirst().getSeller().getName());
                Map<String, byte[]> filesToEmail = new HashMap<>();
                List<InvoiceSummary> invoiceSummaries = new ArrayList<>();

                for (var invoice : invoices) {
                    byte[] xmlContent = ksefService.getInvoiceByKsefId(invoice.getKsefNumber(), sessionToken);
                    filesToEmail.put("Faktura_" + invoice.getKsefNumber() + ".xml", xmlContent);
                    invoiceSummaries.add(ksefXmlParser.parseXml(xmlContent, invoice));
                }

                emailService.sendSuccessNotification(invoiceSummaries, filesToEmail);
            }

        } catch (Exception e) {
            log.error("Wystąpił błąd podczas procesowania KSeF!", e);
            emailService.sendErrorNotification(e.getMessage(), e);

            throw new ProcessingException(e);
        }

        return RepeatStatus.FINISHED;
    }
}
