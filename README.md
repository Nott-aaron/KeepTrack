# KeepTrack – AI-Based Receipt Scanning and Warranty Management

**KeepTrack** is an Android application designed to help users digitally manage purchase receipts, product information, and warranty details. It uses Vision AI to extract structured information from receipt and document images, reducing manual data entry and making it easier to maintain purchase records.

The application allows users to capture or select receipt images, extract information using an AI model, review and edit the extracted details, and save confirmed records locally.

---

## 📌 Project Information

| Detail | Information |
|---|---|
| Project Name | KeepTrack |
| Project Type | Android Mobile Application |
| Project Domain | Mobile Application Development / Artificial Intelligence |
| Project Title | AI-Based Receipt Scanning and Warranty Management |
| Group Number | 21 |
| Institution | JAIN (Deemed-to-be University) |
| Development Language | Kotlin |

### Team Members

- **Spencer Aaron Fernandes** – 25MCAR0123
- **Mohith T R** – 25MCAR0227

---

## 🎯 Project Objectives

- Digitize physical purchase receipts and documents.
- Extract product and purchase information using Vision AI.
- Reduce manual data entry and improve record organization.
- Maintain purchase history and warranty information.
- Allow users to review and correct AI-generated information before saving.
- Store records locally on the device.

---

## ✨ Features

### 1. User Authentication
- User registration and login.
- Persistent login sessions.
- Returning users can access the Home screen through an existing valid session.
- Logout functionality.

### 2. AI-Powered Receipt Scanning
- Capture receipt images using the camera.
- Select existing images from the device gallery.
- Process receipt and document images using Vision AI.
- Extract structured information automatically.

### 3. AI-Based Information Extraction

The application extracts the following information where available:

- Store Name
- Product Name
- Category
- Brand
- Model
- Serial Number
- Purchase Date
- Purchase Price
- Warranty Period
- Notes

### 4. Editable AI Results
- Display extracted information in editable fields.
- Allow users to correct incorrect or incomplete information.
- Provide a review step before saving.
- Save records only after the user selects **Confirm & Save**.

### 5. Local Data Storage
- Store application records using Room Database.
- Store original document images locally using FileStorage.
- Preserve saved records and images after logout.
- Avoid creating a database record when a scan is cancelled before confirmation.

### 6. Error Handling
- Loading and preview states during image processing.
- Retry option when extraction encounters an error.
- Manual-entry alternative when AI extraction is unsuccessful.
- User-controlled confirmation before saving.

---

## 🤖 Artificial Intelligence Integration

KeepTrack integrates a pre-trained multimodal Vision AI model through the Groq API.

### AI Model Details

| Component | Details |
|---|---|
| AI Model | `qwen/qwen3.8-27b` |
| AI Provider | Groq |
| API Type | Chat Completions API |
| Endpoint | `https://api.groq.com/openai/v1/chat/completions` |
| Integration | Retrofit and OkHttp |
| Response Processing | Kotlin Serialization |

### Image Processing Workflow

Before sending an image to the AI model, the application performs preprocessing:

1. Save the original image locally.
2. Resize the image so its maximum dimension is 1024 pixels.
3. Convert the image to JPEG format.
4. Compress the image at 80% quality.
5. Encode the processed image using Base64.
6. Send the image as a data URL with extraction instructions to the Groq API.
7. Parse the AI response into structured fields.
8. Display the extracted information for user review.

### AI Integration Workflow

```text
Receipt Image
     |
     v
Camera / Gallery
     |
     v
Save Original Image
     |
     v
Image Preprocessing
     |
     v
Base64 Encoding
     |
     v
Groq API Request
     |
     v
Qwen Vision AI Model
     |
     v
Structured Information Extraction
     |
     v
Editable Review Screen
     |
     v
User Verification
     |
     v
Confirm & Save
     |
     v
Room Database
```

**Important:** AI-generated information may contain errors or missing values. Users should verify the extracted details before saving.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Android Studio | Android application development |
| Jetpack Compose | User interface development |
| Material 3 | UI components and design |
| Room Database | Local database storage |
| Retrofit | API communication |
| OkHttp | HTTP client |
| Kotlin Serialization | JSON response parsing |
| Groq API | AI model integration |
| Qwen Vision AI | Receipt and document information extraction |
| FileStorage | Local storage of original images |

