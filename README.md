# FoodPanda Rider (Android — Kotlin & Jetpack Compose)

A native Android Rider delivery companion application upgraded from Flutter to modern **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, **Navigation Compose**, and **Gradle 9.3.1 / AGP 9.1.1**.

## Features
- **Rider Authentication & Account Management**: Sign in, register with real-time password strength validation, email verification workflow, quick demo rider login, and admin approval status screen (`HomeScreenNoApprove`).
- **Order Dispatch & Management (`HomeScreen`)**: Browse available incoming orders (`All Order`) and active accepted deliveries (`Current Order`), inspect itemized food orders and customer instructions, or simulate new incoming orders.
- **Interactive Live Delivery Map (`MapScreen`)**: Interactive zoomable/pannable map with route polylines, custom Shop/Rider/Customer markers, Haversine distance calculation (`km`), 10-second live tracking updates, and two-stage `Pick Up` -> `Delivered` workflow (unlocking delivery within 0.5 km of the customer).
- **Delivery History & Earnings (`OrderHistoryScreen` & `FinishDeliverScreen`)**: Persistent Room database storing completed deliveries, rider earnings summary, and ratings.
