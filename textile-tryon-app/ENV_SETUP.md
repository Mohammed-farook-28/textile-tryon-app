# Environment Variables Setup Guide

## Overview
All sensitive credentials have been moved to a `.env` file to prevent them from being committed to version control.

## Setup Instructions

### 1. Environment File
The `.env` file is already created in the root directory with your actual credentials. This file is **gitignored** and will NOT be pushed to your repository.

### 2. For New Team Members
If someone else needs to set up this project:
1. Copy `.env.example` to `.env`
2. Fill in the actual credentials in the `.env` file
3. Never commit the `.env` file

### 3. Environment Variables Used

#### Database Configuration
- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database username  
- `DB_PASSWORD` - Database password

#### AWS S3 Configuration
- `AWS_ACCESS_KEY_ID` - AWS access key
- `AWS_SECRET_ACCESS_KEY` - AWS secret key
- `AWS_REGION` - AWS region (default: us-east-1)
- `AWS_BUCKET_NAME` - S3 bucket name

#### Google Gemini API Configuration
- `GOOGLE_GEMINI_API_KEY` - Google Gemini API key
- `GOOGLE_GEMINI_API_URL` - Gemini API URL
- `GOOGLE_GEMINI_MODEL` - Gemini model name

#### CORS Configuration
- `CORS_ALLOWED_ORIGINS` - Allowed origins for CORS

### 4. Loading Environment Variables

#### Option A: Using IntelliJ IDEA / Eclipse
1. Install the "EnvFile" plugin (IntelliJ) or similar
2. Configure the plugin to load the `.env` file
3. Run your application

#### Option B: Using Command Line
```bash
# Load environment variables from .env
export $(cat .env | xargs)

# Run the application
cd backend
mvn spring-boot:run
```

#### Option C: Using System Environment Variables
Set the environment variables in your system/IDE configuration.

### 5. Verification
After setup, verify that:
- [ ] `.env` file exists and contains your credentials
- [ ] `.env` is listed in `.gitignore`
- [ ] No sensitive keys appear in any `.yml` files
- [ ] Application starts successfully with environment variables

## Security Notes
- ⚠️ **NEVER** commit the `.env` file
- ⚠️ **NEVER** hardcode sensitive keys in code
- ✅ Always use `.env.example` as a template
- ✅ Keep `.env` in `.gitignore`

## Safe to Push
After these changes, your codebase is now safe to push to Git without exposing sensitive credentials.
