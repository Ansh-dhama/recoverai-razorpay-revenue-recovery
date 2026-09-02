# RecoverAI — Razorpay AI Revenue Recovery Platform

RecoverAI is an AI-assisted failed payment recovery platform built for the Razorpay AI Buildathon.

It detects failed Razorpay payments, analyzes the reason for failure, recommends the best recovery action using Gemini AI with deterministic rule fallback, schedules retries, stops excessive retry loops, escalates risky cases for human review, and provides recovery analytics through a professional dashboard.

---

## Problem

Failed digital payments directly impact revenue.

A payment may fail because of:

- Insufficient funds
- Bank decline
- Network timeout
- Gateway error
- Authentication failure
- Temporary technical issues
- Payment method-specific problems

In many systems, failed payments are simply recorded as failures.

RecoverAI instead turns them into an intelligent recovery workflow.

---

## Core Idea

```text
Payment Attempt
      ↓
Payment Failure
      ↓
Razorpay Webhook
      ↓
Recovery Case
      ↓
AI / Rule Analysis
      ↓
Recovery Decision
      ↓
Execute / Schedule
      ↓
Retry / Escalate
      ↓
Recover Revenue
```

RecoverAI follows the principle:

```text
Detect → Understand → Decide → Execute → Recover
```

---

# Key Features

## 1. Razorpay Payment Integration

RecoverAI creates Razorpay payment orders from the frontend.

Each order stores:

- Merchant Order ID
- Razorpay Order ID
- Customer ID
- Amount
- Currency
- Receipt
- Order Status
- Payment Attempts

Example flow:

```text
Frontend
   ↓
Create Payment Order
   ↓
Razorpay Order
   ↓
Razorpay Checkout
```

---

## 2. Razorpay Webhook Processing

RecoverAI processes Razorpay webhook events including:

```text
payment.authorized
payment.failed
payment.captured
```

When a payment fails:

```text
payment.failed
      ↓
Payment = FAILED
      ↓
PaymentOrder = ATTEMPTED
      ↓
RecoveryCase = PENDING_ANALYSIS
```

Webhook events are also stored for:

- Auditability
- Idempotency
- Duplicate event protection
- Event status tracking

---

## 3. Recovery Case Creation

Every failed payment creates a recovery case.

A recovery case stores:

- Payment
- Status
- Recommended action
- Confidence
- Reason
- Recovery attempt count
- Next retry time
- Last attempt time

Initial state:

```text
PENDING_ANALYSIS
```

---

## 4. AI-Assisted Recovery Intelligence

RecoverAI uses Google Gemini through Spring AI.

The AI receives payment context including:

- Payment method
- Amount
- Currency
- Failure reason
- Failure description
- Previous recovery attempts

The AI can recommend:

```text
RETRY_NOW
RETRY_LATER
ALTERNATIVE_PAYMENT_METHOD
HUMAN_REVIEW
NO_ACTION
```

Each recommendation contains:

- Action
- Confidence
- Reason

---

## 5. Hybrid AI + Rule Fallback

RecoverAI does not completely depend on an LLM.

If Gemini is:

- Unavailable
- Rate limited
- Quota exhausted
- Returning invalid output
- Temporarily failing

RecoverAI automatically switches to the deterministic recovery rule engine.

```text
Gemini Available
      ↓
AI Decision
      ↓
Recovery Flow
```

If Gemini is unavailable:

```text
Gemini Unavailable
      ↓
Rule Engine
      ↓
Recovery Flow Continues
```

This makes RecoverAI resilient to AI-provider failures.

---

## 6. Rule-Based Recovery Engine

The deterministic fallback engine handles known payment failure patterns.

| Failure Type | Recovery Action | Confidence |
|---|---|---:|
| Insufficient funds | `ALTERNATIVE_PAYMENT_METHOD` | 94% |
| Bank / issuer decline | `ALTERNATIVE_PAYMENT_METHOD` | 90% |
| Temporary gateway issue | `RETRY_NOW` | 90% |
| Network / timeout issue | `RETRY_LATER` | 86% |
| Authentication / OTP failure | `RETRY_NOW` | 82% |
| Generic UPI failure | `RETRY_NOW` | 78% |
| Unknown failure | `RETRY_LATER` | 65% |
| 3+ recovery attempts | `HUMAN_REVIEW` | 95% |

