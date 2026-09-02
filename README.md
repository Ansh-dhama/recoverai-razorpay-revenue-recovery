# RecoverAI — Razorpay AI Revenue Recovery Platform

RecoverAI is an AI-assisted failed payment recovery platform built for the Razorpay AI Buildathon.

It detects failed Razorpay payments, analyzes the reason for failure, recommends the best recovery action using Gemini AI with deterministic rule fallback, schedules retries, stops excessive retry loops, escalates risky cases for human review, and provides recovery analytics through a professional dashboard.

---

## Problem

Failed digital payments directly impact revenue.

A payment may fail because of:

- insufficient funds
- bank decline
- network timeout
- gateway error
- authentication failure
- temporary technical issues
- payment method-specific problems

In many systems, failed payments are simply recorded as failures.

RecoverAI instead turns them into an intelligent recovery workflow.

---

# Core Idea

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


Key Features
1. Razorpay Payment Integration

RecoverAI creates Razorpay payment orders from the frontend.

Each order stores:

merchant order ID
Razorpay order ID
customer ID
amount
currency
receipt
order status
payment attempts

Example flow:

Frontend
   ↓
Create Payment Order
   ↓
Razorpay Order
   ↓
Razorpay Checkout
2. Razorpay Webhook Processing

RecoverAI processes Razorpay webhook events including:

payment.authorized
payment.failed
payment.captured

When a payment fails:

payment.failed
      ↓
Payment = FAILED
      ↓
PaymentOrder = ATTEMPTED
      ↓
RecoveryCase = PENDING_ANALYSIS

Webhook events are also stored for:

auditability
idempotency
duplicate event protection
event status tracking
3. Recovery Case Creation

Every failed payment creates a recovery case.

A recovery case stores:

payment
status
recommended action
confidence
reason
recovery attempt count
next retry time
last attempt time

Initial state:

PENDING_ANALYSIS
4. AI-Assisted Recovery Intelligence

RecoverAI uses Google Gemini through Spring AI.

The AI receives payment context including:

Payment Method
Amount
Currency
Failure Reason
Failure Description
Previous Recovery Attempts

The AI can recommend:

RETRY_NOW
RETRY_LATER
ALTERNATIVE_PAYMENT_METHOD
HUMAN_REVIEW
NO_ACTION

Each recommendation contains:

Action
Confidence
Reason
5. Hybrid AI + Rule Fallback

RecoverAI does not completely depend on an LLM.

If Gemini is:

unavailable
rate limited
quota exhausted
returning invalid output
temporarily failing

RecoverAI automatically switches to the deterministic recovery rule engine.

Gemini Available
      ↓
AI Decision
      ↓
Recovery Flow

Gemini Unavailable
      ↓
Rule Engine
      ↓
Recovery Flow Continues

This makes RecoverAI resilient to AI provider failures.

6. Rule-Based Recovery Engine

The deterministic fallback engine handles known payment failure patterns.

Failure Type	Recovery Action	Confidence
Insufficient funds	ALTERNATIVE_PAYMENT_METHOD	94%
Bank / issuer decline	ALTERNATIVE_PAYMENT_METHOD	90%
Temporary gateway issue	RETRY_NOW	90%
Network / timeout issue	RETRY_LATER	86%
Authentication / OTP failure	RETRY_NOW	82%
Generic UPI failure	RETRY_NOW	78%
Unknown failure	RETRY_LATER	65%
3+ recovery attempts	HUMAN_REVIEW	95%

Example:

Bank Declined Transaction
        ↓
ALTERNATIVE_PAYMENT_METHOD
        ↓
Confidence = 90%
7. Recovery Scheduling

When the decision is:

RETRY_LATER

the customer recovery case must be scheduled.

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

The scheduler automatically processes due recovery cases.

8. Retry Protection

RecoverAI prevents infinite payment recovery loops.

Maximum automatic recovery attempts:

3

Flow:

Attempt 1
   ↓
Attempt 2
   ↓
Attempt 3
   ↓
Retry Limit Reached
   ↓
HUMAN_REVIEW

After the maximum attempt limit is reached:

Automatic Retry = STOPPED
9. Recovery State Machine

The normal recovery lifecycle is:

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

Other possible states:

HUMAN_REVIEW
FAILED
CANCELLED
10. Invalid Recovery Flow Protection

RecoverAI validates recovery actions.

For example:

RETRY_LATER

cannot be directly executed.

