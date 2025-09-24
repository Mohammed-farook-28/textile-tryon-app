# Textile Try-On Application

A full-stack web application for a textile shop with virtual try-on capabilities using AI-powered image generation.

## 🏗️ Architecture

- **Frontend**: React 19 with TypeScript, TailwindCSS, React Router
- **Backend**: Spring Boot 3.1.5 with Java 17
- **Database**: MySQL with JPA/Hibernate
- **Cloud Storage**: AWS S3 for image storage
- **AI Integration**: Google Virtual Try-On API & Flux Context Pro API

## 📁 Project Structure

```
textile-tryon-app/
├── frontend/               # React application
│   ├── src/
│   │   ├── components/    # Reusable UI components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API service functions
│   │   └── utils/         # Utility functions
│   └── package.json       # Frontend dependencies
├── backend/                # Spring Boot application
│   ├── src/main/java/com/textiletryon/
│   │   ├── controller/    # REST API controllers
│   │   ├── service/       # Business logic services
│   │   ├── repository/    # Data access layer
│   │   ├── model/         # JPA entities
│   │   ├── dto/           # Data transfer objects
│   │   └── config/        # Configuration classes
│   └── pom.xml            # Backend dependencies
└── database/
    └── schema.sql         # Database schema
```

## 🗄️ Database Schema

### Core Tables

1. **garments** - Store garment information (name, category, price, stock)
2. **garment_images** - Store garment image URLs with display order
3. **user_profiles** - Session-based user management
4. **user_photos** - User uploaded photos for try-on
5. **favorites** - User favorite garments
6. **tryon_results** - Generated try-on results

## 🔧 Backend Implementation

### Services

- **GarmentService**: CRUD operations, search, filtering
- **S3Service**: AWS S3 file upload/download operations
- **AITryOnService**: AI model integration for virtual try-on
- **UserSessionService**: Session-based user management
- **FavoriteService**: User favorites management

### Controllers

- **GarmentController**: Garment browsing and search
- **AdminGarmentController**: Admin garment management
- **UserController**: User profile and photo management
- **FavoriteController**: Favorites operations
- **TryOnController**: Virtual try-on functionality

### Features

- ✅ Comprehensive garment management with image support
- ✅ Advanced search and filtering capabilities
- ✅ AWS S3 integration for scalable image storage
- ✅ Session-based user management (no authentication required)
- ✅ User photo upload and management
- ✅ Favorites system with analytics
- ✅ AI-powered virtual try-on with multiple model support
- ✅ RESTful API with consistent response format
- ✅ Comprehensive error handling and validation
- ✅ Database optimization with proper indexing

## 🎨 Frontend Features

### User Interface
- Modern, responsive design with TailwindCSS
- Garment grid with filters and search
- Image gallery with zoom and navigation
- Photo upload with drag-and-drop
- Virtual try-on interface with model selection
- Favorites management
- Results gallery with sharing options

### Key Components Needed
- `GarmentGrid` - Browse garments with cards
- `FilterSidebar` - Price, color, category filters
- `SearchBar` - Search by name/pattern
- `GarmentCard` - Display garment with favorite button
- `PhotoUploader` - User photo upload/management
- `TryOnInterface` - Virtual try-on UI
- `ResultsGallery` - Display try-on results

## 🔌 API Endpoints

### Garment Endpoints
```
GET /api/garments                    # Browse/search garments
GET /api/garments/{id}               # Get garment details
GET /api/garments/filters/categories # Get available categories
GET /api/garments/filters/colors     # Get available colors
```

### Admin Endpoints
```
POST /api/admin/garments             # Create garment
PUT /api/admin/garments/{id}         # Update garment
DELETE /api/admin/garments/{id}      # Delete garment
POST /api/admin/garments/{id}/images # Add garment image
```

### User Endpoints
```
POST /api/user/profile               # Create/get user profile
POST /api/user/photos/upload         # Upload user photo
GET /api/user/photos                 # Get user photos
DELETE /api/user/photos/{id}         # Delete user photo
```

### Favorites Endpoints
```
POST /api/favorites/add              # Add to favorites
DELETE /api/favorites/remove/{id}    # Remove from favorites
GET /api/favorites                   # Get user favorites
GET /api/favorites/trending          # Get trending garments
```

### Try-On Endpoints
```
POST /api/tryon/generate             # Generate virtual try-on
GET /api/tryon/results              # Get try-on results
DELETE /api/tryon/results/{id}       # Delete try-on result
GET /api/tryon/models               # Get available AI models
```

## 🚀 Setup Instructions

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- AWS Account with S3 access
- AI API keys (Google/Flux)

### Backend Setup

1. **Database Setup**
   ```sql
   CREATE DATABASE textile_tryon;
   # Run schema.sql to create tables
   ```

2. **Environment Configuration**
   Create `.env` file in backend directory:
   ```
   DB_URL=jdbc:mysql://localhost:3306/textile_tryon
   DB_USERNAME=root
   DB_PASSWORD=your_password
   
   AWS_ACCESS_KEY=your_aws_access_key
   AWS_SECRET_KEY=your_aws_secret_key
   AWS_BUCKET_NAME=textile-images
   AWS_REGION=us-east-1
   
   FLUX_API_KEY=your_flux_api_key
   GOOGLE_TRYON_API_KEY=your_google_api_key
   ```

3. **Run Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Environment Configuration**
   Create `.env` file in frontend directory:
   ```
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```

3. **Run Frontend**
   ```bash
   npm start
   ```

## 🔒 Security Considerations

- Session-based authentication (no passwords stored)
- File upload validation and size limits
- CORS configuration for secure cross-origin requests
- Input validation on both frontend and backend
- SQL injection prevention with JPA/parameterized queries

## 📈 Performance Features

- Database indexing for optimal query performance
- Lazy loading for garment images
- Pagination for large datasets
- Connection pooling for database access
- Caching strategies for frequently accessed data

## 🎯 Future Enhancements

- User authentication system
- Real-time notifications
- Advanced AI model configurations
- Mobile app development
- Inventory management dashboard
- Sales analytics and reporting
- Social features (sharing, reviews)

## 🏃‍♂️ Getting Started

1. Set up the database using `database/schema.sql`
2. Configure environment variables for both frontend and backend
3. Start the backend server on port 8080
4. Start the frontend development server on port 3000
5. Access the application at `http://localhost:3000`

The application provides a complete textile shopping experience with cutting-edge AI-powered virtual try-on capabilities, making it easy for customers to visualize how garments will look on them before making a purchase.
