package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.config.KsefClientProperties;
import com.basecode.ksef.notifier.exception.ProcessingException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import pl.akmf.ksef.sdk.api.builders.auth.AuthKsefTokenRequestBuilder;
import pl.akmf.ksef.sdk.api.builders.invoices.InvoiceQueryFiltersBuilder;
import pl.akmf.ksef.sdk.api.services.DefaultCryptographyService;
import pl.akmf.ksef.sdk.client.interfaces.KSeFClient;
import pl.akmf.ksef.sdk.client.model.ApiException;
import pl.akmf.ksef.sdk.client.model.auth.AuthKsefTokenRequest;
import pl.akmf.ksef.sdk.client.model.auth.AuthStatus;
import pl.akmf.ksef.sdk.client.model.auth.AuthenticationChallengeResponse;
import pl.akmf.ksef.sdk.client.model.auth.ContextIdentifier;
import pl.akmf.ksef.sdk.client.model.auth.SignatureResponse;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceMetadata;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceQueryDateRange;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceQueryDateType;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceQueryFilters;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceQuerySubjectType;
import pl.akmf.ksef.sdk.client.model.invoice.QueryInvoiceMetadataResponse;
import pl.akmf.ksef.sdk.client.model.util.SortOrder;

@Slf4j
@Service
@RequiredArgsConstructor
public class KsefService {

    private final KsefClientProperties ksefClientProperties;
    private final KSeFClient ksefClient;
    private final DefaultCryptographyService cryptographyService;

    public SignatureResponse login(ContextIdentifier.IdentifierType identifierType, String identifier) throws ApiException {
        AuthenticationChallengeResponse challenge = ksefClient.getAuthChallenge();

        byte[] encryptedToken = cryptographyService.encryptKsefTokenWithRSAUsingPublicKey(ksefClientProperties.getToken(), challenge.getTimestamp());

        AuthKsefTokenRequest authTokenRequest = new AuthKsefTokenRequestBuilder()
            .withChallenge(challenge.getChallenge())
            .withContextIdentifier(new ContextIdentifier(identifierType, identifier))
            .withEncryptedToken(Base64.getEncoder().encodeToString(encryptedToken))
            .build();

        return ksefClient.authenticateByKSeFToken(authTokenRequest);
    }

    public String redeemToken(String authenticationToken) throws ApiException {
        return ksefClient.redeemToken(authenticationToken)
            .getAccessToken()
            .getToken();
    }

    public List<InvoiceMetadata> getInvoicesFromYesterday(String sessionToken) throws ApiException {
        InvoiceQueryFilters request = new InvoiceQueryFiltersBuilder()
            .withSubjectType(InvoiceQuerySubjectType.SUBJECT2)
            .withDateRange(
                new InvoiceQueryDateRange(InvoiceQueryDateType.PERMANENTSTORAGE,
                    OffsetDateTime.now().minusDays(1),
                    OffsetDateTime.now()))
            .build();

        QueryInvoiceMetadataResponse response = ksefClient.queryInvoiceMetadata(0, 20, SortOrder.ASC, request, sessionToken);

        return response.getInvoices();
    }

    @Retryable(
        retryFor = {
            ProcessingException.class,
        }, maxAttempts = 2,
        recover = "recoverAuthReadyStatusCheck",
        backoff = @Backoff(delay = 30)

    )
    public void isAuthStatusReady(String referenceNumber, String tempToken) throws ApiException {
        log.debug("Sprawdzanie statusu autoryzacji");
        AuthStatus authStatus = ksefClient.getAuthStatus(referenceNumber, tempToken);

        if (authStatus.getStatus().getCode() != 200) {
            throw new ProcessingException("Authentication process has not been finished yet");
        }
    }

    @Recover
    public void recoverAuthReadyStatusCheck(String referenceNumber, String tempToken) throws ApiException {
        log.debug("Ostatnie sprawdzenie statusu autoryzacji");
        AuthStatus authStatus = ksefClient.getAuthStatus(referenceNumber, tempToken);

        if (authStatus.getStatus().getCode() != 200) {
            throw new ProcessingException("Authentication process has not been finished yet");
        }
    }
}
