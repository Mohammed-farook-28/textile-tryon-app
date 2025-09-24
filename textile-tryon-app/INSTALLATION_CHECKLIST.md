# 🚀 Complete Installation Checklist

## ✅ **Already Done**
- ✅ Java 25 (OpenJDK) - Perfect!
- ✅ Node.js v22.17.0 - Perfect!
- ✅ npm v10.9.2 - Good!
- ✅ Application code and AI APIs configured
- ✅ Backend configuration file created: `application-local.yml`
- ✅ Frontend configuration file created: `.env`

## ❌ **Still Need to Install**

### 1. **MySQL 8.0+** (Database)
```powershell
# Download MySQL 8.0+ from:
# https://dev.mysql.com/downloads/mysql/

# During installation:
# - Set root password (remember this!)
# - Enable MySQL as Windows Service
# - Add MySQL to PATH
```

### 2. **Maven** (Spring Boot Build Tool)
```powershell
# Option 1: Using Chocolatey (Recommended)
choco install maven

# Option 2: Manual Installation
# 1. Download from: https://maven.apache.org/download.cgi
# 2. Extract to C:\apache-maven-3.9.x
# 3. Add C:\apache-maven-3.9.x\bin to PATH
# 4. Set JAVA_HOME to your Java installation
```

## 🔧 **Configuration Steps After Installation**

### 1. **Setup MySQL Database**
After installing MySQL:
```sql
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE textile_tryon;

# Exit MySQL
exit;

# Import schema
cd D:\Private\Textile tryon\textile-tryon-app\database
mysql -u root -p textile_tryon < schema.sql
```

### 2. **Update Backend Configuration**
Edit: `textile-tryon-app/backend/src/main/resources/application-local.yml`
```yaml
spring:
  datasource:
    username: root  # Your MySQL username
    password: YOUR_ACTUAL_PASSWORD  # Replace with your MySQL password
```

### 3. **Setup AWS S3** (For Image Storage)
1. **Create AWS Account** (if you don't have one)
2. **Create S3 Bucket**:
   - Login to AWS Console
   - S3 → Create bucket
   - Name: `textile-images-dev`
   - Enable public read access
3. **Create IAM User**:
   - IAM → Users → Create user
   - Attach policy: `AmazonS3FullAccess`
   - Generate Access Key + Secret Key
4. **Update Configuration**:
   Edit `application-local.yml`:
   ```yaml
   aws:
     access-key: your_actual_access_key
     secret-key: your_actual_secret_key
   ```

## 🚀 **Testing Installation**

### 1. **Test Java & Maven**
```powershell
java -version  # Should show Java 25
mvn -version   # Should show Maven version
```

### 2. **Test MySQL**
```powershell
mysql -u root -p  # Should connect to MySQL
```

### 3. **Start Backend**
```powershell
cd D:\Private\Textile tryon\textile-tryon-app\backend
mvn clean install
mvn spring-boot:run
# Should start on http://localhost:8080
```

### 4. **Start Frontend**
```powershell
cd D:\Private\Textile tryon\textile-tryon-app\frontend
npm install
npm start
# Should start on http://localhost:3000
```

## 🎯 **Quick Installation Commands**

### Install Missing Software:
```powershell
# Install MySQL 8.0+
# Manual download: https://dev.mysql.com/downloads/mysql/

# Install Maven (if you have Chocolatey)
choco install maven

# Or install Chocolatey first, then Maven
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
choco install maven
```

## 📋 **Current Status Summary**

| Component | Status | Action |
|-----------|--------|--------|
| Java 17+ | ✅ DONE | None - Java 25 installed |
| Node.js 18+ | ✅ DONE | None - v22.17.0 installed |
| npm | ✅ DONE | None - v10.9.2 installed |
| MySQL 8.0+ | ❌ MISSING | Install MySQL |
| Maven | ❌ MISSING | Install Maven |
| Backend Config | ✅ DONE | Update MySQL password |
| Frontend Config | ✅ DONE | None needed |
| Database Schema | ❌ PENDING | Run after MySQL install |
| AWS S3 | ❌ PENDING | Create bucket + credentials |
| AI APIs | ✅ DONE | Flux + Gemini configured |

## 🆘 **Need Help?**

### Common Issues:
1. **"mvn not recognized"** → Install Maven and add to PATH
2. **"mysql not recognized"** → Install MySQL and add to PATH  
3. **Database connection error** → Check MySQL service is running
4. **Port 8080 already in use** → Stop other services on port 8080

### Next Steps:
1. Install MySQL + Maven
2. Setup database
3. Configure AWS S3 (optional for now)
4. Test the application!

**You're almost there! Just need MySQL and Maven, then you're ready to go! 🎉**