---

## 🏗️ Application Architecture

KeepTrack follows an Android application architecture with separate responsibilities for the user interface, data storage, and API communication.

### Main Components

- **UI Layer:** Handles screens, navigation, user interactions, and editable AI results.
- **Data Layer:** Manages local records and persistence using Room Database.
- **AI Integration Layer:** Handles image preprocessing, API requests, and AI response parsing.
- **File Storage:** Stores original receipt and document images locally.
- **Authentication:** Handles registration, login, session persistence, and logout.

---

## 📱 Application Workflow

1. The user opens KeepTrack.
2. The user logs in or registers.
3. The Home screen provides access to the application's features.
4. The user opens Scan Document.
5. The user captures an image or selects one from the gallery.
6. The application saves the original image and starts preprocessing.
7. The processed image is sent to the Groq API.
8. The AI model extracts the available purchase and product information.
9. The application displays the extracted information in editable fields.
10. The user reviews and corrects the information.
11. The user selects Confirm & Save.
12. The confirmed record is stored in the local Room Database.

If the user cancels before confirmation, no new Room record is created.

---

## 🧪 Testing and Validation

The following testing results were reported during development:

| Test | Reported Result |
|---|---|
| Unit Tests | 9 passed |
| Skipped Tests | 0 |
| Failed Tests | 0 |
| Live API Request | HTTP 200 OK |
| Reported API Response Time | 784 ms |
| Non-Receipt Image Test | Sample ID card identified as an ID card |

The application was tested on a Samsung Android phone.

**Testing limitations:**

- No separate labelled benchmark dataset was reported.
- No formal train/test split was reported.
- No measured accuracy, precision, recall, or F1 score was reported.
- The 784 ms response time represents one reported API request, not a general performance benchmark.

---

## ⚙️ Installation and Setup

### Prerequisites

- Android Studio
- Android SDK
- Kotlin support
- Android device or emulator
- Internet connection for Groq API access
- Groq API credentials

### Steps

**1. Clone the repository**

```bash
git clone https://github.com/YOUR-USERNAME/KeepTrack.git
```

**2. Open the project**

- Launch Android Studio.
- Select **Open**.
- Choose the cloned KeepTrack project folder.
- Allow Gradle synchronization to complete.

**3. Configure Groq API credentials**

- Obtain an API key from the Groq developer platform.
- Configure the key using the project's API configuration mechanism.
- Ensure the key is not committed to GitHub or exposed in public source code.

**4. Build and run**

- Connect an Android device or start an emulator.
- Allow the project to build.
- Run the application from Android Studio.

**Note:** Exact Android SDK, Gradle, and minimum Android version requirements should be checked against the current project configuration.

---

## 🔐 Security and Privacy

- Original receipt images are stored locally on the device.
- Application records are maintained using Room Database.
- Users review AI-generated information before saving.
- API credentials should not be hardcoded or committed to the repository.
- For production deployment, API requests should be routed through a secure backend proxy to protect credentials.

---

## 🚀 Future Enhancements

Potential improvements include:

- Warranty expiry notifications and reminders.
- Advanced search and filtering of saved records.
- Warranty status tracking.
- Cloud backup and synchronization.
- Exporting purchase and warranty records.
- Improved AI extraction validation.
- Formal evaluation using a labelled receipt dataset.
- Secure backend integration for AI API requests.
- Additional document categories and supported formats.

---

## 📂 Repository Structure

The project follows an Android Studio structure. The exact package and file organization may vary with the current implementation.

```text
KeepTrack/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/keeptrack/
│   │       │       ├── ui/
│   │       │       ├── data/
│   │       │       ├── repository/
│   │       │       ├── viewmodel/
│   │       │       └── ...
│   │       │
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 👥 Contributors

**Spencer Aaron Fernandes**  
25MCAR0123

**Mohith T R**  
25MCAR0227

---

## 📄 Academic Project

KeepTrack is developed as part of the Master of Computer Applications (MCA) programme at JAIN (Deemed-to-be University).

**Project Title:** AI-Based Receipt Scanning and Warranty Management in KeepTrack

**Group:** 21

---

*KeepTrack – Making receipt management and warranty tracking simpler through AI.*
