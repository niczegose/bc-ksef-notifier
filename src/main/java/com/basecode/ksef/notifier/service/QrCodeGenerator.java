package com.basecode.ksef.notifier.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.akmf.ksef.sdk.api.KsefApiProperties;
import pl.akmf.ksef.sdk.client.interfaces.QrCodeService;
import pl.akmf.ksef.sdk.client.model.ApiException;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceMetadata;

@Component
@RequiredArgsConstructor
class QrCodeGenerator {

    private final KsefApiProperties ksefApiProperties;
    private final QrCodeService qrCodeService;

    String generateQrCodeBase64(InvoiceMetadata invoiceMetadata) throws ApiException {
        var invoiceUrl = buildInvoiceVerificationUrl(invoiceMetadata.getSeller().getNip(), invoiceMetadata.getIssueDate(), invoiceMetadata.getInvoiceHash());
        var qrCode = qrCodeService.generateQrCode(invoiceUrl);

        return Base64.getEncoder().encodeToString(qrCode);
    }

    private String buildInvoiceVerificationUrl(String nip, LocalDate issueDate, String invoiceHash) {
        var date = issueDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        var invoiceHashBytes = Base64.getDecoder().decode(invoiceHash);
        var invoiceHashUrlEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(invoiceHashBytes);

        return String.format("%s/invoice/%s/%s/%s", ksefApiProperties.getQrUri(), nip, date, invoiceHashUrlEncoded);
    }
}
