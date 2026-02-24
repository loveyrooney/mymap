import aiohttp
from typing import Dict, Any
from config import settings
from models.schemas import BikeStatusItem

async def call_bike(st_name: str) -> Dict[str, Any]:
    api_key = settings.SUB_API_KEY # Bike uses same key? Check original code. Original code used SUB_API_KEY
    # "http://openapi.seoul.go.kr:8088/{api_key}/json/bikeList/1/5/{st_name}"
    
    url = f"http://openapi.seoul.go.kr:8088/{api_key}/json/bikeList/1/5/{st_name}"
    
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            if response.status == 200:
                try:
                    data = await response.json()
                    rentBikeStatus = data.get("rentBikeStatus", {})
                    row = rentBikeStatus.get("row", [])
                    
                    if row:
                        first_row = row[0]
                        return {
                            'name': first_row.get("stationName"),
                            'count': first_row.get("parkingBikeTotCnt")
                        }
                    return {}
                except Exception as e:
                    print(f"Bike Parse Error: {e}")
                    return {}
            else:
                print(f"Bike API Error: {response.status}")
                return {}
