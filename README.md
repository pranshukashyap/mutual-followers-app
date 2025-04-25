# Mutual Followers Spring Boot App

This is a Spring Boot application built for the **Bajaj Finserv Health Programming Challenge**.  
It automatically triggers an API interaction on startup, processes mutual follower data, and posts the result to a webhook.

---

## 🚀 Features

- Auto-executes at application startup
- Calls `/generateWebhook` endpoint
- Parses follower relationships
- Identifies mutual (2-way) follower pairs
- Posts the output to a webhook with JWT token
- Includes retry mechanism (up to 4 times)

---

## 📦 Project Structure

- `MutualFollowersApp.java` – Main Spring Boot entry
- `WebhookRunner.java` – Handles logic on startup
- `User`, `InitResponse`, `InitData` – Models for JSON parsing

---

## 🔧 Tech Stack

- Java 17+
- Spring Boot
- Maven
- RestTemplate
- Jackson
- Lombok

---

## 🧪 Run Locally

### 1. Clone the repo

```bash
git clone https://github.com/yourusername/mutual-followers-app.git
cd mutual-followers-app
