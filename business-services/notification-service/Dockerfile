# ========================================
# Dockerfile for Notification Service
# ========================================
#
# This Dockerfile containerizes the Notification Service microservice
#
# SERVICE PURPOSE:
# - Asynchronously sends notifications (email, SMS, push)
# - Order confirmation emails to customers
# - Order status update notifications
# - Tracks notification delivery status
# - Consumers for Kafka order events
# - Integrate with mail providers (MailHog for dev, SMTP for prod)
#
# RUNTIME PORT: 8084
#
# DEPENDENCIES (at runtime):
# - MySQL (notification_service database)
# - Kafka broker (event messaging)
# - Eureka Discovery Server (service registration)
# - SMTP Server or MailHog (email sending)
#
# BUILD COMMAND:
# docker build -t online-shopping/notification-service:0.0.1-SNAPSHOT .
#
# RUN COMMAND (with MailHog for testing):
# docker run -e NOTIFICATION_DATASOURCE_URL=jdbc:mysql://localhost:3306/notification_service \
#            -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
#            -e MAIL_HOST=localhost \
#            -e MAIL_PORT=1025 \
#            -e EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka \
#            -p 8084:8084 \
#            online-shopping/notification-service:0.0.1-SNAPSHOT
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8084
# ========================================
# Port 8084: Notification Service REST API
# Accessible at: http://localhost:8084/api/notifications
# Or via API Gateway: http://localhost:8080/api/notifications
#
# Key endpoints:
# POST /api/notifications/email - Send email
# POST /api/notifications/sms - Send SMS
# POST /api/notifications/push - Send push notification
# GET /api/notifications - List sent notifications
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
