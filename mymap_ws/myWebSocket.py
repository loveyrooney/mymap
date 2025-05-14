import os, aiohttp, xmltodict, time, asyncio
from jose import JWTError, jwt
from dotenv import load_dotenv
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()
load_dotenv()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],  # 모든 도메인 허용 (특정 도메인으로 제한할 수도 있음)
    allow_credentials=True,
    allow_methods=["*"],  # 모든 HTTP 메소드 허용
    allow_headers=["*"],  # 모든 헤더 허용
)

SESSION_TIMEOUT = 30 # 60 * 20  # seconds

def verify_jwt(token: str):
    jwt_key = os.getenv("JWT_KEY")
    public_key =f"-----BEGIN PUBLIC KEY-----\n{jwt_key}\n-----END PUBLIC KEY-----"
    try:
        payload = jwt.decode(token, public_key, algorithms=["RS256"])
        return payload
    except JWTError as e:
        print(f"authenticate 예외 발생: {e}")
        return None

@app.websocket("/mymap_ws/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    authenticate = True
    try:
        if authenticate :
            token = await websocket.receive_text()
            payload = verify_jwt(token)
            if payload is None :
                await websocket.send_text("Authentication failed")
                raise Exception()
            else :
                user_id = payload.get("sub")  
                await websocket.send_text(f"Hello {user_id}, you are connected!")
                authenticate = False
        while True:
            try:
                data = await asyncio.wait_for(websocket.receive_json(), timeout=SESSION_TIMEOUT)
                #last_activity = time.time()
                print(f"받은 메시지: {data}, 받은 시각: {int(time.time() * 1000)}")
                # data를 큐에 넣고 호출하는 구조로 만들어야 됨.
                if "bus" in data:
                    print("here")
                    for k,v in data['bus'].items():
                        # data 중 중복 arsid 요청이 있으면 캐시에서 가져오는 구조로 만들어야 됨. 그렇지 않으면 call_bus 실행
                        result_call_bus = await call_bus(k,set(v))
                        print(f"콜버스 결과: {result_call_bus}, , 받은 시각: {int(time.time() * 1000)}")
                        await websocket.send_json(result_call_bus)
                if "sub" in data:        
                    for k in data['sub']:
                        result_call_sub = await call_subway(k)
                        print(f"콜섭 결과: {result_call_sub}, 받은 시각: {int(time.time() * 1000)}")
                        await websocket.send_json(result_call_sub)
                if "bike" in data:
                    print("here")        
                    for k in data['bike']:
                        result_call_bike = await call_bike(k)
                        print(f"콜바이크 결과: {result_call_bike}, 받은 시각: {int(time.time() * 1000)}")
                        await websocket.send_json(result_call_bike)
            except asyncio.TimeoutError:
                await websocket.send_text("세션이 만료되었습니다.")
                await websocket.close()
                break        
    except Exception as e:
        print(f"예외 발생: {e}")
        await websocket.close()
    except WebSocketDisconnect:
        print("클라이언트 클로즈")


# bus routes filter
BUS_KEYSET = {'arrmsg1','arrmsg2','busRouteAbrv','busRouteId','busType1','busType2','congestion1','congestion2','deTourAt','isLast1','isLast2','nxtStn','routeType','rtNm','stNm','staOrd'}
def bus_routes_filter(item: dict, routes: set) -> dict:
    if 'busRouteAbrv' in item and item['busRouteAbrv'] in routes:
        return {k: item[k] for k in item if k in BUS_KEYSET}
    return {}

async def call_bus(arsId:str, routes:set)-> list:
    api_key = os.getenv("BUS_API_KEY")
    url = f"http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?serviceKey={api_key}&arsId={arsId}"
    #params ={'serviceKey' : api_key, 'arsId' : arsId }  이걸 지정하고 session.get(url, param=param)으로 하면 키내용이 바뀐다..
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            #print(f"Final URL: {response.url}")
            if response.status == 200:
                data = await response.text()  
                itemLists = xmltodict.parse(data).get("ServiceResult",{}).get("msgBody",{}).get("itemList",[])
                print(f"api 받은 시각: {int(time.time() * 1000)}")
                #print(f"itemLists===> {itemLists}")
                return [bus_routes_filter(item,routes) for item in itemLists if bus_routes_filter(item,routes)]
            else:
                return response.status

# subway filter
SUB_KEYSET = {'subwayId', 'updnLine', 'trainLineNm', 'trnsitCo', 'subwayList', 'btrainSttus', 'recptnDt', 'arvlMsg2', 'arvlMsg3', 'arvlCd', 'lstcarAt'}
def subway_filter(item: dict, direction: str) -> dict:
    if 'updnLine' in item and item['updnLine'] == direction:
        return {k: item[k] for k in item if k in SUB_KEYSET}
    return {}
            
async def call_subway(st_name:str, direction:str)-> list:
    api_key = os.getenv("SUB_API_KEY")
    url = f"http://swopenAPI.seoul.go.kr/api/subway/{api_key}/json/realtimeStationArrival/0/15/{st_name}"
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            #print(f"Final URL: {response.url}")
            if response.status == 200:
                data = await response.json() 
                itemLists = data.get("realtimeArrivalList",[])
                print(f"api 받은 시각: {int(time.time() * 1000)}")
                #print(f"itemLists===> {itemLists}")
                return [subway_filter(item,direction) for item in itemLists if subway_filter(item,direction)]
            else:
                return response.status
            
# bike filter
async def call_bike(st_name:str)-> list:
    api_key = os.getenv("SUB_API_KEY")
    url = f"http://openapi.seoul.go.kr:8088/{api_key}/json/bikeList/1/5/{st_name}"
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            #print(f"Final URL: {response.url}")
            if response.status == 200:
                data = await response.json() 
                itemLists = data.get("rentBikeStatus",{}).get("row",[])
                print(f"api 받은 시각: {int(time.time() * 1000)}")
                #print(f"itemLists===> {itemLists}")
                return itemLists[0].get("parkingBikeTotCnt")
            else:
                return response.status