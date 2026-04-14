import aiohttp
import xmltodict
import time
from typing import List, Set, Dict, Any
from config import settings
from models.schemas import BusArrivalItem

# bus routes filter
BUS_KEYSET = { 'stNm', 'busRouteId', 'rtNm', 'busRouteAbrv', 'routeType', 'staOrd', 'isLast1', 'busType1', 'isLast2', 'busType2', 'arrmsg1', 'arrmsg2', 'nxtStn', 'deTourAt', 'congestion1', 'congestion2'}
GG_BUS_KEYSET = {'crowded1', 'crowded2','locatioinNo1','locationNo2', 'predictTime1','predictTime2','remainSeatCnt1','remainSeatCnt2','routeDestName','routeId','routeName','routeTypeCd', 'stationId'}

def bus_routes_filter(item: dict, routes: Set[str]) -> dict:
    print(f"hello busroutefilter : {item}, {routes}")
    #if 'busRouteAbrv' in item and item['busRouteAbrv'] in routes:
    if 'busRouteId' in item and item.get('busRouteId') in routes:
        return {k: item[k] for k in item if k in BUS_KEYSET}
    elif 'routeId' in item and str(item.get('routeId')) in routes:
        if (item.get('predictTime1') != None or item.get('predictTime1') != '') or (item.get('predictTime2') != None or item.get('predictTime2') != ''):
            return {k: item[k] for k in item if k in GG_BUS_KEYSET}
    return {}

async def get_bus_arrivals_from_api(session, api_key, stId, is_seoul, routes):
    if is_seoul:
        # 여기서 redis_service 에서 stId 를 이용해서 arsId 를 가져와야 됨.
        arsId = redis_service.get(stId)
        url = f"http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?serviceKey={api_key}&arsId={arsId}"
        root_key, list_key, keyset = "ServiceResult", "itemList", BUS_KEYSET
    else:
        url = f"https://apis.data.go.kr/6410000/busarrivalservice/v2/getBusArrivalListv2?serviceKey={api_key}&stationId={stId}&format=json"
        root_key, list_key, keyset = "response", "busArrivalList", GG_BUS_KEYSET

    list_key = "busArrivalList"
    async with session.get(url) as response:
        if response.status != 200:
            api_name = "Seoul" if is_seoul else "Gyeonggi"
            raise Exception(f"{api_name} API Error: {response.status}")
            
        parsed_data = xmltodict.parse(await response.text()) if is_seoul else await response.json()
            
        msg_body = parsed_data.get(root_key, {}).get("msgBody")
        # API 키 에러 등 비정상 상황 시 msgBody가 빈 문자열("")이나 None으로 올 수 있음
        if not isinstance(msg_body, dict):
            return []
            
        item_lists = msg_body.get(list_key, [])
        
        print(f"api 받은 시각: {int(time.time() * 1000)}")
        print(f"itemLists===> {item_lists}")
        print(f"len list===> {len(item_lists) if isinstance(item_lists, dict) else len(item_lists)}, {isinstance(item_lists, list)}")
        
        if isinstance(item_lists, list):
            return [bus_routes_filter(item, routes) for item in item_lists if bus_routes_filter(item, routes)]
        else:
            filtered = {k: item_lists[k] for k in item_lists if k in keyset}
            # 반환 타입이 맞춰질 수 있게 처리 (요청에 따라 기존 else 로직을 살려 리턴)
            return filtered

async def call_bus(session: aiohttp.ClientSession, stId: str, routes: Set[str]) -> List[Any]:
    api_key = settings.BUS_API_KEY
    #params ={'serviceKey' : api_key, 'arsId' : arsId }  이걸 지정하고 session.get(url, param=param)으로 하면 키내용이 바뀐다..
    is_seoul = stId.startswith('1')
    
    try:
        return await get_bus_arrivals_from_api(session, api_key, stId, is_seoul, routes)
    except Exception as e:
        print(f"Call API Error: {e}")
        try:
            print(f"Retry with alternate API for stId: {stId}")
            return await get_bus_arrivals_from_api(session, api_key, stId, not is_seoul, routes)
        except Exception as e2:
            print(f"Retry Call API Error: {e2}")
            return []
