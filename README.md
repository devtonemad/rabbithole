# Rabbithole Web Application

This project is a Spring MVC and Jakarta EE based web application for messaging with RabbitMQ integration.

## Header Style Adjustment

The header design was updated to better align with the new logo. Changes include:

- Background color changed to a warm brown (`#7b4f24`)
- Header text color updated to carrot orange (`#ff9800`)
- Table header background color also updated to brown with white text for better contrast

These changes improve visual cohesion with the logo and enhance the user interface aesthetics.

## Usage

- Run the application on a local server (default port 8080)
- Navigate to the homepage where the updated header style is visible
- Send, receive, and manage messages through the UI

## Project Structure

- `src/main/java/de/gnubis/rabbithole/` — Java source files implementing controllers and messaging logic
- `src/main/resources/static/css/` — CSS styles including updated style sheets
- `src/main/resources/static/images/` — Application images, including the logo
- `src/main/resources/templates/` — Thymeleaf HTML templates for the frontend UI

## How to Customize Header Styles

Modify the `header` CSS selector in `src/main/resources/static/css/style.css`:
