package com.basecode.ksef.notifier.service;

import com.basecode.ksef.notifier.model.fa3.Faktura;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.akmf.ksef.sdk.client.model.ApiException;
import pl.akmf.ksef.sdk.client.model.invoice.InvoiceMetadata;

@Component
@RequiredArgsConstructor
class KsefPdfCreator {

    private final SpringTemplateEngine templateEngine;
    private final QrCodeGenerator qrCodeGenerator;

    byte[] convertToPdf(byte[] xmlContent, InvoiceMetadata invoiceMetadata) throws JAXBException, IOException, ApiException {
        var html = transformXmlToHtml(xmlContent, invoiceMetadata);

        try (var pdfStream = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/OpenSans-Regular.ttf"), "OpenSans Regular");
            builder.withHtmlContent(html, null);
            builder.toStream(pdfStream);
            builder.run();
            return pdfStream.toByteArray();
        }
    }

    private String transformXmlToHtml(byte[] xmlContent, InvoiceMetadata invoiceMetadata) throws JAXBException, ApiException {
        var invoice = parseXml(xmlContent);
        var qrCode = qrCodeGenerator.generateQrCodeBase64(invoiceMetadata);

        var context = new Context();
        context.setVariable("fa", invoice);
        context.setVariable("qrCode", qrCode);
        context.setVariable("ksefNumber", invoiceMetadata.getKsefNumber());

        return templateEngine.process("invoice", context);
    }

    private Faktura parseXml(byte[] xmlBytes) throws JAXBException {
        var context = JAXBContext.newInstance(Faktura.class);
        var unmarshaller = context.createUnmarshaller();
        return (Faktura) unmarshaller.unmarshal(new ByteArrayInputStream(xmlBytes));
    }
}
