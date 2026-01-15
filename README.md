# 🌱 DAVE — Plant Care Mobile App

DAVE is an Android mobile application that helps users manage their personal plant collection, learn about plants, and track basic care needs.  
The app combines an external plant database (Perenual API) with personal user data stored in Firebase.

---

## ✨ Features

### 🔐 Authentication
- Sign in with Firebase Authentication
- Secure user session
- Password update from account screen
- Logout

### 🏡 Home
- Display the list of plants added by the user
- Data loaded from Firestore (user collection)
- Access to plant details
- Quick navigation with bottom navigation bar

### ➕ Add a Plant
- Search plant by name using **Perenual API**
- Auto-fill plant information (scientific name, care, image, etc.)
- Optional nickname (custom name)
- Save plant to user's Firestore collection

### 🌿 Plant Details
- Combine:
    - Plant catalog data from Perenual
    - Personal data from Firestore (nickname, notes, tasks)
- Modify notes and care info
- Delete plant from collection

### 👤 Account
- Display user profile info
- Edit display name
- Change password
- Logout

---

## 🧱 Architecture

The project follows a simplified **MVVM architecture**:
```bash
UI (Jetpack Compose Screens)
↓
ViewModels (State & business logic)
↓
Repositories
↓
Data Sources (Firebase / Perenual API)
```

### Layers

- **UI**
    - Jetpack Compose screens
    - Navigation with NavController
    - Stateless UI observing ViewModel state

- **ViewModels**
    - Handle screen logic
    - Expose StateFlow / LiveData
    - Call repositories

- **Repositories**
    - Abstract data sources
    - Decide whether to call API or Firebase

- **Data Sources**
    - Firebase Authentication
    - Cloud Firestore
    - Perenual REST API (Retrofit)

---

## 🔥 Data Sources

### Firebase
- **Authentication**
    - Login / Logout
    - Update password
- **Firestore**
    - `users/{uid}/plants`
    - Stores:
        - Perenual plant ID
        - Nickname
        - Notes
        - Care tasks

### Perenual API
- Plant search by name
- Plant details (scientific name, care info, images)

**Used to:**
- Auto-fill plant form when adding
- Display catalog information in details view

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM
- **Build system:** Gradle
- **Backend:** Firebase
    - Authentication
    - Firestore
- **Networking:** Retrofit + OkHttp
- **API:** Perenual Plant API

---

## 🚀 Setup

### 1. Clone the project

```bash
git clone <repo-url>
```

### 2. Firebase configuration
- Create a Firebase project
- Enable:
  - Authentication (Email/Password)
  - Firestore Database
- Download google-services.json
- Place it in /app

### 3. Perenual API Key
- Create an account on https://perenual.com/
- Get your API key
- Add it to your gradle.properties
Example:
```bash
PERENUAL_API_KEY=your_api_key_here
```
And access via BuildConfig in Retrofit.

---
# 📌 Possible Improvements
- Offline cache (Room / DataStore)
- Plant reminders & notifications
- Image upload for user plants
- Better search filters
- Statistics & plant health tracking

---
# 👩‍💻 Authors
Project developed as part of a mobile development and data-oriented learning project, by Maëna A., Rozenn R. Éloïse L.

--- 
# 🌍 Why DAVE?
Because plants deserve a digital friend too 🌿

