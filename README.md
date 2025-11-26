# Order Processing Pipeline

## 📖 Project Overview

A robust, **event‑driven microservices** example that demonstrates an end‑to‑end order processing flow using **Apache Kafka (KRaft mode, no Zookeeper)**, **Spring Boot 3.5.7**, and **Docker**. The solution showcases resilient messaging patterns such as **retryable topics**, **dead‑letter queues (DLQ)**, and real‑time analytics with **Kafka Streams**.

---

## � Project Structure

```text
order-processing-pipeline/
├─ analytics-service/      # Kafka Streams service that aggregates daily spend per customer
├─ event-contracts/        # Shared DTOs / event schemas
├─ infrastructure/         # Docker‑Compose for Kafka (KRaft) & Kafdrop UI
├─ order-service/          # Producer – REST API to create orders
├─ payment-service/        # Consumer – processes payments, retries, DLQ handling
├─ pom.xml                 # Maven parent POM
└─ README.md               # This documentation
```

---

## ✨ Key Features

- **Event‑Driven Architecture** – Services communicate asynchronously via Kafka topics.
- **KRaft Kafka** – Modern Kafka deployment without Zookeeper.
- **Resiliency** – Automatic retries with configurable back‑off and DLQ for poison messages.
- **Real‑Time Analytics** – `analytics‑service` uses Kafka Streams to compute daily spend per customer.
- **Infrastructure as Code** – One‑click Kafka cluster setup via Docker‑Compose.
- **Observability** – Spring Actuator endpoints and Kafdrop UI for topic inspection.

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **JDK** | **Java 21** |
| **Framework** | **Spring Boot 3.5.7** |
| **Messaging** | **Apache Kafka 3.x (KRaft mode)** |
| **Containerisation** | **Docker & Docker‑Compose** |
| **Build** | **Maven** |
| **Testing** | **JUnit 5, Testcontainers** |

---

## ⚙️ Getting Started

### Prerequisites

- **Java 21** (ensure `JAVA_HOME` points to a JDK 21 installation)
- **Docker Desktop** (running)
- **Maven** (`mvn` on the PATH)

### Step‑by‑Step

1. **Start the Kafka infrastructure** (KRaft, no Zookeeper)
   ```bash
   cd infrastructure
   docker-compose up -d
   ```
   This brings up a single‑node Kafka broker and the Kafdrop UI (http://localhost:9000).

2. **Build all modules**
   ```bash
   cd ..   # back to repository root
   mvn clean install
   ```
   Maven compiles the code, runs unit tests, and packages each service.

3. **Run the services** (open three terminals, one per service)
   ```bash
   # Terminal 1 – Order Service
   cd order-service
   mvn spring-boot:run
   ```
   ```bash
   # Terminal 2 – Payment Service
   cd payment-service
   mvn spring-boot:run
   ```
   ```bash
   # Terminal 3 – Analytics Service (optional, for spend queries)
   cd analytics-service
   mvn spring-boot:run
   ```

---

## 🧪 How to Test

1. **Create an Order**
   ```bash
   curl -X POST http://localhost:8080/order/create \
        -H "Content-Type: application/json" \
        -d '{"customerId":"CUST789012","item":"Laptop","price":1200.00}'
   ```
   The order event is published to the `order-events` topic.

2. **Verify Payment Processing**
   - Check the logs of `payment-service`; you should see the order being consumed and the payment simulated.

3. **Query Daily Spend (String endpoint)**
   ```bash
   curl http://localhost:8081/analytics/daily-spend/CUST789012/2025-11-24
   ```
   Expected response:
   ```String
   "Customer CUST789012 spent 1200.0 on 2025-11-24"
   ```

4. **Test Failure & DLQ**
   ```bash
   curl -X POST http://localhost:8080/order/create \
        -H "Content-Type: application/json" \
        -d '{"customerId":"CUST789012","item":"Phone","price":13}'
   ```
   The payment service will retry three times and then move the message to the dead‑letter topic `order-events-topic.DLT`. You can inspect it via Kafdrop:
   ```bash
   open http://localhost:9000
   ```

---

## 🚀 Future Improvements

- **Multi‑node Kafka cluster** with partition & replica configuration.
- **Metrics & Alerting** – Prometheus + Grafana for consumer lag, throughput, error rates.
- **Schema Registry** – Confluent Schema Registry with Avro/Protobuf for strong contract enforcement.
- **Kubernetes Deployment** – Helm charts for services and Kafka.
- **End‑to‑End Test Suite** – Simulate high‑volume event flow using Testcontainers.
- **Security** – TLS encryption and SASL authentication for Kafka communication.

---

## 🤝 Contributing

Contributions are welcome! Fork the repository, create a feature branch, and submit a pull request. Ensure all tests pass and follow the existing code style.

---

*Happy coding!*
