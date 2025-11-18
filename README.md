# HabitFlow — POE Part 3 Documentation

# 📱 HabitFlow — Android Habit Tracker
*A gamified productivity app built with modern Android, Firebase, and a custom REST API.*

![GitHub last commit](https://img.shields.io/github/last-commit/your-user/your-repo?color=blue)
![GitHub repo size](https://img.shields.io/github/repo-size/your-user/your-repo)
![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange?logo=firebase)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-purple?logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 📌 Table of Contents

1. [Introduction](#1-introduction)
2. [System Overview](#2-system-overview)
3. [POE Features & Requirements](#3-poe-part-3-requirements--implementation)
4. [System Architecture](#4-system-architecture)
5. [Screenshots](#📷-screenshots)
6. [Demo Video](#6-demo-video)
7. [Testing & Deployment](#5-testing--deployment)
8. [Conclusion](#7-conclusion)
9. [References](#8-references-harvard-style)

---

## 1. Introduction

**HabitFlow** is a productivity-focused Android mobile application designed to help users build and track daily habits using **gamification**, **real-time feedback**, and **smart progress monitoring**.

This final phase (Part 3) delivers:

- A fully working mobile prototype
- Integration of advanced Android features
- Real-world deployment and demonstration

HabitFlow uses:

- Firebase Authentication
- Android Biometric APIs
- Room Database
- A custom Node.js REST API
- Multi-language resource support

---

## 2. System Overview

HabitFlow allows users to:

- Create, update, and complete daily tasks
- Earn coins & XP (gamified system)
- Sync habits between local storage & cloud
- Log in with Google or Email (SSO)
- Unlock the app using biometrics
- Use the app fully offline
- Auto-sync when back online
- Switch language (English ↔ isiZulu)
- Change notification & biometric settings
- Receive real-time reminders

---

## 3. POE Part 3 Requirements & Implementation

| Requirement | Implementation |
|-------------|----------------|
| **Single Sign-On (SSO)** | ✔ Firebase Auth, Google Sign-In, Email Login, Secure Token Sessions |
| **Biometric Authentication** | ✔ AndroidX BiometricPrompt, Fingerprint unlock, Settings toggle |
| **User Settings Page** | ✔ Language switch, Notification toggle, Biometrics, Clear cache, Profile email |
| **REST API + Database** | ✔ Node.js + Express API, MongoDB Atlas, Retrofit2 integration |
| **Offline Mode w/ Sync** | ✔ RoomDB storage, Sync queue, Auto-sync when online, Network checks |
| **Real-Time Notifications** | ✔ Local notifications, Daily reminders, Android NotificationManager |
| **Multi-Language Support** | ✔ English + isiZulu, `values/` & `values-zu/` strings, instant UI reload |
| **Part 1 Features** | ✔ Task list, XP + Coins, Progress bars, Calendar, Profile, Offline support |

---

### 3.1 Single Sign-On (SSO)

**Includes:**
- Google login
- Email login
- Firebase Authentication
- Token-based sessions

User sees a personalized welcome message.

---

### 3.2 Biometric Authentication

**Uses:**
- Android BiometricPrompt API
- Fingerprint unlock
- Optional toggle in settings

---

### 3.3 User Settings Page

Settings allow users to:

- Change language
- Enable/disable biometrics
- Switch notification preferences
- Clear local cache
- View email profile

Saved using **SharedPreferences**.

---

### 3.4 REST API + Online Database

Backend stack:

- Node.js + Express
- MongoDB Atlas
- Firebase Auth tokens
- Deployed API endpoint

Endpoints:

GET /tasks
POST /tasks
PUT /tasks/:id
DELETE /tasks/:id

Used via **Retrofit2**.

---

### 3.5 Offline Mode With Sync

Offline logic:

- RoomDB stores unsynced tasks
- Saves with `remoteId = null`

When online:

- Syncs tasks to API
- Updates remoteId
- Removes duplicates

---

### 3.6 Real-Time Notifications

Implemented using:

- Android NotificationManager
- Daily habit reminders
- Custom message:
  > “Don’t forget to complete your habits!”

---

### 3.7 Multi-Language Support

Includes:

- English (default)
- isiZulu

Folder structure:

values/strings.xml
values-zu/strings.xml

Language switcher refreshes UI instantly.

---

### 3.8 Delivery of Part 1 Features

All original planned features were completed:

- Gamified task list
- XP, coins, mana & health bars
- Rewards
- Calendar view
- Statistics page
- Offline + API Sync
- User profile

---

## 4. System Architecture

User → App UI → ViewModel → Repository
↑ ↓
Firebase Auth Room Database (offline)
↓ ↓
Biometric Retrofit API Client
↓ ↓
Login →——————> HabitFlow REST API → MongoDB

### Frontend (Android App)

- Kotlin + XML
- MVVM
- ViewModel & LiveData
- RoomDB
- Retrofit2
- Firebase Authentication
- BiometricPrompt API
- Android Notifications
- Multi-language support

### Backend

- Node.js & Express
- MongoDB Atlas
- REST API
- JWT authentication
- JSON I/O

### Architecture Diagram


---

## 5. Testing & Deployment

✔ Local device testing (Samsung & Bluestacks)  
✔ Android Studio debug logs  
✔ MongoDB Atlas cloud testing  
✔ API response validation  
✔ Crash-free builds

---

## 6. Demo Video

📺 **Full Video Demo:**  
https://youtu.be/8YpgJjRzKDs

### The video includes:

- Google Login
- Biometric unlock
- Task CRUD
- XP + Coin rewards
- Offline behavior
- Automatic sync
- API & DB verification
- Language switching
- Notification popup

---

## 7. Conclusion

All POE Part 3 requirements were successfully met.

**HabitFlow is now a production-ready habit tracker with:**

- Secure authentication & biometrics
- Offline-first architecture
- REST API integration
- Real-time notifications
- Multi-language UI
- XP, coins & gamification
- Clean modern Android UI/UX

---

## 8. References (Harvard Style)

Android Developers (2024) *Guide to App Architecture*.  
Available at: https://developer.android.com/topic/architecture  
(Accessed 7 Nov 2025).

Firebase (2024) *Authentication Documentation*.  
Available at: https://firebase.google.com/docs/auth  
(Accessed 7 Nov 2025).

JetBrains (2024) *Kotlin Language Documentation*.  
Available at: https://kotlinlang.org/docs/home.html  
(Accessed 7 Nov 2025).

Square (2024) *Retrofit: Type-safe HTTP Client*.  
Available at: https://square.github.io/retrofit/  
(Accessed 7 Nov 2025).

MongoDB Atlas (2024) *Getting Started*.  
Available at: https://www.mongodb.com/atlas  
(Accessed 7 Nov 2025).

Google Material Design (2024) *Material 3 Guidelines*.  
Available at: https://m3.material.io  
(Accessed 7 Nov 2025).

Android Developers (2025) *Biometric Authentication Guide*.  
Available at: https://developer.android.com/training/sign-in/biometric-auth  
(Accessed 8 Nov 2025).
