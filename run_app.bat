@echo off
if "%JWT_SECRET%"=="" set JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
if "%GROQ_API_KEY%"=="" set GROQ_API_KEY=your_groq_api_key_here

echo Freeing port 8080 if in use...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1

echo Starting QuickEats Spring Boot Backend on http://localhost:8080...
"C:\Users\thesh\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd" spring-boot:run
