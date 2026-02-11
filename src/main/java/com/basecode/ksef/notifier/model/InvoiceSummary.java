package com.basecode.ksef.notifier.model;

import java.time.LocalDate;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceMetadata;

public record InvoiceSummary(
    InvoiceMetadata metadata,
    LocalDate dueDate,
    String accountNumber
) {

}