Example:

```text
Bank Declined Transaction
        ↓
ALTERNATIVE_PAYMENT_METHOD
        ↓
Confidence = 90%
```

---

## 7. Recovery Scheduling

When the recovery decision is:

```text
RETRY_LATER
```

the recovery case must be scheduled.

```text
RECOVERY_PLANNED
      ↓
Schedule
      ↓
RECOVERY_SCHEDULED
      ↓
nextRetryAt
      ↓
Spring Scheduler
      ↓
RECOVERY_IN_PROGRESS
```

The scheduler automatically processes recovery cases whose retry time has arrived.

---

## 8. Retry Protection

RecoverAI prevents infinite payment recovery loops.

Maximum automatic recovery attempts:

```text
3
```

Flow:

```text
Attempt 1
   ↓
Attempt 2
   ↓
Attempt 3
   ↓
Retry Limit Reached
   ↓
HUMAN_REVIEW
```

After the maximum retry limit is reached:

```text
Automatic Retry = STOPPED
```

---

## 9. Recovery State Machine

The normal recovery lifecycle is:

```text
PENDING_ANALYSIS
      ↓
ANALYZING
      ↓
RECOVERY_PLANNED
      ↓
RECOVERY_SCHEDULED
      ↓
RECOVERY_IN_PROGRESS
      ↓
RECOVERED
```

Other possible states include:

```text
HUMAN_REVIEW
FAILED
CANCELLED
```

---

## 10. Invalid Recovery Flow Protection

RecoverAI validates recovery actions before executing them.

For example:

```text
RETRY_LATER
```

cannot be executed directly.

Incorrect flow:

```text
RETRY_LATER
      ↓
Execute
      ↓
409 Conflict
```

Correct flow:

```text
RETRY_LATER
      ↓
Schedule
      ↓
RECOVERY_SCHEDULED
```

This protects the recovery lifecycle from invalid state transitions.

---

## 11. Buildathon Simulation Engine

RecoverAI includes a synthetic batch simulation engine for demonstrating revenue recovery at scale.

Two simulation modes are supported.

### FAST_SIMULATION

This mode uses deterministic recovery rules and does not call Gemini.

It is recommended for large Buildathon demo batches.

Example:

```json
{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}
```

This mode is useful for:

- 50-case simulation
- 100-case simulation
- Fast demo metrics
- Reliable Buildathon presentation

### LIVE_AI

This mode uses the real Gemini-powered hybrid recovery engine.

Example:

```json
{
  "size": 1,
  "seed": 42,
  "mode": "LIVE_AI"
}
```

If Gemini fails:

```text
LIVE_AI
   ↓
Gemini Error / Quota Limit
   ↓
Rule Fallback
   ↓
Simulation Continues
```

---

## 12. Buildathon Metrics

The dashboard shows:

- Revenue At Risk
- Simulated Revenue Recovered
- Recovery Rate
- AI Decisions
- Rule Fallbacks
- Human Reviews
- Retries Stopped
- Recovered Cases

Example:

```text
Synthetic Failed Payments: 100
Revenue At Risk: ₹X
Simulated Revenue Recovered: ₹Y
Recovery Rate: Z%
AI Decisions: N
Rule Fallbacks: N
Human Reviews: N
Retries Stopped: N
```

> **Important:** Buildathon simulation metrics are synthetic and are not claimed as actual production revenue recovered.

---

## 13. Professional Dashboard

RecoverAI provides a complete frontend dashboard.

Pages:

```text
/
payments.html
recoveries.html
webhooks.html
```

The dashboard includes:

- Total orders
- Failed payments
- Recovery cases
- Recovered cases
- Pending analysis
- Recovery planned
- Scheduled recoveries
- Human-review cases
- Recent payments
- Recent recovery cases
- Buildathon simulation metrics

---

# Architecture

