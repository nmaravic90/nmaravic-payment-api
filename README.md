
> ⚠️ This project is currently under active development.

# Payment API Service 💳
REST API for payment processing including fund transfers, bill payments, parking, and QR code payments.
Built with Spring Boot 4.x, PostgreSQL, and OpenAPI contract-first approach. Includes idempotency support and pessimistic locking to prevent duplicate charges and race conditions.

## 📊 Status
[![CI Pipeline](https://github.com/nmaravic90/nmaravic-payment-api/actions/workflows/ci.yml/badge.svg)](https://github.com/nmaravic90/nmaravic-payment-api/actions/workflows/ci.yml)

## 🛠️ Technologies
![Java](https://img.shields.io/badge/Java_26-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=openapiinitiative&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

## 🚀 Features

- **Contract-First Development:** API defined and generated via OpenAPI 3.0 spec.
- **Idempotency Support:** Prevents duplicate charges using client-generated UUID keys.
- **Race Condition Protection:** Pessimistic locking on balance deduction to handle concurrent requests.
- **Factory Pattern:** Clean and extensible payment service architecture.
- **Payment Types:** Fund transfers, utility bill payments, parking payments, and QR code payments.
- **Database Migrations:** Versioned schema management via Flyway.

Author: Nikola Maravić
