package com.basecode.ksef.notifier.service;

import pl.akmf.ksef.sdk.client.model.auth.ContextIdentifier;
import com.basecode.ksef.notifier.config.KsefClientProperties;
import com.basecode.ksef.notifier.exception.ProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KsefCheckTasklet implements Tasklet {

    private final KsefService ksefService;
    private final EmailService emailService;
    private final KsefClientProperties ksefClientProperties;

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
                emailService.sendSuccessNotification(invoices.size());
            }

        } catch (Exception e) {
            log.error("Wystąpił błąd podczas procesowania KSeF!", e);
            emailService.sendErrorNotification(e.getMessage(), e);

            throw new ProcessingException(e);
        }

        return RepeatStatus.FINISHED;
    }
}
