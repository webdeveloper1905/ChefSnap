# 🍳 ChefSnap - AI-Powered Recipe & Ingredient Scanner

ChefSnap is an intelligent native Android application designed to transform everyday cooking. By leveraging the multimodal capabilities of the **Gemini AI API**, the app analyzes ingredients via camera snapshots or gallery uploads to generate personalized, step-by-step recipes, nutritional estimates, and cooking instructions in real-time.

---

## 📱 App Previews

| Ingredient Scanner | AI Recipe Generation | Subscription & Paywall |
|:---:|:---:|:---:|
| <img src="screenshots/scan.png" width="240" alt="Scanner Screen"/> | <img src="screenshots/recipe.png" width="240" alt="Recipe Screen"/> | <img src="screenshots/paywall.png" width="240" alt="Paywall Screen"/> |

*(Tip: Add your actual app screenshots into a `screenshots/` directory in the repository to display them here.)*

---

## ✨ Key Features

* **Visual Ingredient Recognition:** Capture photos of pantry items or ingredients to automatically recognize food items.
* **Generative Recipe Creation:** Powered by the **Gemini API** to produce creative recipes based strictly on available ingredients and dietary preferences.
* **Pro Tier & Monetization:** Complete in-app subscription flow integrated seamlessly with **RevenueCat** and **Google Play Billing**.
* **Clean & Modern UI:** Built following Android modern UI guidelines, offering fluid navigation and accessibility.
* **Offline Fallbacks & State Management:** Robust handling of loading states, network errors, and edge-case AI outputs.

---

## 🏗️ Architecture & Tech Stack

This project strictly adheres to modern Android development best practices, featuring an offline-aware and testable architecture.

* **Language:** Kotlin (100%)
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles
* **UI Framework:** Jetpack Components / ViewBinding & Material Design 3
* **Asynchronous Programming:** Kotlin Coroutines & Flow
* **Artificial Intelligence:** Google Generative AI SDK (Gemini API)
* **In-App Purchases:** RevenueCat Android SDK & Google Play Billing
* **Networking & Parsing:** Retrofit2 / OkHttp3 & Gson / Kotlinx Serialization

---

## 🚀 Getting Started

### Prerequisites

* Android Studio Ladybug | 2024.2+ or newer
* JDK 17+
* Android SDK 24+

### Setup & API Keys

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/webdeveloper1905/ChefSnap.git](https://github.com/webdeveloper1905/ChefSnap.git)
   cd ChefSnap
   Configure API Keys:
For security reasons, production credentials are not tracked in version control. Configure your keys before building the app:

Gemini API: Obtain a free key from Google AI Studio and place it inside the configuration class or local.properties:

Properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
RevenueCat: Add your public SDK key from the RevenueCat Dashboard:

Properties
REVENUECAT_PUBLIC_KEY=YOUR_REVENUECAT_PUBLIC_KEY_HERE
Build & Run:
Sync the Gradle project in Android Studio and select your physical device or emulator to run.

🔒 Security & Privacy
Sensitive Data Redaction: API credentials and signing keys are isolated from public version control.

Data Handling: Images uploaded for ingredient scanning are processed in real-time and are never stored on private servers.

👨‍💻 Developer
Dırgan Özan

GitHub: @webdeveloper1905