It must be scheduled.

Incorrect flow:

RETRY_LATER
      ↓
Execute
      ↓
409 Conflict

Correct flow:

RETRY_LATER
      ↓
Schedule
      ↓
RECOVERY_SCHEDULED

This protects the recovery lifecycle from invalid transitions.

11. Buildathon Simulation Engine

RecoverAI includes a synthetic batch simulation engine for demonstrating revenue recovery at scale.

Two simulation modes are supported.

FAST_SIMULATION

This mode uses deterministic recovery rules and does not call Gemini.

Recommended for large batches.

Example:

{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}

This mode is ideal for:

50-case simulation
100-case simulation
demo metrics
fast Buildathon presentation
LIVE_AI

This mode uses the real Gemini-powered hybrid recovery engine.

Example:

{
  "size": 1,
  "seed": 42,
  "mode": "LIVE_AI"
}

If Gemini fails:

LIVE_AI
   ↓
Gemini Error / Quota Limit
   ↓
Rule Fallback
   ↓
Simulation Continues
12. Buildathon Metrics

The dashboard shows:

Revenue At Risk
Simulated Revenue Recovered
Recovery Rate
AI Decisions
Rule Fallbacks
Human Reviews
Retries Stopped
Recovered Cases

Example:

Synthetic Failed Payments: 100
Revenue At Risk: ₹X
Simulated Revenue Recovered: ₹Y
Recovery Rate: Z%
AI Decisions: N
Rule Fallbacks: N
Human Reviews: N
Retries Stopped: N

Important: Buildathon simulation metrics are synthetic and are not claimed as actual production revenue recovered.

13. Professional Dashboard

RecoverAI provides a complete frontend dashboard.

Pages:

/
payments.html
recoveries.html
webhooks.html

The dashboard includes:

total orders
failed payments
recovery cases
recovered cases
pending analysis
recovery planned
scheduled recoveries
human review cases
recent payments
recent recovery cases
Buildathon simulation metrics
Architecture
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
Technology Stack
Backend
Java 21
Spring Boot
Spring MVC
Spring Data JPA
Hibernate
Spring Scheduler
Spring AI
Maven
AI
Google Gemini
Spring AI Google GenAI Integration
Payments
Razorpay Java SDK
Razorpay Checkout
Razorpay Webhooks
Database
MySQL 8
Frontend
HTML
CSS
Vanilla JavaScript
Development Tools
IntelliJ IDEA
Postman
Cloudflare Tunnel
Git
GitHub
Project Structure
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

Frontend:

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
Database Model
PaymentOrder

Represents a merchant/Razorpay order.

Important fields:

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
Payment

Represents an individual payment attempt.

Important fields:

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

One PaymentOrder may have multiple Payment attempts.

RecoveryCase

Represents the recovery lifecycle.

Important fields:

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
WebhookEvent

Stores Razorpay webhook events.

Used for:

Idempotency
Duplicate Protection
Auditability
Processing Status
Important REST APIs
Create Payment Order
POST /api/v1/payment-orders
Get Payment Orders
GET /api/v1/payment-orders
Get Payments
GET /api/v1/payments
Razorpay Webhook
POST /api/v1/webhooks/razorpay
Recovery Analysis

Analyze one recovery case:

POST /api/v1/recovery-cases/{id}/analyze

Analyze pending cases:

POST /api/v1/recovery-cases/analyze-pending
Recovery Execution
POST /api/v1/recovery-cases/{id}/execute
Recovery Scheduling
POST /api/v1/recovery-cases/{id}/schedule

Process due recoveries:

POST /api/v1/recovery-cases/process-due
Dashboard Summary
GET /api/v1/dashboard/summary
Buildathon Simulation
POST /api/v1/buildathon/simulate

Example:

{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}
Environment Variables

Never store credentials directly inside GitHub.

RecoverAI uses environment variables.

Required variables:

MYSQL_PASSWORD

RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
RAZORPAY_WEBHOOK_SECRET

GEMINI_API_KEY

Example application.properties:

spring.datasource.password=${MYSQL_PASSWORD}

razorpay.key-id=${RAZORPAY_KEY_ID}
razorpay.key-secret=${RAZORPAY_KEY_SECRET}
razorpay.webhook-secret=${RAZORPAY_WEBHOOK_SECRET}

