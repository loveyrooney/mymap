import aiohttp
import time
from typing import Dict, Any
from config import settings
from models.schemas import BikeStatusItem

# bike filter
async def call_bike(session: aiohttp.ClientSession, st_name: str) -> Dict[str, Any]:
    api_key = settings.SUB_API_KEY # Bike uses same key? Check original code. Original code used SUB_API_KEY
    # "http://openapi.seoul.go.kr:8088/{api_key}/json/bikeList/1/5/{st_name}"
    
    url = f"http://openapi.seoul.go.kr:8088/{api_key}/json/bikeList/1/5/{st_name}"
    
    async with session.get(url) as response:
        #print(f"Final URL: {response.url}")
        if response.status == 200:
            data = await response.json() 
            itemLists = data.get("rentBikeStatus",{}).get("row",[])
            print(f"api 받은 시각: {int(time.time() * 1000)}")
            #print(f"itemLists===> {itemLists}")
            return {'name' : itemLists[0].get("stationName"), 'count' : itemLists[0].get("parkingBikeTotCnt")}
        else:
            return response.status

