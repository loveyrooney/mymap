import time
import asyncio
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware

from services.auth import verify_jwt
from services.bus import call_bus
from services.subway import call_subway
from services.bike import call_bike
from models.schemas import WebSocketRequest

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Allow all origins for now
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

SESSION_TIMEOUT = 60 * 30  # 30 minutes

@app.websocket("/mymap_ws/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    authenticated = False
    
    try:
        if not authenticated:
            # First message should be the token
            token = await websocket.receive_text()
            payload = verify_jwt(token)
            
            if payload is None:
                await websocket.send_json({"msg": "Authentication failed"})
                await websocket.close()
                return
            else:
                user_id = payload.get("sub")
                await websocket.send_json({"msg": f"Hello {user_id}, webSocket connected"})
                authenticated = True

        while True:
            try:
                # Wait for data with timeout
                data = await asyncio.wait_for(websocket.receive_json(), timeout=SESSION_TIMEOUT)
                print(f"Received: {data}, Time: {int(time.time() * 1000)}")
                
                # Parse using Pydantic (optional, but good for validation if we want strict schema)
                # request = WebSocketRequest(**data) 
                
                # Using dictionary directly for flexibility as per original code structure
                cluster_name = data.get('clusterName')
                
                if "bus" in data:
                    bus_data = data['bus'] # Dict[arsId, List[routes]]
                    for arsId, routes in bus_data.items():
                        # Concurrently call APIs if needed, but for now sequential as per original
                        bus_result = await call_bus(arsId, set(routes))
                        print(f"Bus Result: {len(bus_result)} items")
                        await websocket.send_json({
                            'bus': bus_result, 
                            'clusterName': cluster_name
                        })

                if "sub" in data:
                    sub_list = data['sub'] # List[stationName]
                    for station in sub_list:
                        sub_result = await call_subway(station)
                        print(f"Sub Result: {len(sub_result)} items")
                        await websocket.send_json({
                            'sub': sub_result, 
                            'clusterName': cluster_name
                        })

                if "bike" in data:
                    bike_list = data['bike'] # List[stationName]
                    for station in bike_list:
                        bike_result = await call_bike(station)
                        print(f"Bike Result: {bike_result}")
                        await websocket.send_json({
                            'bike': bike_result, 
                            'clusterName': cluster_name
                        })

            except asyncio.TimeoutError:
                await websocket.send_json({"msg": "Session expired"})
                await websocket.close()
                break
                
    except WebSocketDisconnect:
        print("Client disconnected")
    except Exception as e:
        print(f"Unexpected error: {e}")
        try:
            await websocket.close()
        except:
            pass
