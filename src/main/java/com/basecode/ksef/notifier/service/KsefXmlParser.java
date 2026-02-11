package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.model.InvoiceSummary;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.springframework.stereotype.Component;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceMetadata;

@Component
public class KsefXmlParser {

    public static final String PAYMENT_DUE_DATE_XPATH = "//*[local-name()='Platnosc']/*[local-name()='TerminPlatnosci']/*[local-name()='Termin']";
    public static final String BANK_ACCOUNT_NUMBER_XPATH = "//*[local-name()='Platnosc']/*[local-name()='RachunekBankowy']/*[local-name()='NrRB']";

    public InvoiceSummary parseXml(byte[] xmlContent, InvoiceMetadata metadata) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        var doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlContent));
        var xpath = XPathFactory.newInstance().newXPath();

        String dueDate = xpath.evaluate(PAYMENT_DUE_DATE_XPATH, doc);
        String bankAccount = xpath.evaluate(BANK_ACCOUNT_NUMBER_XPATH, doc);

        return new InvoiceSummary(metadata, LocalDate.parse(dueDate), bankAccount);
    }
}
