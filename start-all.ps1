# CinePass Microservices Start Script
Write-Host "=========================================" -ForegroundColor Green
Write-Host "   Starting CinePass Microservices Stack" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# 1. Start Auth Service (Day 3)
Write-Host "[1/5] Starting Auth Service on port 8081..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot\day3; .\mvnw spring-boot:run"

# 2. Start Movie Service (Day 5)
Write-Host "[2/5] Starting Movie Catalog Service on port 8082..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot\day5; .\mvnw spring-boot:run"

# 3. Start Booking Service (Day 7)
Write-Host "[3/5] Starting Booking Service on port 8083..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot\day7; .\mvnw spring-boot:run"

# 4. Start API Gateway (Day 4)
Write-Host "[4/5] Starting API Gateway on port 8080..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot\day4; .\mvnw spring-boot:run"

# 5. Start React Frontend (Day 2)
Write-Host "[5/5] Starting React Frontend on port 5173..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot\day2; npm run dev"

Write-Host ""
Write-Host "All 5 services started! Separate terminal windows have been opened." -ForegroundColor Green
Write-Host "You can access the application at http://localhost:5173" -ForegroundColor Green
Write-Host "Gateway entrypoint: http://localhost:8080" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
