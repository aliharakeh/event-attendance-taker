# 📝 AttendanceTaker

A comprehensive Android application for managing and tracking attendance for events, meetings, classes, and group activities. Built with Jetpack Compose and Room database for a modern, efficient, and user-friendly experience.

## ✨ Features

### 👥 Contact Management

-   **Add, Edit, Delete Contacts**: Manage your attendee database with names and phone numbers
-   **Contact Groups**: Organize contacts into logical groups (e.g., "Youth Group", "Board Members", "Team A")
-   **Search & Filter**: Quickly find contacts and groups with real-time search functionality

### 📅 Event Management

-   **One-time Events**: Create events for specific dates and times
-   **Recurring Events**: Set up weekly recurring events with customizable start and end dates
-   **Event Templates**: Manage recurring event templates that automatically generate events
-   **Event History**: View and search through past events with date range filtering
-   **Flexible Scheduling**: Support for both fixed and recurring event patterns

### 📋 Attendance Tracking

-   **Visual Attendance**: Simple toggle switches to mark present/absent for each attendee
-   **Notes System**: Add detailed notes for each person's attendance record
-   **Group-based Tracking**: Assign specific contact groups to events for organized attendance management
-   **Real-time Updates**: Changes are saved immediately with no data loss

### 📱 Communication Integration

-   **WhatsApp Integration**: Direct messaging to attendees from the attendance screen
-   **Phone Call Integration**: One-tap calling functionality for quick contact
-   **Contact Actions**: Seamless communication without leaving the app

### 🌐 Multi-language Support

-   **English & Arabic**: Full localization support with easy language switching
-   **RTL Support**: Proper right-to-left layout support for Arabic users
-   **Cultural Adaptation**: UI elements adapted for different language orientations

### 🔍 Advanced Features

-   **Smart Search**: Search across events, contacts, and groups
-   **Date Filtering**: Filter events by custom date ranges
-   **Modern UI**: Material Design 3 with dynamic theming
-   **Offline First**: Full functionality without internet connection
-   **Data Persistence**: Reliable local storage with Room database

## 🎯 Use Cases

### Religious Organizations

-   Track attendance for prayer services, religious classes, or community events
-   Manage different groups (youth, adults, volunteers)
-   Communicate with members for event reminders

### Educational Institutions

-   Monitor student attendance for classes or extracurricular activities
-   Organize students by classes or groups
-   Contact parents directly from the attendance system

### Sports & Fitness

-   Track attendance for training sessions, matches, or tournaments
-   Manage different teams or age groups
-   Coordinate with players and parents

### Corporate & Business

-   Monitor meeting attendance
-   Track training session participation
-   Manage employee groups and departments

### Community Organizations

-   Track volunteer participation
-   Monitor event attendance
-   Organize members by committees or roles

### Event Management

-   Monitor attendee participation for conferences or workshops
-   Track RSVPs and actual attendance
-   Manage different attendee categories

## 🏗️ Technical Architecture

### Built With

-   **Kotlin** - Modern Android development language
-   **Jetpack Compose** - Declarative UI toolkit
-   **Room Database** - Local data persistence
-   **Material Design 3** - Modern UI components
-   **Coroutines & Flow** - Asynchronous programming
-   **Navigation Component** - Screen navigation management

### Database Schema

-   **Contacts**: Store attendee information (name, phone number)
-   **Contact Groups**: Organize contacts into logical groups
-   **Events**: Manage both one-time and recurring events
-   **Attendance Records**: Track presence/absence with notes

### Key Components

-   **AttendanceRepository**: Central data management layer
-   **RecurringEventManager**: Automated recurring event generation
-   **LanguageManager**: Multi-language support system

## 📱 App Structure

### Main Screens

1. **Events Screen**: View, create, and manage events
2. **Contacts Screen**: Manage contact groups and members
3. **Attendance Screen**: Track attendance for specific events
4. **Event History**: Browse past events with filtering
5. **Recurring Templates**: Manage recurring event patterns
6. **Event Edit**: Create and modify events with group assignments

### Navigation Flow

-   **Bottom Navigation**: Quick access to Events and Contacts
-   **Deep Linking**: Direct navigation to specific attendance screens
-   **Intuitive UI**: Clear visual hierarchy and user-friendly interactions

## 🚀 Getting Started

### Prerequisites

-   Android Studio (latest version)
-   Android SDK (API level 26+)
-   Kotlin support

### Installation

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device or emulator

### First Time Setup

1. **Add Contact Groups**: Create groups to organize your attendees
2. **Add Contacts**: Populate groups with attendee information
3. **Create Events**: Set up your first event and assign contact groups
4. **Take Attendance**: Start tracking attendance with the intuitive interface

## 📊 Data Management

### Local Storage

-   All data stored locally using Room database
-   No internet connection required for core functionality
-   Automatic data migrations for app updates

### Data Export

-   Contact integration with device phone book
-   Communication history maintained within the app
-   Attendance records preserved with full history

### Privacy & Security

-   All data remains on device
-   No external data transmission
-   User controls all personal information

## 🔄 Recurring Events

### Automatic Generation

-   Recurring events automatically create new instances
-   Smart scheduling prevents duplicate events
-   Configurable start and end dates for recurring patterns

### Template Management

-   Create reusable event templates
-   Modify recurring patterns without affecting past events
-   Flexible scheduling for various organizational needs

## 🌟 Benefits

### For Organizations

-   **Streamlined Attendance**: Reduce administrative overhead
-   **Better Communication**: Integrated contact capabilities
-   **Historical Tracking**: Maintain comprehensive attendance records
-   **Group Management**: Organize attendees efficiently

### For Administrators

-   **Time Saving**: Quick attendance marking with bulk operations
-   **Accurate Records**: Digital tracking eliminates manual errors
-   **Easy Access**: Mobile-first design for on-the-go management
-   **Flexible Setup**: Adapt to various organizational structures

### For Members

-   **Quick Contact**: Easy communication channels
-   **Transparent Tracking**: Clear attendance visibility
-   **Multi-language**: Accessible in preferred language

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**AttendanceTaker** - Making attendance management simple, efficient, and accessible for everyone.
