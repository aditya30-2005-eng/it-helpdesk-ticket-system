# 🎧 HelpDesk – IT Ticket Management System

An ITSM-style helpdesk app built with **core Java** (no frameworks, no database) and a modern web dashboard.

## Features
- Create Incidents and Service Requests with priority levels
- Assign tickets and move them through Open → In Progress → Resolved → Closed
- Live dashboard stats, search and status filters
- SLA hours calculated by ticket type and priority
- Data saved automatically in a CSV file

## Java Concepts Used
- **OOP** – abstract `Ticket` class, `Incident` and `ServiceRequest` subclasses (abstraction, inheritance, polymorphism)
- **Collections** – `List`, `Map`
- **Streams & Lambdas** – filtering, sorting, grouping for dashboard stats
- **File I/O** – saving/loading tickets
- Built-in `com.sun.net.httpserver` for the REST API

## Project Structure
```
src/   Ticket, Incident, ServiceRequest, TicketService, Main
web/   index.html, style.css, app.js
```

## Run
Requires JDK 11 or higher.
```
Windows : double-click run.bat
Mac/Linux: ./run.sh
```
Open http://localhost:8080
