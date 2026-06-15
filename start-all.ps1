# CinePass Microservices Start Script
Write-Host "=========================================" -ForegroundColor Green
Write-Host "   Starting CinePass Microservices Stack" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# 1. Start Auth Service (Day 3)
Write-Host "[1/4] Starting Auth Service on port 8081..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\dailyactivity\antigravity\day3; .\mvnw spring-boot:run"

# 2. Start Movie Service (Day 5)
Write-Host "[2/4] Starting Movie Catalog Service on port 8082..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\dailyactivity\antigravity\day5; .\mvnw spring-boot:run"

# 3. Start API Gateway (Day 4)
Write-Host "[3/4] Starting API Gateway on port 8080..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\dailyactivity\antigravity\day4; .\mvnw spring-boot:run"

# 4. Start React Frontend (Day 2)
Write-Host "[4/4] Starting React Frontend on port 5173..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\dailyactivity\antigravity\day2; npm run dev"

Write-Host ""
Write-Host "All services started! Separate terminal windows have been opened." -ForegroundColor Green
Write-Host "You can access the application at http://localhost:5173" -ForegroundColor Green
Write-Host "Gateway entrypoint: http://localhost:8080" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
