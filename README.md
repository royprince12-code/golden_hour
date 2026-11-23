# 🚨 GoldenHour — Emergency & Road Accident Protection App

> **A Mobile App for Faster Help in Road Accidents and Personal Emergencies**  
> Built with **Java**, **SQLite**, and **Android Motion Sensors**.

---

## 📌 Overview

Road accidents are one of the leading causes of preventable deaths worldwide. The first hour after an accident—often called the **Golden Hour**—decides whether a victim survives. 

**GoldenHour** is a native Android application designed to detect vehicle crashes automatically, send instant location alerts to emergency contacts, provide pre-alerts to hospitals and ambulances, and empower users in personal danger with silent SOS triggers.

---

## ✨ Key Features

### 1. 🏎️ Ride Mode & Real-Time Crash Detection
* **Physical Sensor Sensing**: Uses `SensorManager` and the device's **Accelerometer** (`Sensor.TYPE_ACCELEROMETER`) to continuously monitor impact forces.
* **Impact Spike Rule**: Triggers when resultant acceleration $a = \sqrt{x^2+y^2+z^2}$ exceeds a **$4.0g$ ($39.2 \text{ m/s}^2$)** impact threshold.
* **Background Foreground Service**: Monitored via `CrashDetectionService` with persistent low-power background execution.

### 2. ⏱️ 10-Second Crash Countdown & False Alarm Cancellation
* **Responsiveness Check**: When a crash is detected, a loud alarm & vibration countdown starts.
* **"I AM OK" Cancel Button**: Allows users to cancel false alarms within 10 seconds.
* **Automated Dispatch**: If the victim is unconscious or non-responsive, critical crash alerts are generated automatically.

### 3. 🔓 Over-The-Lock-Screen Medical ID (`setShowWhenLocked`)
* **Emergency Access**: Displays the victim's **Blood Group**, **Allergies**, and **Medical Conditions** directly over the Android Lock Screen without requiring device unlock.
* **Public Lock Screen Notification**: High-priority notification card visible on the lock screen showing critical health summary.

### 4. 🚨 Danger SOS Panic Mode
* **Instant Silent Alert**: One-tap panic trigger logs emergency alerts into SQLite and dispatches simulated SMS & live GPS coordinates to chosen emergency contacts.

### 5. 🎭 Fake Call Safety Tool
* **Unsafe Situation Exit**: Realistic incoming phone call screen with answer/decline controls and call timer to help users leave unsafe environments seamlessly.

### 6. 🚑 Bystander Accident Reporting
* **Witness Dispatch**: Allows bystanders at an accident scene to report location, severity level, and photo notes to dispatch nearby ambulances instantly.

### 7. 📊 Incident History & Responder Tracker
* **Offline SQLite Database**: Local persistence via `DatabaseHelper.java` for Medical ID, Emergency Contacts, and Incident Logs.
* **Live Responder Statuses**: Real-time status tags (*Ambulance Dispatched*, *Hospital Ready*, *Police Notified*).

### 8. 🔒 Dynamic Permissions & Privacy Policy
* **Dynamic Runtime Permissions**: Prompts for `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `POST_NOTIFICATIONS` directly on screen.
* **Transparent Privacy Dialog**: Clear disclosure that Medical ID and location data remain stored locally on device.

---

## 🛠️ Technology Stack

* **Language**: Java 11
* **Target SDK**: Android 36 (Min SDK 24)
* **Local Database**: SQLite (`SQLiteOpenHelper`)
* **UI Architecture**: Material Design 3, ViewBinding, Navigation Component
* **Hardware APIs**: `SensorManager` (`Sensor.TYPE_ACCELEROMETER`), `Vibrator`

---

## 📂 Project Structure

```text
pbl/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/pbl/
│   │   │   ├── db/
│   │   │   │   └── DatabaseHelper.java       # SQLite helper for profiles, contacts & logs
│   │   │   ├── model/
│   │   │   │   ├── UserProfile.java          # Medical ID data model
│   │   │   │   ├── EmergencyContact.java     # Contact data model
│   │   │   │   └── Incident.java             # Emergency incident data model
│   │   │   ├── services/
│   │   │   │   └── CrashDetectionService.java # Background accelerometer sensor service
│   │   │   ├── ui/
│   │   │   │   ├── HomeFragment.java         # Home SOS & Ride Mode UI
│   │   │   │   ├── MedicalIdFragment.java    # Medical profile editor
│   │   │   │   ├── ContactsFragment.java     # Emergency contacts manager
│   │   │   │   ├── IncidentHistoryFragment.java # Responder logs
│   │   │   │   ├── CrashCountdownActivity.java  # Lock screen crash countdown
│   │   │   │   ├── FakeCallActivity.java     # Fake call simulator
│   │   │   │   └── ReportBystanderActivity.java # Bystander dispatch form
│   │   │   └── utils/
│   │   │       └── SosManager.java           # Danger SOS trigger helper
│   │   ├── res/                              # Layouts, themes, navigation graph
│   │   └── AndroidManifest.xml
└── README.md
```

---

## 🚀 Building and Running

### 1. Clone Repository
```bash
git clone https://github.com/royprince12-code/golden_hour.git
cd golden_hour
```

### 2. Build Debug APK via Terminal
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License
This project is developed under the Project Based Learning (PBL) initiative for emergency safety.
