# HikeAlong

HikeAlong is a full-stack web application for managing hiking events and reservations.

The application allows users to browse hiking events, search and filter them, make reservations, and receive reservation status updates. Organizers can create and manage their own events and accept or reject reservation requests. Admin users can manage the application data and user roles.

This project was developed for the Software Design course.

---

## Student

**Student:** Butuza Raisa Briana  
**Subject:** Software Design  

---

## Project Overview

HikeAlong is an event reservation platform built around three main entities:

- **Person**: represents an application user.
- **Event**: represents a hiking event created by an organizer.
- **Reservation**: connects a user with an event and stores the reservation status.

The application supports different user roles:

- **USER**
  - browses events;
  - searches and filters events;
  - makes reservations;
  - views and edits own reservations;
  - edits own profile;
  - receives reservation notifications.

- **ORGANIZER**
  - inherits basic user functionality;
  - creates own events;
  - edits own events;
  - deletes own events;
  - views reservations for own events;
  - accepts or rejects reservation requests.

- **ADMIN**
  - can manage users;
  - can manage events;
  - can manage reservations;
  - can change user roles.

---

## Main Features

### Authentication

The application includes a login system where users authenticate using their email and password.

After login, users are redirected based on their role.

Example role-based redirects:

- `USER` → event feed
- `ORGANIZER` → organizer event/reservation pages
- `ADMIN` → admin management pages

---

### User Management

Users can:

- create an account;
- log in;
- edit their profile;
- recover their password by email;
- have a specific role assigned.

The system validates user data such as:

- name;
- email;
- password strength;
- role.

Duplicate email addresses are not allowed.

---

### Event Management

Organizers can manage hiking events.

Each event contains:

- title;
- description;
- location;
- date;
- maximum number of participants;
- organizer.

Organizers can:

- create events;
- view their own events;
- edit event details;
- delete events.

The application validates event data, including title, location, date, and capacity.

---

### Event Browsing, Search, Filter and Sort

Users can browse the event feed and search for relevant hiking events.

The event feed supports:

- searching events by title;
- filtering events by location;
- filtering upcoming events;
- sorting events by date;
- viewing event details;
- making reservations.

The `Event` entity was chosen for search, filtering, and sorting because it is the main entity users interact with.

---

### Reservation Management

Users can make reservations for events.

A reservation contains:

- user;
- event;
- number of reserved spots;
- reservation status.

Reservation statuses:

- `PENDING`
- `ACCEPTED`
- `DECLINED`

When a user creates a reservation, it is initially saved as `PENDING`.

Organizers can later accept or reject reservation requests.

---

### WebSocket Notifications

The project includes a WebSocket notification feature for reservation status updates.

The notification flow is:

1. A user creates a reservation.
2. The reservation is saved as `PENDING`.
3. The organizer accepts or rejects the reservation.
4. The backend updates the reservation status.
5. The backend sends a WebSocket notification.
6. The frontend receives the notification.
7. A toast message is displayed to the user.

Example messages:

```text
Your reservation was accepted.
Your reservation was rejected.
A reservation status has been updated.
