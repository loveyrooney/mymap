import aiohttp
import xmltodict
from typing import List, Set, Dict, Any
from config import settings
from models.schemas import BusArrivalItem

BUS_KEYSET = {'arrmsg1','arrmsg2','busRouteAbrv','busRouteId','busType1','busType2','congestion1','congestion2','deTourAt','isLast1','isLast2','nxtStn','routeType','rtNm','stNm','staOrd','routeType'}

def bus_routes_filter(item: dict, routes: Set[str]) -> dict:
    if 'busRouteAbrv' in item and item['busRouteAbrv'] in routes:
        return {k: item[k] for k in item if k in BUS_KEYSET}
    return {}

async def call_bus(arsId: str, routes: Set[str]) -> List[Any]:
    api_key = settings.BUS_API_KEY
    url = f"http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?serviceKey={api_key}&arsId={arsId}"
    
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            if response.status == 200:
                data = await response.text()
                try:
                    parsed = xmltodict.parse(data)
                    itemLists = parsed.get("ServiceResult",{}).get("msgBody",{}).get("itemList",[])
                    
                    if not itemLists:
                        return []
                        
                    if isinstance(itemLists, list):
                        filtered = [bus_routes_filter(item, routes) for item in itemLists if bus_routes_filter(item, routes)]
                        return filtered
                    else:
                        filtered = bus_routes_filter(itemLists, routes)
                        return [filtered] if filtered else []
                except Exception as e:
                    print(f"Bus Parse Error: {e}")
                    return []
            else:
                print(f"Bus API Error: {response.status}")
                return []
