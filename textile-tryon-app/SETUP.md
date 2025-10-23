# Textile Try-On Application - Setup Guide

## 🚀 Complete Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- MySQL 8.0 or higher
- AWS Account (for S3 storage)
- AI API Access (Flux and/or Google Virtual Try-On)

### 📁 Project Structure
```
textile-tryon-app/
├── frontend/               # React TypeScript application
├── backend/                # Spring Boot application
├── database/               # Database schema and scripts
├── README.md              # Project documentation
└── SETUP.md              # This setup guide
```

## 🗄️ Database Setup

### 1. Create MySQL Database
```sql
CREATE DATABASE textile_tryon;
```

### 2. Run Database Schema
```bash
cd database/
mysql -u root -p textile_tryon < schema.sql
```

### 3. Verify Tables Created
- garments
- garment_images
- user_profiles
- user_photos
- favorites
- tryon_results

## 🔧 Backend Setup (Spring Boot)

### 1. Navigate to Backend Directory
```bash
cd backend/
```

### 2. Configure Environment Variables

Create `application-local.yml` in `src/main/resources/`:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/textile_tryon
    username: your_mysql_username
    password: your_mysql_password

# AWS S3 Configuration
aws:
  access-key: your_aws_access_key
  secret-key: your_aws_secret_key
  region: us-east-1
  s3:
    bucket-name: your-s3-bucket-name
    base-url: https://your-s3-bucket-name.s3.us-east-1.amazonaws.com

# AI API Configuration
ai:
  google:
    gemini:
      api-key: ${GOOGLE_GEMINI_API_KEY}
      api-url: ${GOOGLE_GEMINI_API_URL:https://generativelanguage.googleapis.com/v1beta/}
      model: ${GOOGLE_GEMINI_MODEL:gemini-2.5-flash-image}
      timeout: 60000

# CORS Configuration
app:
  cors:
    allowed-origins: http://localhost:3000
```

### 3. Install Dependencies and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

## 🎨 Frontend Setup (React)

### 1. Navigate to Frontend Directory
```bash
cd frontend/
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Create Environment File

Create `.env` in the frontend root:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_SESSION_TIMEOUT=3600000
REACT_APP_MAX_FILE_SIZE=10485760
REACT_APP_ALLOWED_FILE_TYPES=image/jpeg,image/jpg,image/png,image/gif,image/webp
```

### 4. Start Development Server
```bash
npm start
```

The frontend will start on `http://localhost:3000`

## ☁️ AWS S3 Setup

### 1. Create S3 Bucket
- Login to AWS Console
- Create a new S3 bucket (e.g., `textile-images-dev`)
- Enable public read access for uploaded images
- Configure CORS policy:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
        "AllowedOrigins": ["http://localhost:3000", "http://localhost:8080"],
        "ExposeHeaders": []
    }
]
```

### 2. Create IAM User
- Create IAM user for S3 access
- Attach policy: `AmazonS3FullAccess`
- Generate Access Key and Secret Key
- Use these credentials in your configuration

## 🤖 AI API Configuration

### Google Gemini API
- **Configuration**: Set in `.env` file
- **API Key**: `GOOGLE_GEMINI_API_KEY` (environment variable)
- **Model**: `gemini-2.5-flash-image`
- **Purpose**: AI-powered virtual try-on generation
- See `ENV_SETUP.md` for configuration details

## 🏃‍♂️ Running the Application

### 1. Start Backend
```bash
cd backend/
mvn spring-boot:run
```

### 2. Start Frontend
```bash
cd frontend/
npm start
```

### 3. Access the Application
- **Landing Page**: http://localhost:3000
- **User Interface**: http://localhost:3000/browse
- **Admin Panel**: http://localhost:3000/admin
- **Backend API**: http://localhost:8080/api

## 📝 Application Usage

### User Features
1. **Browse Garments**: Search and filter through available garments
2. **Upload Photos**: Upload personal photos for virtual try-on
3. **Virtual Try-On**: Use AI to see how garments look on you
4. **Favorites**: Save and manage favorite garments
5. **Profile Management**: Manage uploaded photos and preferences

### Admin Features
1. **Dashboard**: View inventory statistics and analytics
2. **Garment Management**: Add, edit, and delete garments
3. **Image Upload**: Upload product images with drag-and-drop
4. **Inventory Tracking**: Monitor stock levels and low stock alerts
5. **Bulk Operations**: Perform bulk updates and deletions

## 🔍 API Endpoints

### User Endpoints
- `GET /api/garments` - Browse garments
- `POST /api/user/photos/upload` - Upload user photo
- `POST /api/tryon/generate` - Generate virtual try-on
- `POST /api/favorites/add` - Add to favorites

### Admin Endpoints
- `POST /api/admin/garments` - Create garment
- `PUT /api/admin/garments/{id}` - Update garment
- `DELETE /api/admin/garments/{id}` - Delete garment
- `POST /api/admin/garments/{id}/images` - Upload garment image

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL is running
   - Check connection credentials
   - Ensure database exists

2. **CORS Errors**
   - Verify frontend URL in CORS configuration
   - Check that both frontend and backend are running

3. **File Upload Issues**
   - Verify AWS S3 credentials
   - Check bucket permissions
   - Ensure file size limits

4. **AI API Errors**
   - Verify API keys are correct
   - Check API quotas and limits
   - Monitor API response times

### Logs
- **Backend logs**: Console output from Spring Boot
- **Frontend logs**: Browser console
- **Database logs**: MySQL error logs

## 🔐 Security Notes

1. **API Keys**: Never commit real API keys to version control
2. **Database**: Use strong passwords for production
3. **AWS**: Use IAM roles with minimal required permissions
4. **CORS**: Configure strict origins for production

## 🚀 Production Deployment

### Backend
- Package: `mvn clean package`
- Deploy JAR file to your server
- Configure production database
- Set up SSL/HTTPS

### Frontend
- Build: `npm run build`
- Deploy to static hosting (Netlify, Vercel, S3)
- Configure production API URLs

### Database
- Use managed database service (AWS RDS)
- Set up backups and monitoring
- Configure SSL connections

## 📊 Sample Data

The database schema includes sample garments for testing:
- Silk Sarees
- Cotton Dresses
- Formal Shirts
- Casual Wear

## 🆘 Support

For issues or questions:
1. Check the troubleshooting section
2. Review application logs
3. Verify all environment variables are set correctly

---

**Happy Coding! 🎉**

The Textile Try-On application is now ready for development and testing. The Flux API key is already configured and ready to use for AI-powered virtual try-on functionality.
