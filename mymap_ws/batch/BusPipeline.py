import pandas as pd
import sys
import os

# 현재 파일(dbPipeline.py)이 위치한 폴더(batch) 절대 경로 구하기
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 현재 파일의 상위 폴더(mymap_ws)를 파이썬 경로에 추가 (config.py 임포트용)
sys.path.append(os.path.dirname(BASE_DIR))

from config import settings
from io import StringIO
from sqlalchemy import create_engine, text

# 모든 파일 경로를 현재 파이썬 스크립트(batch 폴더) 기준으로 고정!
GG_TXT = os.path.join(BASE_DIR, 'routestation20260402V2.txt')
GG_BUS_CSV = os.path.join(BASE_DIR, 'gg_bus.csv')
GG_ORD_CSV = os.path.join(BASE_DIR, 'gg_ord.csv')

GG_ARS_TXT = os.path.join(BASE_DIR, 'station20260402V2.txt')
GG_ARS_CSV = os.path.join(BASE_DIR, 'gg_ars.csv')

SEO_XLSX = os.path.join(BASE_DIR, '서울시버스노선별정류소정보(20260108).xlsx')
SEO_BUS_CSV = os.path.join(BASE_DIR, 'seo_bus.csv')
SEO_ORD_CSV = os.path.join(BASE_DIR, 'seo_ord.csv')

# =========================================================
# 1. 서울 버스 파일 읽기 (기준 데이터이므로 먼저 처리)
# =========================================================
seo_df = pd.read_excel(SEO_XLSX, dtype={'ARS_ID': str, 'ROUTE_ID': str, 'NODE_ID': str})
seo_df = seo_df.rename(columns={
    "ROUTE_ID": "route_id",
    "순번": "sta_ord",
    "NODE_ID": "station_id",
    "ARS_ID": "arsid",
    "정류소명": "station_name",
    "X좌표": "x",
    "Y좌표": "y"
})
print("--- SEOUL DATA ---")
print(seo_df.head())

# bus 테이블 추출 (station_id 기준 물리적 중복 제거)
seo_df_station = seo_df[['station_id', 'arsid', 'station_name', 'x', 'y']].drop_duplicates(subset=['station_id'])
seo_df_station.to_csv(SEO_BUS_CSV, index=False, encoding='utf-8')
print(f"seo_bus.csv 저장완료: {len(seo_df_station)}행")

# order 테이블 추출
seo_df_order = seo_df[['station_id', 'route_id', 'arsid', 'sta_ord']].drop_duplicates()
seo_df_order.to_csv(SEO_ORD_CSV, index=False, encoding='utf-8')
print(f"seo_ord.csv 저장완료: {len(seo_df_order)}행")


