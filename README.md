# KSeF Notifier

A Spring Boot batch application that automatically monitors the Polish National e-Invoice System (KSeF) for new invoices and sends email notifications with detailed summaries and invoice attachments.

## Overview

This application periodically checks for new invoices in the KSeF system using token-based authentication and notifies designated recipients via email. It includes invoice details parsed from XML and attaches the original invoice files.

## Features

- **Token Authentication**: Secure authentication using KSeF tokens with RSA encryption
- **Email Notifications**: Sends HTML email notifications with:
  - Detailed invoice summaries (seller, buyer, amounts, dates)
  - Invoice XML files as attachments
  - PDF visualizations (currently supported for VAT invoices only)
- **Error Handling**:Sends error notifications via email on application failures

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.4**
  - Spring Batch
  - Spring Mail
  - Spring Web
  - Spring Actuator
- **KSeF Client SDK 2.3.2** ([ksef-client](https://github.com/alapierre/ksef-client-java-mf-fork) - MIT License)
- **Thymeleaf** for email templates
- **H2 Database** for batch job metadata
- **Lombok** for reducing boilerplate code
- **Google Cloud Platform** support

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Active KSeF account with token authentication
- SMTP server access (Gmail configured by default)

## Configuration

The application requires the following environment variables:

### KSeF Configuration
- `NIP`: Your company's Tax Identification Number
- `KSEF_TOKEN`: KSeF authentication token
- `NOTIFICATION_EMAIL`: Email address to receive notifications

### Email Configuration
- `KSEF_NOTIFIER_EMAIL`: Gmail account for sending notifications
- `KSEF_NOTIFIER_EMAIL_PASSWORD`: Gmail app password

### Optional Configuration
- `KSEF_NOTIFIER_PROJECT_ID`: Google Cloud Project ID (if using GCP)
- `PORT`: Application port (default: 8080)

### Application Properties

```yaml
ksef:
  context:
    check-interval-in-days: 1  # How far back to check for invoices
```

## How It Works

1. **Authentication**:
   - Requests authentication challenge from KSeF
   - Encrypts token using RSA with KSeF public key
   - Authenticates using token and obtains session token

2. **Invoice Retrieval**:
   - Queries KSeF for invoices from the specified time period
   - Filters for invoices where the configured NIP is the buyer (SUBJECT2)
   - Downloads invoice XML files

3. **Notification**:
   - Parses invoice XML to extract key information
   - Generates HTML email with invoice summaries
   - Generates PDF visualization (currently enabled for VAT invoices only)
   - Attaches original XML files and PDF visualizations
   - Sends email to configured recipients

4. **Error Handling**:
   - Sends error notification emails on failure
   - Logs detailed error information

## Running the Application

### Local Development

```bash
# Set required environment variables
export NIP=your_nip
export KSEF_TOKEN=your_ksef_token
export NOTIFICATION_EMAIL=recipient@example.com
export KSEF_NOTIFIER_EMAIL=sender@gmail.com
export KSEF_NOTIFIER_EMAIL_PASSWORD=your_app_password

# Run with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Production Build

```bash
mvn clean package
java -jar target/ksef-notifier-1.0.0.jar
```

## Scheduling

The application runs as a batch job and exits after completion. To run it periodically, schedule it using:

- **Cron**: Add to crontab for scheduled execution
- **Cloud Scheduler**: Use GCP Cloud Scheduler or equivalent
- **Kubernetes CronJob**: Deploy as a CronJob in Kubernetes

Example crontab entry (runs daily at 8 AM):
```bash
0 8 * * *
```

## Email Notification Example

When new invoices are detected, the application sends an HTML email notification with the following information for each invoice:

- **KSeF Invoice Number**: Unique identifier from KSeF system
- **Seller**: Company name
- **Invoice Number**: Original invoice number from seller
- **Gross Amount**: Total amount with currency (highlighted in red)
- **Issue Date**: Invoice issue date
- **Due Date**: Payment due date (highlighted in red)
- **Bank Account Number**: Seller's bank account for payment

### Sample Email Output

```
Subject: KSeF: Nowa faktura

Zestawienie faktur pobranych z KSeF:

┌─────────────────────────────────────────────────────────────┐
│ Faktura KSeF o identyfikatorze: 1234567890-12345678-1234... │
├──────────────────────────────┬──────────────────────────────┤
│ SPRZEDAWCA                   │           Example Sp. z o.o. │
│ NR FAKTURY                   │                  FV/2026/001 │
│ KWOTA BRUTTO                 │                 1 234,56 PLN │
│ DATA WYSTAWIENIA             │                   2026-02-11 │
│ TERMIN PŁATNOŚCI             │                   2026-02-25 │
│ NR RACHUNKU BANKOWEGO        │       12 3456 7890 1234 5678 │
└──────────────────────────────┴──────────────────────────────┘

 Wszystkie powyższe faktury zostały dołączone do niniejszej wiadomości w formacie XML oraz wizualizacje faktur VAT w formacie PDF.

Uwaga: Jeśli faktura jest innego typu niż VAT, np. KOR (korygująca) jej wizualizacja w formacie PDF nie jest (aktualnie) generowana

Attachments: Faktura_1234567890-12345678-1234....xml, Faktura_1234567890-12345678-1234..._wizualizacja.pdf
```

## Dependencies Attribution

This project uses the [ksef-client](https://github.com/alapierre/ksef-client-java-mf-fork) library (version 2.3.2), licensed under the MIT License, for integration with the Polish National e-Invoice System (KSeF).

## Support

For issues related to:
- **KSeF API**: Refer to [official KSeF documentation](https://ksef.podatki.gov.pl/ksef-na-okres-obligatoryjny/wsparcie-dla-integratorow)
- **ksef-client library**: Check [ksef-client repository](https://github.com/alapierre/ksef-client-java-mf-fork)
