# Rabbithole Web Application

<img width="1469" height="907" alt="image" src="https://github.com/user-attachments/assets/367da709-68ff-49be-a288-861ec0c8ab74" />


This project is a Spring MVC and Jakarta EE based web application for messaging with RabbitMQ integration.

## Usage

- Run the docker compose via ```docker compose up -d``` so you have a rabbit running locally for testing
- Adjust configuration at application.properties file if needed
- Create the wanted exchanges, queues or streams inside rabbit (user:guest password:guest)
- Run the application on a local server (default port 8080)
- Navigate to the homepage localhost:8080
- Send, receive, and manage messages through the UI

## Project Structure

- `src/main/java/de/gnubis/rabbithole/` — Java source files implementing controllers and messaging logic
- `src/main/resources/static/css/` — CSS styles including updated style sheets
- `src/main/resources/static/images/` — Application images, including the logo
- `src/main/resources/templates/` — Thymeleaf HTML templates for the frontend UI

