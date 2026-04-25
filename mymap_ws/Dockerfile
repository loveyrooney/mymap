FROM python:3.11-slim
WORKDIR /app

# 시간대 설정 (선택 사항)
ENV TZ=Asia/Seoul

# 로그 즉시 출력을 위한 환경변수 (필수!)
ENV PYTHONUNBUFFERED=1

# Copy requirements first for better layer caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest
COPY . .

EXPOSE 8090
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8090"]
