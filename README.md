-----

# Order Processing Pipeline

A robust, event-driven microservices architecture demonstrating an order processing flow using **Apache Kafka**, **Spring Boot**, and **Docker**. This project implements resilient messaging patterns including **Retry Logic** and **Dead Letter Queues (DLQ)** to handle distributed transactions reliably.

## 🚀 Project Structure

The repository is organized into the following modules:

  * **`order-service`**: The **Producer** service. It exposes REST endpoints to create orders and publishes order events to Kafka.
  * **`payment-service`**: The **Consumer** service. It listens for order events, processes payments, and handles failures with retry mechanisms.
  * **`event-contracts`**: A shared library containing event schemas/DTOs to ensure consistency between services.
  * **`infrastructure`**: Contains Docker Compose configurations to spin up the Kafka ecosystem (Zookeeper, Kafka Broker, etc.).

## ✨ Key Features

  * **Event-Driven Architecture**: Decoupled services communicating asynchronously via Kafka.
  * **Resiliency**: Implements **Retryable Topics** to handle transient failures automatically.
  * **Fault Tolerance**: configuring **Dead Letter Queues (DLQ)** for poison pill messages that cannot be processed after max retries.
  * **Infrastructure as Code**: Complete Kafka environment setup using Docker Compose.

## 🛠️ Tech Stack

  * **Language**: Java 17+
  * **Framework**: Spring Boot 3.x
  * **Messaging**: Apache Kafka
  * **Containerization**: Docker & Docker Compose
  * **Build Tool**: Maven

## ⚙️ Getting Started

### Used Tools

  * Java 21
  * Docker Desktop installed and running
  * Maven

### 1\. Start the Infrastructure

Navigate to the infrastructure folder and start Kafka:

```bash
cd infrastructure
docker-compose up -d
```

### 2\. Build the Project

From the root directory, build all modules:

```bash
mvn clean install
```

### 3\. Run the Services

**Run Order Service (Terminal 1):**

```bash
cd order-service
mvn spring-boot:run
```

**Run Payment Service (Terminal 2):**

```bash
cd payment-service
mvn spring-boot:run
```

## 🧪 How to Test

1.  **Place an Order**: Send a POST request to the Order Service.

    ```bash
    curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d '{"orderId": "101", "item": "Laptop", "price": 1200.00}'
    ```

2.  **Verify Processing**: Check the logs of the `payment-service`. You should see the order being consumed and processed.

3.  **Test Error Handling**: Send an invalid order (price=13) to observe the **Retry** attempts and eventual move to the **DLQ**.

     - 3 retry attempts
  
     - Then the message lands in order-events-topic.DLT
  
     - You can consume the DLT topic to inspect the failed event


## 🧠 Future Enhancements

    - Add topic partition / replica configuration for multi-node Kafka cluster
    
    - Add consumer lag monitoring, metrics, alerting
    
    - Add schema registry (e.g., Confluent Schema Registry) and Avro/Protobuf for event contracts
    
    - Add Kubernetes deployment for services and Kafka cluster
    
    - Add end-to-end test suite to simulate high-volume event flow


## 🤝 Contributing

Contributions are welcome\! Please feel free to submit a Pull Request.
