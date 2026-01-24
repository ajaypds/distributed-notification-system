## Use below commands to create kafka topics

1.  **notification-events**

    ```bash
    kafka-topics --create --topic notification-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
    ```

2.  **notification-events-retry**

    ```bash
    kafka-topics --create --topic notification-events-retry --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
    ```

3.  **notification-events-dlq**

    ```bash
    kafka-topics --create --topic notification-events-dlq --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
    ```
