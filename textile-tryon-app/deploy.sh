#!/bin/bash

# RAMRAJ Textile Try-On Application Deployment Script
echo "🚀 Starting RAMRAJ Textile Try-On Application..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 16 or higher."
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed. Please install npm."
        exit 1
    fi
    
    print_status "All requirements satisfied ✅"
}

# Kill existing processes
cleanup_processes() {
    print_status "Cleaning up existing processes..."
    
    # Kill any existing Spring Boot processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    # Kill any existing npm processes on port 3000
    lsof -ti:3000 | xargs kill -9 2>/dev/null || true
    
    # Kill any existing processes on port 8080
    lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    
    sleep 2
    print_status "Cleanup completed ✅"
}

# Build and start backend
start_backend() {
    print_status "Building and starting backend..."
    
    cd backend
    
    # Clean and compile
    mvn clean compile
    if [ $? -ne 0 ]; then
        print_error "Backend compilation failed"
        exit 1
    fi
    
    # Start backend in background
    print_status "Starting Spring Boot backend on port 8080..."
    mvn spring-boot:run > ../backend.log 2>&1 &
    BACKEND_PID=$!
    
    # Wait for backend to start
    print_status "Waiting for backend to initialize..."
    sleep 15
    
    # Check if backend is running
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/garments | grep -q "200"; then
        print_status "Backend started successfully ✅"
    else
        print_error "Backend failed to start. Check backend.log for details."
        exit 1
    fi
    
    cd ..
}

# Build and start frontend
start_frontend() {
    print_status "Building and starting frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing frontend dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        print_error "Frontend dependency installation failed"
        exit 1
    fi
    
    # Start frontend in background
    print_status "Starting React frontend on port 3000..."
    DANGEROUSLY_DISABLE_HOST_CHECK=true npm start > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    # Wait for frontend to start
    print_status "Waiting for frontend to initialize..."
    sleep 20
    
    # Check if frontend is running
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 | grep -q "200"; then
        print_status "Frontend started successfully ✅"
    else
        print_error "Frontend failed to start. Check frontend.log for details."
        exit 1
    fi
    
    cd ..
}

# Main deployment function
deploy() {
    print_status "🎨 RAMRAJ Textile Try-On Application Deployment"
    print_status "=============================================="
    
    check_requirements
    cleanup_processes
    start_backend
    start_frontend
    
    print_status ""
    print_status "🎉 RAMRAJ Application deployed successfully!"
    print_status "=============================================="
    print_status "📱 Frontend: http://localhost:3000"
    print_status "🔧 Backend:  http://localhost:8080/api"
    print_status "📊 H2 Console: http://localhost:8080/api/h2-console"
    print_status ""
    print_status "📋 Features:"
    print_status "  ✨ Premium RAMRAJ branding with gold & dark green theme"
    print_status "  🤖 AI-powered virtual try-on for Vesti and Saree"
    print_status "  🎯 Optimized prompts for elegant traditional draping"
    print_status "  📱 Responsive design with premium typography"
    print_status ""
    print_status "🔧 Process IDs:"
    print_status "  Backend PID: $BACKEND_PID"
    print_status "  Frontend PID: $FRONTEND_PID"
    print_status ""
    print_status "📝 Logs:"
    print_status "  Backend: ./backend.log"
    print_status "  Frontend: ./frontend.log"
    print_status ""
    print_warning "Press Ctrl+C to stop both applications"
    
    # Keep script running and handle Ctrl+C
    trap 'print_status "Shutting down..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0' INT
    
    # Wait for processes to finish
    wait
}

# Run deployment
deploy
