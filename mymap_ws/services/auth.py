from jose import JWTError, jwt
from config import settings

def verify_jwt(token: str):
    jwt_key = settings.JWT_PUBLIC_KEY
    if not jwt_key:
        return None
        
    public_key = f"-----BEGIN PUBLIC KEY-----\n{jwt_key}\n-----END PUBLIC KEY-----"
    try:
        # RS256 algorithm
        payload = jwt.decode(token, public_key, algorithms=["RS256"])
        return payload
    except JWTError as e:
        print(f"authenticate 예외 발생: {e}")
        return None