```text
                        ┌──────────────────────┐
                        │      Frontend        │
                        │ HTML / CSS / JS      │
                        └──────────┬───────────┘
                                   │
                                   ▼
                        ┌──────────────────────┐
                        │   Spring Boot API    │
                        │      Java 21         │
                        └──────────┬───────────┘
                                   │
             ┌─────────────────────┼──────────────────────┐
             │                     │                      │
             ▼                     ▼                      ▼
      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
      │   Razorpay   │      │  Gemini AI   │      │ Rule Engine  │
      │   Payments   │      │  Spring AI   │      │   Fallback   │
      └──────┬───────┘      └──────────────┘      └──────────────┘
             │
             ▼
      ┌──────────────┐
      │   Webhooks   │
      └──────┬───────┘
             │
             ▼
      ┌──────────────────┐
      │ Recovery Engine  │
      └──────┬───────────┘
             │
             ▼
      ┌──────────────────┐
      │ Spring Scheduler │
      └──────┬───────────┘
             │
             ▼
      ┌──────────────────┐
      │      MySQL       │
      └──────────────────┘
```

---

# Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Spring Scheduler
- Spring AI
- Maven

## AI

- Google Gemini
- Spring AI Google GenAI Integration

## Payments

- Razorpay Java SDK
- Razorpay Checkout
- Razorpay Webhooks

## Database

- MySQL 8

## Frontend

- HTML
- CSS
- Vanilla JavaScript

## Development Tools

- IntelliJ IDEA
- Postman
- Cloudflare Tunnel
- Git
- GitHub

---

# Project Structure

```text
src/main/java/com/example/razorpay_recover_ai/

├── config
│   ├── RazorpayConfig
│   └── RazorpayProperties
│
├── controller
│   ├── PaymentOrderController
│   ├── PaymentController
│   ├── RazorpayWebhookController
│   ├── RecoveryAnalysisController
│   ├── RecoveryExecutionController
│   ├── RecoverySchedulerController
│   ├── DashboardController
│   └── BuildathonSimulationController
│
├── dto
│
├── entity
│   ├── Payment
│   ├── PaymentOrder
│   ├── RecoveryCase
│   └── WebhookEvent
│
├── enums
│
├── exception
│
├── repository
│
├── schedular
│   └── RecoveryScheduler
│
├── service
│   ├── AiRecoveryAnalysisServiceImpl
│   ├── HybridRecoveryDecisionServiceImpl
│   ├── RuleBasedRecoveryDecisionServiceImpl
│   ├── RazorpayWebhookProcessor
│   ├── RecoveryExecutionServiceImpl
│   ├── RecoverySchedulerServiceImpl
│   └── BuildathonSimulationServiceImpl
│
└── serviceInterface
```

Frontend:

```text
src/main/resources/static/

├── index.html
├── payments.html
├── recoveries.html
├── webhooks.html
│
├── css/
│   └── app.css
│
└── js/
    ├── app.js
    ├── dashboard.js
    ├── payments.js
    ├── recoveries.js
    └── webhooks.js
```

---

# Database Model

## PaymentOrder

Represents a merchant/Razorpay order.

Important fields:

```text
id
merchantOrderId
razorpayOrderId
customerId
amount
currency
receipt
status
attempts
createdAt
```

---

## Payment

Represents an individual payment attempt.

Important fields:

```text
id
razorpayPaymentId
paymentOrder
customerId
amount
currency
paymentMethod
paymentStatus
failureReason
failureDescription
```

One `PaymentOrder` may have multiple `Payment` attempts.

---

## RecoveryCase

Represents the payment recovery lifecycle.

Important fields:

```text
id
payment
status
recommendedAction
confidence
reason
attemptCount
nextRetryAt
lastAttemptAt
analysisSource
```

---

## WebhookEvent

Stores Razorpay webhook events.

Used for:

- Idempotency
- Duplicate protection
- Auditability
- Processing status

---

# Important REST APIs

## Create Payment Order

```http
POST /api/v1/payment-orders
```

## Get Payment Orders

```http
GET /api/v1/payment-orders
```

## Get Payments

```http
GET /api/v1/payments
```

## Razorpay Webhook

```http
POST /api/v1/webhooks/razorpay
```

## Recovery Analysis

Analyze one recovery case:

```http
POST /api/v1/recovery-cases/{id}/analyze
```

Analyze pending cases:

```http
POST /api/v1/recovery-cases/analyze-pending
```

