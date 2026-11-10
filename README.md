# SmartStore Manager

**A modern, fully offline Android application for grocery and general store management.**

SmartStore Manager is designed to simplify everyday store operations, including sales billing, product management, inventory tracking, purchases, expenses, and business reports — all from one Android application.

The app focuses on simplicity, speed, privacy, and reliable local data storage.

## Features

### Dashboard
- Today's sales and profit overview.
- Total transactions.
- Inventory valuation.
- Low-stock alerts.
- Recent sales and business analytics.

### Sales & POS Billing
- Quick and easy product billing.
- Add multiple products to a cart.
- Automatic subtotal and discount calculations.
- Cash, UPI, and Card payment support.
- Receipt generation and reprinting.

### Product & Inventory Management
- Add, edit, and manage products.
- Track purchase and selling prices.
- Manage stock quantities and units.
- Barcode support.
- Low-stock alerts.
- Stock movement history.

### Purchases & Suppliers
- Record product purchases.
- Manage supplier information.
- Track purchase history.
- Monitor paid and outstanding amounts.
- Automatically update inventory.

### Expense Management
- Record daily business expenses.
- Categorize expenses.
- Track payment methods.
- View expense history.

### Reports & Analytics
- Daily, weekly, and monthly sales reports.
- Gross profit and net profit.
- Expense and purchase reports.
- Inventory valuation.
- Best-selling products.
- PDF and CSV export.

### Receipt Printing
- 58mm thermal receipt support.
- A4 receipt support.
- Receipt preview.
- Printing and PDF generation.

### Offline Data & Backup
- Fully offline functionality.
- Local database storage.
- No cloud database.
- Manual backup and restore.
- Local data protection.

### Modern UI
- Clean and modern Android interface.
- Light Mode and Dark Mode.
- Easy bottom navigation.
- Responsive layouts.
- User-friendly controls.

## Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android development |
| Jetpack Compose | User interface |
| Material 3 | UI components |
| Room Database | Local data storage |
| SQLite | Database engine |
| Kotlin Coroutines | Asynchronous operations |
| DataStore | App preferences |
| MVVM | Application architecture |

## Privacy & Offline Operation

SmartStore Manager is designed with an offline-first approach.

- All business records are stored locally on the device.
- No Firebase or cloud database.
- No automatic synchronization between devices.
- No employee login or accounts.
- No customer database.
- No GST functionality.
- No internet connection required for normal store operations.

Each device maintains its own separate database.

## Platform

- Android
- Designed for grocery and general stores.
- Currency: Indian Rupees (INR).

## Getting Started

To build the project:

1. Clone this repository:

   ```bash
   git clone https://github.com/YOUR-USERNAME/SmartStore-Manager.git
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronize dependencies.
4. Connect an Android device or start an emulator.
5. Build and run the application.

### Generate APK

1. Open Android Studio.
2. Select **Build → Generate App Bundles or APKs → Generate APKs**.
3. Follow the build instructions.
4. Find the generated APK in the project's build output directory.

## Project Modules

- Dashboard
- Sales & POS
- Products
- Inventory
- Purchases
- Suppliers
- Expenses
- Cash Register
- Sales Returns
- Reports
- Receipt Printing
- Backup & Restore
- Settings

## Future Improvements

- Improved barcode scanning.
- More thermal printer compatibility.
- Advanced sales analytics.
- Better inventory forecasting.
- Additional backup options.
- UI and performance improvements.

## Developer

**Shubham Kumar Gupta**

Developed as an Android application project focused on simplifying everyday grocery and general store management.

## License

This project is currently available for personal and educational development.

A formal open-source license can be added when the project is ready for public distribution.
