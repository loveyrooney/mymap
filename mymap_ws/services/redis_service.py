import os
import redis
from dotenv import load_dotenv

load_dotenv()

class RedisCache:
    def __init__(self):
        self.host = os.getenv("REDIS_HOST")
        self.port = os.getenv("REDIS_PORT")
        self.password = os.getenv("REDIS_PASSWORD")

        if not self.host or not self.port:
            raise ValueError("❌ REDIS_HOST 또는 REDIS_PORT 환경 변수가 .env 파일에 설정되지 않았습니다.")

        self.client = redis.Redis(
            host=self.host,
            port=int(self.port),
            password=self.password,
            decode_responses=True
        )

    def get_ars_id(self, station_id: str) -> str:
        """station_id로 arsid를 조회합니다 (전역 매핑)"""
        return self.client.hget("station:mapping", station_id)

    def set_bus_data(self, key: str, data: str, ttl: int = 10):
        """버스 위치 데이터를 10초간 캐싱합니다"""
        self.client.set(key, data, ex=ttl)

    def get_bus_data(self, key: str) -> str:
        """캐싱된 버스 위치 데이터를 가져옵니다"""
        return self.client.get(key)

# 싱글톤 인스턴스 생성
redis_cache = RedisCache()
