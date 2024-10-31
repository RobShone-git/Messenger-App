# VoIP & Text Messaging System

## Introduction

This project is a comprehensive Voice over IP (VoIP) and text messaging application built on a client-server model. It allows multiple users to connect, communicate via text and voice, initiate private or group calls, and exchange voice notes in real-time. The system consists of a **server** to manage user sessions and activities, and **client applications** to facilitate user interactions through a graphical interface.

Designed for simplicity and efficiency, this VoIP & Text Messaging System simulates key functionalities of modern communication platforms, including real-time voice transmission, group messaging, and user activity updates.

## Features

### Server

The server is responsible for managing client connections and coordinating communication activities. Key features include:

- **Multi-Client Support**: Allows multiple users to connect and communicate simultaneously.
- **Session Management**: Manages user connection requests, session initiation for voice calls, and updates for the list of active users.
- **Request Handling**: Handles various client requests, including voice call initiation and text messaging between users.
- **Multi-Threading**: Ensures a smooth experience by managing each client on a separate thread.
- **Safe Connection Management**: Enables clients to connect and disconnect without disrupting other users.
- **Minimal GUI**: A simple server interface displays client activity and current online users for monitoring purposes.

### Client

The client provides a user-friendly interface to interact with other users. Clients can initiate text chats, voice calls, join group calls, and send voice notes, with features such as:

- **User Authentication**: Clients connect with unique usernames or IP addresses, managed by the server.
- **Real-Time Voice Calls**: Allows one-on-one voice calls with real-time audio quality.
- **Group Calls and Chats**: Supports multiple users in global voice call and global text chat channel.
- **Voice Notes**: Users can send and play pre-recorded voice messages directly from the interface.
- **Online User List**: A GUI-based list of currently connected users, updated in real-time, allowing users to initiate calls or messages.
- **Concurrent Messaging**: Allows users to send and receive messages while performing other actions, such as typing or joining calls.
- **User-Friendly GUI**: A simple and intuitive interface that includes:
  - Main window for logging events and displaying messages.
  - Real-time list of online users.
  - Interactive elements for call initiation and text messaging.

### Communication Protocols and Quality

- **Real-Time Audio with UDP**: Real-time voice calls are facilitated over the UDP protocol to reduce latency, providing an optimal user experience.
- **Message Delivery with TCP**: Voice notes and other non-time-sensitive messages are sent over TCP to ensure reliable delivery.
- **High Audio Quality**: Voice communication is configured to use 8000 samples per second, 16-bit sample size, and mono sound to ensure clear, high-quality audio.
- **Error Handling and Recovery**: The system is resilient to unexpected disconnections, ensuring that users can safely reconnect without disrupting others.
