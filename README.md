# VoiceDrivenAIChatbot

An interactive, voice-enabled educational chatbot for children aged 3–7.  
Built using a Kotlin Android frontend and a Flask + LangChain Python backend.

---

## 📱 Features

- 🎤 Voice-to-text and text-to-speech interaction
- 📚 PDF-based knowledge retrieval
- 🧠 AI-powered responses via Groq API
- 🧩 Clean separation of backend and frontend

---

## 🧠 Tech Stack

| Frontend       | Backend          | AI & Tools           |
|----------------|------------------|-----------------------|
| Kotlin (Android) | Flask (Python)   | LangChain, ChromaDB  |
| Jetpack Compose | REST API         | Google Gen AI Embedding |
| Android Studio | PDF Parsing       | Groq LLM             |

---

## 🚀 Getting Started

### ✅ Backend

```bash
cd backend
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env
python test.py
