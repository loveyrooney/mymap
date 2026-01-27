# Start Spring Boot in background
Start-Process -FilePath ".\gradlew.bat" -ArgumentList "bootRun" -WorkingDirectory "..\mymap" -NoNewWindow

# Wait for Spring to init (naive wait)
Start-Sleep -Seconds 10

# Start FastAPI
cd ..\mymap_ws
python -m uvicorn main:app --reload --port 8090
