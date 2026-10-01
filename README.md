# 💊 MedicScan — Scanneur de Médicaments & Gestionnaire d'Ordonnances

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room DB](https://img.shields.io/badge/Database-Room%20SQLite-00599C?style=for-the-badge)
![API](https://img.shields.io/badge/API-BDPM%20(Open%20Data)-FF6F00?style=for-the-badge)

**MedicScan** est une application Android moderne et ergonomique développée avec **Jetpack Compose** et **Material 3**. Elle permet de scanner les codes **DataMatrix GS1** des boîtes de médicaments, de consulter leurs fiches d'informations complètes via l'API publique de la Base de Données Publique des Médicaments (BDPM), de numériser des **ordonnances papier ou PDF** (OCR ML Kit) et de programmer des **rappels de pharmacie**.

---

## ✨ Fonctionnalités Principales

### 1. 🔍 Scan de Codes DataMatrix GS1
- **Décodage en temps réel** via CameraX et ML Kit Barcode Scanning.
- **Extraction automatique du code CIP13** / GTIN-14 (identifiant GS1 `01`).
- **Lecture des métadonnées de la boîte** : Numéro de lot (`AI 10`) et Date de péremption (`AI 17`).
- **Bouton Flash / Torche** réactif et mode de **saisie manuelle du code CIP**.

### 2. 📋 Informations Médicamenteuses Synthétiques (API BDPM)
- Connexion en temps réel à l'API publique [`medicaments-api.giygas.dev`](https://medicaments-api.giygas.dev/).
- **Fiche synthétique Material 3 (BottomSheet)** comprenant :
  - **Identité** : Nom commercial, forme pharmaceutique (comprimé, gélule, suspension...), voies d'administration (orale, rectale...).
  - **Substances Actives & Dosages** : Composition détaillée du traitement.
  - **Prix & Remboursement** : Prix public indicatif et taux de remboursement Sécurité Sociale (ex : 65%).
  - **Conditions de délivrance** : Liste I / II, ordonnance sécurisée, durée de prescription.
  - **Métadonnées de la boîte** : Numéro de lot et date d'expiration scannés.

### 3. 📄 Numérisation & Importation d'Ordonnances (OCR)
- **Scan d'ordonnances papier** via la caméra avec détection de texte OCR en direct (*ML Kit Text Recognition*).
- **Importation de fichiers PDF** : Rendu natif `PdfRenderer` et analyse OCR automatique.
- **Pré-remplissage intelligent** : Détection automatique du **nom du médecin** (*Dr/Docteur...*), de la **date de l'ordonnance** et génération d'un titre automatique.
- **Visionneur de document intégré** : Visualisation en plein écran de l'ordonnance ou du fichier PDF scanné.

### 4. ⏰ Rappels de Pharmacie Programmés
- Programmation d'un **rappel de passage en pharmacie** avec sélecteurs de date et d'heure Material 3.
- Déclenchement d'une **notification Android prioritaire** (`AlarmManager` & `BroadcastReceiver`).
- **Désactivation automatique du rappel** dès que l'ordonnance est marquée comme *"Récupérée"*.

### 5. 🗂️ Historique & Registre Local (Room SQLite)
- Sauvegarde automatique hors-ligne de tous les scans et ordonnances dans une base de données **Room (SQLite avec KSP)**.
- **Recherche rapide** par nom de médicament, principe actif ou code CIP.
- **Filtres par statut** : *"Toutes"*, *"À récupérer"*, *"Récupérées"*.

---

## 🛠️ Stack Technique & Architecture

L'application suit les recommandations d'architecture officielle de Google (**MVVM + Clean Architecture + Repository Pattern**) :

* **Langage** : Kotlin 2.2+
* **UI & Design System** : Jetpack Compose + Material 3 (Design Edge-to-Edge complet)
* **Caméra & Vision** : CameraX + ML Kit (Barcode Scanning & Text Recognition)
* **Réseau** : Retrofit 2 + Gson + OkHttp Logging Interceptor
* **Base de données** : Room DB (KSP - Kotlin Symbol Processing)
* **Navigation** : Navigation Compose
* **Gestion des Arrière-plans** : `AlarmManager` & `NotificationManager`

---

## 📂 Structure du Projet

```
com.example.scanneurdemdicament/
├── data/
│   ├── local/            # Base Room (AppDatabase, Entities, DAOs, Converters)
│   ├── parser/           # Parsers GS1 DataMatrix, Text OCR et PDF
│   ├── remote/           # Services Retrofit & DTOs API BDPM
│   ├── reminder/         # AlarmManager & BroadcastReceiver pour les notifications
│   └── repository/       # Repositories (MedicamentRepository, PrescriptionRepository)
├── ui/
│   ├── components/       # Composants réutilisables (Card, DetailSheet, Viewers)
│   ├── scanner/          # Gestionnaires CameraX & Analyzers ML Kit
│   ├── screens/          # Écrans (ScannerScreen, HistoryScreen, SearchScreen, PrescriptionsScreen)
│   ├── theme/            # Thème Material 3 (Color, Type, Theme)
│   └── viewmodel/        # ViewModels (ScannerViewModel, HistoryViewModel, etc.)
└── MainActivity.kt       # Activité principale & Scaffold de Navigation
```

---

## 🚀 Préréquis & Installation

### Préréquis
* **Android Studio** (2024.1+ ou supérieur)
* **JDK** 17 ou 21
* **Android SDK** Minimum API 24 (Android 7.0+), Cible API 35 (Android 15)

### Compilation & Lancement

1. **Cloner le dépôt Git :**
   ```bash
   git clone https://github.com/votre-utilisateur/MedicScan.git
   cd MedicScan
   ```

2. **Ouvrir le projet dans Android Studio.**

3. **Lancer la synchronisation Gradle** (`Sync Project with Gradle Files`).

4. **Exécuter les tests unitaires :**
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Lancer l'application** sur un appareil physique ou un émulateur.

---

## 📄 Source des Données & Licence

* **Données Médicamenteuses** : [Base de Données Publique des Médicaments (BDPM)](https://base-donnees-publique.medicaments.gouv.fr/) via l'API publique [`medicaments-api.giygas.dev`](https://medicaments-api.giygas.dev/).
* **Licence** : Projet libre de droits sous licence MIT.