# =========================================================
# 2. 경기도 버스 파일 및 ARS 맵핑 데이터 읽기
# =========================================================
with open(GG_TXT, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('^', '\n')
gg_df = pd.read_csv(StringIO(content), sep='|', dtype={'stationId': str, 'routeId': str})
gg_df = gg_df.rename(columns={
    "routeId": "route_id",
    "staOrder": "sta_ord",
    "stationId": "station_id",
    "stationName": "station_name"
})

# 경기도 ARS 매핑 파일 읽기 (stationId -> mobileNo)
with open(GG_ARS_TXT, 'r', encoding='utf-8') as f:
    content_ars = f.read()
content_ars = content_ars.replace('^', '\n')
gg_ars_df = pd.read_csv(StringIO(content_ars), sep='|', dtype={'stationId': str, 'mobileNo': str})
gg_ars_mapping = gg_ars_df[['stationId', 'mobileNo']].rename(columns={'stationId': 'station_id', 'mobileNo': 'arsid'}).drop_duplicates()

print("--- GYEONGGI DATA ---")
print(gg_df.head())

# =========================================================
# 3. 경기 버스(gg_bus) CSV 정제 
# =========================================================
gg_df_station = gg_df[['station_id', 'station_name', 'x', 'y']].drop_duplicates()

# 서울 df와 중복되는 경기 row는 삭제
gg_df_station = gg_df_station[~gg_df_station['station_id'].isin(seo_df_station['station_id'])]

# 남은 순수 경기 정류장들에 ARS ID 매핑 병합 (JOIN)
gg_df_station = pd.merge(gg_df_station, gg_ars_mapping, on='station_id', how='left')
gg_df_station['arsid'] = gg_df_station['arsid'].fillna('')

gg_df_station = gg_df_station[['station_id', 'arsid', 'station_name', 'x', 'y']] # 컬럼 순서 맞춤
gg_df_station.to_csv(GG_BUS_CSV, index=False, encoding='utf-8')
print(f"gg_bus.csv 저장완료: {len(gg_df_station)}행")

# =========================================================
# 4. 경기 순번(gg_ord) CSV 정제 
# =========================================================
gg_df_order = gg_df[['route_id', 'station_id', 'sta_ord']].drop_duplicates()

# 서울 정류장에 해당하는 arsid 조인
gg_df_order = pd.merge(gg_df_order, seo_df_station[['station_id', 'arsid']], on='station_id', how='left')
gg_df_order = gg_df_order.rename(columns={'arsid': 'seo_arsid'})

# 경기 정류장에 해당하는 arsid 조인
gg_df_order = pd.merge(gg_df_order, gg_ars_mapping, on='station_id', how='left')
gg_df_order = gg_df_order.rename(columns={'arsid': 'gg_arsid'})

# combine_first: 서울 arsid가 있으면 쓰고, 없으면 경기 arsid 값으로 채움
gg_df_order['arsid'] = gg_df_order['seo_arsid'].combine_first(gg_df_order['gg_arsid']).fillna('')

gg_df_order = gg_df_order[['station_id', 'route_id', 'arsid', 'sta_ord']]
gg_df_order.to_csv(GG_ORD_CSV, index=False, encoding='utf-8')
print(f"gg_ord.csv 저장완료: {len(gg_df_order)}행")

# =========================================================
# 5. DB 연결 및 적재
# =========================================================
DB_CONN = settings.DB_CONN
#print(f"DB_CONN: {DB_CONN}")
engine = create_engine(f"postgresql://{DB_CONN}")

def insert_bus(table_name, file_name, conn):
    # 기존 temp 테이블 truncate 
    conn.execute(text(f"TRUNCATE TABLE {table_name}"))
    # copy 방식으로 temp 에 새로 쓰기 
    with conn.connection.cursor() as cursor:
        with open(file_name, "r", encoding="utf-8") as f:
            bus_columns = "station_id, arsid, station_name, x, y"
            cursor.copy_expert(
                f'COPY {table_name} ({bus_columns}) FROM STDIN WITH CSV HEADER',
                f
            )    
    conn.execute(text(f"""
            UPDATE {table_name}
            SET geom = ST_SetSRID(ST_MakePoint(x, y), 4326)
        """))

def insert_ord(table_name, file_name, conn):
    # 기존 temp 테이블 truncate 
    conn.execute(text(f"TRUNCATE TABLE {table_name}"))
    # copy 방식으로 temp 에 새로 쓰기 
    with conn.connection.cursor() as cursor:
        with open(file_name, "r", encoding="utf-8") as f:
            ord_columns = "station_id, route_id, arsid, sta_ord"
            cursor.copy_expert(
                f'COPY {table_name} ({ord_columns}) FROM STDIN WITH CSV HEADER',
                f
            )

def insert_product(table_name, columns, conn):
    # 기존 temp 테이블 truncate 
    conn.execute(text(f"TRUNCATE TABLE {table_name} RESTART IDENTITY"))
    seo_temp = "seo_ord" if table_name == "station_order" else "seo_bus"
    gg_temp = "gg_ord" if table_name == "station_order" else "gg_bus"
    
    # 서울 ord / bus 추가
    conn.execute(text(f"""
            insert into {table_name} ({columns})
            select {columns} from {seo_temp};
        """))
    # 경기 ord / bus 추가
    conn.execute(text(f"""
            insert into {table_name} ({columns})
            select {columns} from {gg_temp};
        """))

with engine.connect() as conn:
    try:
        # temp db에 서울 / 경기 데이터 적재
        insert_ord("seo_ord", SEO_ORD_CSV, conn)
        insert_ord("gg_ord", GG_ORD_CSV, conn)
        insert_bus("seo_bus", SEO_BUS_CSV, conn)
        insert_bus("gg_bus", GG_BUS_CSV, conn)      
        conn.commit()
        print("🎉 1단계: 임시(Temp) 테이블 데이터 적재 완료")

        # Temp 작업이 완벽한 형태로 끝났으므로 최종 운영 DB(Product)에 통합 수행
        insert_product("station_order", "station_id, route_id, arsid, sta_ord", conn)
        insert_product("bus", "station_id, arsid, station_name, geom", conn)
        conn.commit()
        print("🎉 2단계: 운영(Product) DB 통합 완료!")
    except Exception as e:
        conn.rollback()
        print(f"에러 발생, 롤백: {e}")
engine.dispose()