## Recovery Execution

```http
POST /api/v1/recovery-cases/{id}/execute
```

## Recovery Scheduling

```http
POST /api/v1/recovery-cases/{id}/schedule
```

Process due recoveries:

```http
POST /api/v1/recovery-cases/process-due
```

## Dashboard Summary

```http
GET /api/v1/dashboard/summary
```

## Buildathon Simulation

```http
POST /api/v1/buildathon/simulate
```

Example request:

```json
{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}
```

---

# Environment Variables

Never store credentials directly inside GitHub.

RecoverAI uses environment variables.

Required variables:

```text
MYSQL_PASSWORD

RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
RAZORPAY_WEBHOOK_SECRET

GEMINI_API_KEY
```

Example `application.properties`:

```properties
spring.datasource.password=${MYSQL_PASSWORD}

razorpay.key-id=${RAZORPAY_KEY_ID}
razorpay.key-secret=${RAZORPAY_KEY_SECRET}
razorpay.webhook-secret=${RAZORPAY_WEBHOOK_SECRET}

spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

---

# Local Setup

## 1. Clone Repository

```bash
git clone https://github.com/Ansh-dhama/recoverai-razorpay-revenue-recovery.git
```

Then:

```bash
cd recoverai-razorpay-revenue-recovery
```

---

## 2. Create MySQL Database

```sql
CREATE DATABASE recover_ai;
```

---

## 3. Configure Environment Variables

macOS / Linux:

```bash
export MYSQL_PASSWORD="your_mysql_password"

export RAZORPAY_KEY_ID="your_razorpay_key_id"
export RAZORPAY_KEY_SECRET="your_razorpay_key_secret"
export RAZORPAY_WEBHOOK_SECRET="your_webhook_secret"

export GEMINI_API_KEY="your_gemini_api_key"
```

---

## 4. Build Application

```bash
./mvnw clean package
```

---

## 5. Run Application

```bash
./mvnw spring-boot:run
```

Application runs on:

```text
http://localhost:8081
```

---

# Razorpay Webhook Setup

Because Razorpay needs a public webhook URL, use Cloudflare Tunnel for local development.

Run:

```bash
cloudflared tunnel --url http://localhost:8081
```

Cloudflare will provide a temporary public URL:

```text
https://xxxxx.trycloudflare.com
```

Configure the Razorpay Test Mode webhook as:

```text
https://xxxxx.trycloudflare.com/api/v1/webhooks/razorpay
```

Enable:

```text
payment.authorized
payment.failed
payment.captured
```

> Cloudflare Quick Tunnel URLs change when the tunnel restarts.

---

# End-to-End Flow Tested

RecoverAI was manually tested through the following flow:

```text
Create Payment Order
        ↓
Razorpay Checkout
        ↓
Payment Failure
        ↓
payment.failed Webhook
        ↓
PaymentOrder = ATTEMPTED
        ↓
Payment = FAILED
        ↓
RecoveryCase = PENDING_ANALYSIS
        ↓
Analyze Recovery
        ↓
Recovery Action Generated
        ↓
Execute / Schedule
        ↓
Retry Attempts
        ↓
Maximum Retry Limit
        ↓
HUMAN_REVIEW
```

---

# Tested Recovery Example — RETRY_LATER

```text
RecoveryCase
    ↓
RECOVERY_PLANNED
    ↓
RETRY_LATER
    ↓
Schedule
    ↓
RECOVERY_SCHEDULED
    ↓
Scheduler
    ↓
RECOVERY_IN_PROGRESS
```

After repeated recovery attempts:

```text
Attempt 1
   ↓
Attempt 2
   ↓
Attempt 3
   ↓
HUMAN_REVIEW
```

---

# Tested Recovery Example — Bank Decline

Failure:

```text
Bank declined the transaction
```

Decision:

```text
ALTERNATIVE_PAYMENT_METHOD
```

Confidence:

```text
90%
```

Flow:

```text
PENDING_ANALYSIS
      ↓
Analyze
      ↓
RECOVERY_PLANNED
      ↓
ALTERNATIVE_PAYMENT_METHOD
      ↓
Execute
      ↓
