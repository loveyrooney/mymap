import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    JWT_PUBLIC_KEY = os.getenv("JWT_KEY")
    BUS_API_KEY = os.getenv("BUS_API_KEY")
    SUB_API_KEY = os.getenv("SUB_API_KEY")
    DB_CONN = os.getenv("DB_CONN")
    
settings = Settings()
