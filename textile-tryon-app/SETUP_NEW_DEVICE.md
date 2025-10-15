# 🚀 Setup Guide for New Devices/Team Members

## ⚠️ Potential Issues & Solutions

### Issue 1: Missing `.env` File
**Problem**: Application won't start because environment variables are not set.  
**Solution**: Follow the steps below to create and configure your `.env` file.

### Issue 2: Environment Variables Not Loading in Spring Boot
**Problem**: Spring Boot doesn't automatically load `.env` files.  
**Solution**: Use one of the methods described in section "Loading Environment Variables"

### Issue 3: Missing Uploads Directory
**Problem**: File uploads fail because the `uploads/` directory doesn't exist.  
**Solution**: Directory is auto-created by the app, but ensure write permissions.

### Issue 4: Node Modules & Build Artifacts
**Problem**: Large files in repo or missing dependencies.  
**Solution**: These are gitignored. Run `npm install` and `mvn clean install`.

---

## 📋 Prerequisites

Before starting, ensure you have:
- ✅ **Java 17+** installed (`java -version`)
- ✅ **Maven 3.6+** installed (`mvn -version`)
- ✅ **Node.js 18+** installed (`node -version`)
- ✅ **npm** installed (`npm -version`)
- ✅ **Git** installed (`git -version`)

---

## 🔧 Step-by-Step Setup

### Step 1: Clone the Repository
```bash
git clone <your-repo-url>
cd textile-tryon-app
```

### Step 2: Create Environment File
```bash
# Copy the example file
cp .env.example .env

# Edit the .env file with your actual credentials
# Use your preferred editor (nano, vim, vscode, etc.)
nano .env
```

**Required values to update in `.env`:**
```bash
# AWS Keys (Get from AWS Console -> IAM -> Security Credentials)
AWS_ACCESS_KEY_ID=your_actual_aws_access_key
AWS_SECRET_ACCESS_KEY=your_actual_aws_secret_key
AWS_BUCKET_NAME=your-s3-bucket-name

# Google Gemini API Key (Get from https://makersuite.google.com/app/apikey)
GOOGLE_GEMINI_API_KEY=your_actual_gemini_api_key
```

### Step 3: Backend Setup

#### Option A: Load Environment Variables (Mac/Linux)
```bash
# Export environment variables
export $(cat .env | grep -v '^#' | xargs)

# Verify they're loaded
echo $GOOGLE_GEMINI_API_KEY

# Run the backend
cd backend
mvn spring-boot:run
```

#### Option B: Load Environment Variables (Windows PowerShell)
```powershell
# Read and set environment variables
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}

# Run the backend
cd backend
mvn spring-boot:run
```

#### Option C: Using IntelliJ IDEA (Recommended for Development)
1. Install the **"EnvFile"** plugin:
   - Go to `Settings/Preferences` → `Plugins`
   - Search for "EnvFile"
   - Install and restart IntelliJ

2. Configure Run Configuration:
   - Open `Run` → `Edit Configurations`
   - Select your Spring Boot application
   - Go to `EnvFile` tab
   - Click `+` and add your `.env` file
   - Click `Apply` and `OK`

3. Run the application normally (Shift+F10 or click Run)

#### Option D: Using Eclipse
1. Right-click on project → `Run As` → `Run Configurations`
2. Select your Spring Boot App
3. Go to `Environment` tab
4. Click `Add` and manually add each environment variable from `.env`
5. Click `Apply` and `Run`

### Step 4: Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will automatically proxy API requests to `http://localhost:8080` (configured in `package.json`).

---

## 🧪 Verification Checklist

After setup, verify everything works:

### Backend Verification
```bash
# 1. Backend should start on port 8080
curl http://localhost:8080/api/garments
# Should return: 200 OK with JSON response

# 2. Check environment variables are loaded
# Look for this in backend logs:
# "🔑 API Key length: 39" (Gemini key loaded)
```

### Frontend Verification
```bash
# 1. Frontend should start on port 3000
# Open browser: http://localhost:3000

# 2. Check browser console for errors
# Should see no CORS errors
# API calls should work
```

---

## ❌ Common Issues & Fixes

### Issue: "Could not resolve placeholder 'GOOGLE_GEMINI_API_KEY'"
**Cause**: Environment variables not loaded  
**Fix**: Use one of the methods in Step 3 to load environment variables before running

### Issue: "Access Denied" when uploading files
**Cause**: AWS credentials incorrect or missing S3 permissions  
**Fix**: 
1. Verify AWS keys in `.env`
2. Check S3 bucket exists and has correct permissions
3. For local testing, set `app.file-storage.use-local=true` in `application-local.yml`

### Issue: "Port 8080 already in use"
**Cause**: Another application using port 8080  
**Fix**: 
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Issue: Frontend can't connect to backend
**Cause**: CORS or backend not running  
**Fix**:
1. Ensure backend is running on port 8080
2. Check `frontend/package.json` has `"proxy": "http://localhost:8080"`
3. Verify CORS is configured in backend

### Issue: "npm install" fails
**Cause**: Node version mismatch or network issues  
**Fix**:
```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install
```

### Issue: Maven build fails
**Cause**: Java version or dependencies  
**Fix**:
```bash
# Clean Maven
cd backend
mvn clean

# Install with force update
mvn clean install -U
```

---

## 🔐 Security Reminders

- ⚠️ **NEVER** commit the `.env` file
- ⚠️ **NEVER** share your API keys publicly
- ✅ Always use `.env.example` as a template
- ✅ Rotate your API keys if accidentally exposed
- ✅ Use different credentials for development/production

---

## 📦 What's Gitignored (Won't be Pushed)

These folders/files are safe and won't bloat your repo:
- `.env` - Your credentials
- `backend/target/` - Compiled Java classes
- `backend/uploads/` - Uploaded files
- `backend/logs/` - Log files
- `frontend/node_modules/` - npm dependencies
- `frontend/build/` - Production build

---

## 🆘 Still Having Issues?

1. **Check your environment variables**:
   ```bash
   export $(cat .env | xargs)
   env | grep -E 'AWS|GOOGLE|GEMINI'
   ```

2. **Check logs**:
   - Backend logs: `backend/logs/textile-tryon.log`
   - Frontend: Browser console (F12)

3. **Verify file structure**:
   ```bash
   ls -la .env
   ls -la backend/src/main/resources/application*.yml
   ```

4. **Test with H2 Database** (no MySQL needed):
   - The app uses H2 in-memory database by default
   - No database setup required for development

---

## ✅ Success Indicators

Your setup is complete when:
- ✅ Backend starts without errors on port 8080
- ✅ Frontend starts without errors on port 3000
- ✅ You can browse garments in the UI
- ✅ Environment variables are loaded (check logs)
- ✅ No "placeholder" errors in console

**You're ready to develop! 🎉**
