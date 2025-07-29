# Cloudinary_App
A cloud-based system that allows users to upload images, audio, and videos from an Android app to Cloudinary, and view the uploaded media on a Python Flask-based website from anywhere using the public URLs.

## 📘 Features

### 📱 Android App (Java)
- Upload **images**, **audio**, and **video** to Cloudinary
- Capture or select media from device
- Get the **public ID / URL** of uploaded files
- View confirmation and upload status

### 🌐 Web Dashboard (Python Flask)
- Display all uploaded media
- HTML template renders:
  - 📷 Images
  - 🎵 Audio players
  - 🎥 Video players
- Organized and mobile-friendly layout

---

## 🔧 Technologies Used

| Component       | Tech Stack                         |
|------------------|-------------------------------------|
| Android App      | Java, Android Studio, Volley       |
| File Hosting     | Cloudinary API                     |
| Web Framework    | Python Flask                       |
| Frontend         | HTML5, CSS3, Bootstrap             |
| Backend Display  | Cloudinary Public URLs             |