spring.ai.google.genai.api-key=${GEMINI_API_KEY}
Local Setup
1. Clone Repository
git clone https://github.com/Ansh-dhama/recoverai-razorpay-revenue-recovery.git

Then:

cd recoverai-razorpay-revenue-recovery
2. Create MySQL Database
CREATE DATABASE recover_ai;
3. Configure Environment Variables

macOS / Linux:

export MYSQL_PASSWORD="your_mysql_password"

export RAZORPAY_KEY_ID="your_razorpay_key_id"

export RAZORPAY_KEY_SECRET="your_razorpay_key_secret"

export RAZORPAY_WEBHOOK_SECRET="your_webhook_secret"

export GEMINI_API_KEY="your_gemini_api_key"
4. Build Application
./mvnw clean package
5. Run Application
./mvnw spring-boot:run

Application runs on:

http://localhost:8081
Razorpay Webhook Setup

Because Razorpay needs a public webhook URL, use Cloudflare Tunnel for local development.

Run:

cloudflared tunnel --url http://localhost:8081

Cloudflare will provide:

https://xxxxx.trycloudflare.com

Configure Razorpay Test Mode webhook:

https://xxxxx.trycloudflare.com/api/v1/webhooks/razorpay

Enable:

payment.authorized
payment.failed
payment.captured

Cloudflare Quick Tunnel URLs change when the tunnel restarts.

End-to-End Flow Tested

RecoverAI was manually tested through the following flow:

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
Tested Recovery Example — RETRY_LATER
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

After repeated failures:

Attempt 1
Attempt 2
Attempt 3
    ↓
HUMAN_REVIEW
Tested Recovery Example — Bank Decline

Failure:

Bank declined the transaction

Decision:

ALTERNATIVE_PAYMENT_METHOD

Confidence:

90%

Flow:

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
Gemini Quota Resilience

During testing, Gemini API quota limits were also handled.

Instead of crashing:

Gemini 429 / Quota Error
        ↓
Hybrid Engine catches failure
        ↓
Rule Engine executes
        ↓
Recovery continues

This demonstrates graceful degradation.

Security

Secrets must never be committed to GitHub.

Protected values include:

MySQL password
Razorpay Key Secret
Razorpay Webhook Secret
Gemini API Key

The repository uses environment variable placeholders.

If any secret is accidentally exposed, it should be rotated immediately.

Reliability Features

RecoverAI includes:

webhook idempotency
duplicate webhook protection
recovery state validation
AI output validation
deterministic rule fallback
retry attempt limits
scheduler-based delayed recovery
human review escalation
invalid execution protection
synthetic batch simulation
Buildathon Demo Flow

Recommended demonstration:

Step 1

Create payment order.

₹499
Step 2

Open Razorpay Checkout.

Step 3

Simulate failed payment.

Step 4

Show:

Payment = FAILED
PaymentOrder = ATTEMPTED
RecoveryCase = PENDING_ANALYSIS
Step 5

Click:

Analyze

Show AI / fallback recommendation.

Step 6

Show recovery action:

RETRY_LATER

or:

ALTERNATIVE_PAYMENT_METHOD
Step 7

Schedule or execute the recovery.

Step 8

Demonstrate max retry protection:

Attempt 1
Attempt 2
Attempt 3
        ↓
HUMAN_REVIEW
Step 9

Run Buildathon simulation:

{
  "size": 100,
  "seed": 42,
  "mode": "FAST_SIMULATION"
}
Step 10

Show:

Revenue At Risk
Simulated Revenue Recovered
Recovery Rate
Rule Decisions
Human Reviews
Retries Stopped
Known Limitations

RecoverAI is currently a Buildathon prototype.

Current limitations include:

no SMS notification yet
no WhatsApp notification yet
no email recovery notification yet
no production authentication/authorization
customer payment retry link is not fully automated
recovery checkout reopening is not fully automated
scheduler currently runs inside the same Spring Boot application
Gemini free-tier quota may limit LIVE_AI demonstration volume
Future Improvements

Future versions can include:

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

Additional improvements:

email recovery notifications
SMS recovery notifications
WhatsApp recovery links
merchant-specific recovery rules
ML-based recovery probability
best-time-to-retry prediction
persistent AI decision history
admin authentication
distributed scheduling
Kafka-based event processing
advanced merchant analytics
Why RecoverAI?

RecoverAI combines:

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

Instead of treating a failed payment as the end of the transaction, RecoverAI treats it as the beginning of an intelligent revenue recovery workflow.