RECOVERY_IN_PROGRESS
```

---

# Gemini Quota Resilience

Gemini API quota limits are handled through RecoverAI's hybrid recovery architecture.

Instead of stopping the recovery process:

```text
Gemini 429 / Quota Error
        ↓
Hybrid Engine catches failure
        ↓
Rule Engine executes
        ↓
Recovery continues
```

This demonstrates graceful degradation.

---

# Security

Secrets must never be committed to GitHub.

Protected values include:

- MySQL password
- Razorpay Key Secret
- Razorpay Webhook Secret
- Gemini API Key

The repository uses environment-variable placeholders.

If any secret is accidentally exposed, it should be rotated immediately.

---

# Reliability Features

RecoverAI includes:

- Webhook idempotency
- Duplicate webhook protection
- Recovery state validation
- AI output validation
- Deterministic rule fallback
- Retry-attempt limits
- Scheduler-based delayed recovery
- Human-review escalation
- Invalid execution protection
- Synthetic batch simulation

---

# Buildathon Demo Flow

## Step 1 — Create Payment

Create a Razorpay payment order.

Example:

```text
₹499
```

## Step 2 — Open Razorpay Checkout

Open the Razorpay Checkout interface from the frontend.

## Step 3 — Simulate Payment Failure

Create a failed payment in Razorpay Test Mode.

## Step 4 — Verify Webhook Processing

Show:

```text
Payment = FAILED
PaymentOrder = ATTEMPTED
RecoveryCase = PENDING_ANALYSIS
```

## Step 5 — Analyze Recovery

Click:

```text
Analyze
```

Show the Gemini or deterministic fallback recommendation.

## Step 6 — Show Recovery Action

Example:

```text
RETRY_LATER
```

or:

```text
ALTERNATIVE_PAYMENT_METHOD
```

## Step 7 — Schedule or Execute

Execute the action based on the recommendation.

## Step 8 — Demonstrate Retry Protection

```text
Attempt 1
Attempt 2
Attempt 3
        ↓
HUMAN_REVIEW
```

## Step 9 — Run Buildathon Simulation

```json
{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}
```

## Step 10 — Show Metrics

Display:

- Revenue At Risk
- Simulated Revenue Recovered
- Recovery Rate
- Rule Decisions
- Human Reviews
- Retries Stopped

---

# Known Limitations

RecoverAI is currently a Buildathon prototype.

Current limitations include:

- No SMS notification yet
- No WhatsApp notification yet
- No email recovery notification yet
- No production authentication/authorization
- Customer payment retry link is not fully automated
- Recovery checkout reopening is not fully automated
- Scheduler currently runs inside the same Spring Boot application
- Gemini free-tier quota may limit `LIVE_AI` demonstration volume

---

# Future Improvements

Future versions can support:

```text
Customer Notification
        ↓
Retry Payment Link
        ↓
Razorpay Checkout
        ↓
Successful Payment
        ↓
payment.captured
        ↓
RecoveryCase = RECOVERED
```

Additional improvements:

- Email recovery notifications
- SMS recovery notifications
- WhatsApp recovery links
- Merchant-specific recovery rules
- ML-based recovery probability prediction
- Best-time-to-retry prediction
- Persistent AI decision history
- Admin authentication
- Distributed scheduling
- Kafka-based event processing
- Advanced merchant analytics

---

# Why RecoverAI?

RecoverAI combines:

```text
Razorpay
   +
Payment Webhooks
   +
Gemini AI
   +
Deterministic Rules
   +
Scheduled Recovery
   +
Retry Protection
   +
Human Escalation
   +
Recovery Analytics
```

Instead of treating a failed payment as the end of the transaction, RecoverAI treats it as the beginning of an intelligent revenue recovery workflow.

---

# Author

**Ansh Dhama**

GitHub:  
https://github.com/Ansh-dhama

---

# Repository

https://github.com/Ansh-dhama/recoverai-razorpay-revenue-recovery

---

# Disclaimer

RecoverAI is a Razorpay AI Buildathon prototype.

The Buildathon simulation module generates synthetic failed-payment scenarios and synthetic recovery outcomes.

Simulation revenue values are demonstration metrics and are **not claimed as actual production revenue recovered from real customers**.
