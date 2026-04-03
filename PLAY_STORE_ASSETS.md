# SplitMate - Play Store Assets & Build Guide

## App Name Suggestions
1. **SplitMate** (Primary)
2. SplitMate - Split Expenses
3. SplitMate: Bill Splitter & Expense Tracker

## App Description (ASO Optimized)

### Short Description (80 chars)
Split expenses easily with friends & groups. Smart debt simplification included!

### Full Description
**SplitMate - The Smartest Way to Split Expenses**

Tired of awkward money conversations with friends? SplitMate makes sharing expenses effortless, whether you're on a trip, sharing an apartment, or dining out with friends.

**KEY FEATURES:**

💰 **Smart Expense Splitting** - Split bills equally, by exact amounts, percentages, or custom shares. SplitMate handles the math so you don't have to.

👥 **Group Management** - Create groups for trips, home, office, or events. Add unlimited members and track every expense.

📊 **Debt Simplification** - Our advanced algorithm minimizes the number of payments needed to settle all debts. Why make 10 payments when 3 will do?

💵 **Settle Up** - Record settlements via Cash, UPI, or Bank Transfer. Partial settlements supported.

📱 **Offline First** - Works without internet. Your data is always available, syncs when online.

🌙 **Dark Mode** - Easy on the eyes with beautiful dark and light themes.

💱 **Multi-Currency** - Support for INR, USD, EUR, GBP, and more.

📦 **Backup & Export** - Export your data as JSON backup or CSV spreadsheets.

📈 **Dashboard** - See your total balances, who you owe, and who owes you at a glance.

🔍 **Search & Filter** - Find any expense or group instantly.

**WHY SPLITMATE?**
✅ 100% Free - No ads, no premium
✅ Privacy First - Your data stays on your device
✅ Clean, Modern UI - Material Design 3
✅ Lightweight - Fast and responsive
✅ Open Source friendly

**PERFECT FOR:**
- Group trips and vacations
- Roommates sharing bills
- Office lunch groups
- Couple expenses
- Event planning

Download SplitMate today and never worry about who owes what again!

## Keywords
expense splitter, split bills, bill divider, group expenses, debt tracker, money manager, roommate bills, trip expenses, expense sharing, splitwise alternative, bill split calculator, IOU tracker, shared expenses, group bill splitter, settle debts

## Category
Finance

## Content Rating
Everyone

## Screenshots Suggestions
1. **Dashboard** - Show balance cards with "You Owe" and "You're Owed" prominently
2. **Group List** - Multiple groups with member counts and icons
3. **Add Expense** - Clean form with split type selector
4. **Group Detail - Balances Tab** - Simplified debts view
5. **Settle Up** - Payment recording screen
6. **Dark Mode** - Dashboard in dark theme
7. **Onboarding** - Welcome screen showing features

## Privacy Policy Template

### Privacy Policy for SplitMate

Last updated: [Date]

**SplitMate** ("we", "our", "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information.

**Information Collection:**
- SplitMate primarily stores data locally on your device
- We collect only the information you voluntarily provide (name, email, expense data)
- When cloud sync is enabled, data is stored securely on Firebase servers

**Data Usage:**
- Your data is used solely to provide expense tracking and splitting functionality
- We do not sell, rent, or share your personal information with third parties
- We do not use your data for advertising purposes

**Data Storage:**
- Local data is stored in an encrypted SQLite database on your device
- Cloud data (if enabled) is stored on Google Firebase with industry-standard encryption
- You can export or delete all your data at any time

**Data Security:**
- We implement appropriate security measures to protect your data
- Local data is protected by your device's built-in security features
- Cloud data is protected by Firebase Security Rules

**Third-Party Services:**
- Google Firebase (optional cloud sync and authentication)
- No analytics or advertising SDKs

**Children's Privacy:**
- SplitMate is not directed to children under 13
- We do not knowingly collect information from children

**Changes to This Policy:**
- We may update this policy from time to time
- Changes will be posted within the app

**Contact:**
- For questions about this policy, contact: [your-email@example.com]

---

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build Steps

1. **Clone / Open the project:**
   ```
   Open the project folder in Android Studio
   ```

2. **Sync Gradle:**
   ```
   Android Studio will automatically sync Gradle files
   Click "Sync Now" if prompted
   ```

3. **Build Debug APK:**
   ```
   ./gradlew assembleDebug
   ```
   APK location: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release APK:**
   ```
   ./gradlew assembleRelease
   ```

5. **Build App Bundle (for Play Store):**
   ```
   ./gradlew bundleRelease
   ```

### Release Signing
Create a `keystore.properties` file in the project root:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=path/to/your/keystore.jks
```

### Firebase Setup (Optional)
1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app with package name `com.splitmate`
3. Download `google-services.json` and place in `app/` directory
4. Uncomment Firebase dependencies in `app/build.gradle.kts`
5. Uncomment `google-services` plugin
