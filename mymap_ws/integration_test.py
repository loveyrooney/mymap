import requests
import asyncio
import websockets
import json

BASE_URL = "http://localhost:8080"
WS_URL = "ws://localhost:8090/mymap_ws/ws"

def test_login():
    print("Testing Login...")
    payload = {
        "userId": "test", # Assuming 'test' user exists
        "password": "test"
    }
    response = requests.post(f"{BASE_URL}/auth/login", json=payload)
    if response.status_code == 200:
        data = response.json()
        print("Login Successful!")
        return data["accessToken"]
    else:
        print(f"Login Failed: {response.status_code} - {response.text}")
        return None

async def test_websocket(token):
    print(f"Testing WebSocket with token: {token[:20]}...")
    try:
        async with websockets.connect(WS_URL) as websocket:
            # 1. Send token for authentication
            await websocket.send(token)
            
            # 2. Receive welcome message
            welcome = await websocket.recv()
            print(f"Received from WS: {welcome}")
            
            # 3. Send a bus research request (dummy data)
            dummy_request = {
                "clusterName": "TestStation",
                "bus": {
                    "13550": ["100", "143"] # Example arsId and routes
                }
            }
            await websocket.send(json.dumps(dummy_request))
            
            # 4. Receive response
            response = await websocket.recv()
            print(f"Received from WS (Bus data): {response[:200]}...")
            
    except Exception as e:
        print(f"WS Test Failed: {e}")

if __name__ == "__main__":
    # Note: This requires a user 'test' with password 'test' in the DB.
    # If the DB is empty, this might fail unless we register first.
    token = test_login()
    if token:
        asyncio.run(test_websocket(token))
