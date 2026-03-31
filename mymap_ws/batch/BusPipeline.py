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
GG_TXT = os.path.join(BASE_DIR, 'routestation20260304V2.txt')
GG_BUS_CSV = os.path.join(BASE_DIR, 'gg_bus.csv')
GG_ORD_CSV = os.path.join(BASE_DIR, 'gg_ord.csv')

SEO_XLSX = os.path.join(BASE_DIR, '서울시버스노선별정류소정보(20260108).xlsx')
SEO_BUS_CSV = os.path.join(BASE_DIR, 'seo_bus.csv')
SEO_ORD_CSV = os.path.join(BASE_DIR, 'seo_ord.csv')

# 경기도 버스 파일 읽기 
with open(GG_TXT, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('^', '\n')
gg_df = pd.read_csv(StringIO(content), sep='|', dtype={'stationId': str, 'routeId': str})
gg_df = gg_df.rename(columns={
    "routeId": "route_id",
    "staOrder": "sta_ord",
    "stationId": "arsid",
    "stationName": "station_name"
})
print(gg_df.head())
print(gg_df.shape)  # 행/열 수 확인

# bus 테이블 추가를 위한 컬럼 추출
gg_df_station = gg_df[['arsid', 'station_name', 'x', 'y']].drop_duplicates()
gg_df_station.to_csv(GG_BUS_CSV, index=False, encoding='utf-8')
print(f"gg_bus.csv 저장완료: {len(gg_df_station)}행")

# order 테이블 추가를 위한 컬럼 추출
gg_df_order = gg_df[['route_id', 'arsid', 'sta_ord']].drop_duplicates()
gg_df_order.to_csv(GG_ORD_CSV, index=False, encoding='utf-8')
print(f"gg_ord.csv 저장완료: {len(gg_df_order)}행")

# 서울 버스 파일 읽기
seo_df = pd.read_excel(SEO_XLSX, dtype={'ARS_ID': str, 'ROUTE_ID': str})
seo_df = seo_df.rename(columns={
    "ROUTE_ID": "route_id",
    "순번": "sta_ord",
    "ARS_ID": "arsid",
    "정류소명": "station_name",
    "X좌표": "x",
    "Y좌표": "y"
})
print(seo_df.head())
print(seo_df.shape)  # 행/열 수 확인

# bus 테이블 추가를 위한 컬럼 추출
seo_df_station = seo_df[['arsid', 'station_name', 'x', 'y']].drop_duplicates()
seo_df_station.to_csv(SEO_BUS_CSV, index=False, encoding='utf-8')
print(f"seo_bus.csv 저장완료: {len(seo_df_station)}행")

# order 테이블 추가를 위한 컬럼 추출
seo_df_order = seo_df[['route_id', 'arsid', 'sta_ord']].drop_duplicates()
seo_df_order.to_csv(SEO_ORD_CSV, index=False, encoding='utf-8')
print(f"seo_ord.csv 저장완료: {len(seo_df_order)}행")

# DB 연결
DB_CONN = settings.DB_CONN
#print(f"DB_CONN: {DB_CONN}")
engine = create_engine(f"postgresql://{DB_CONN}")

def insert_bus(table_name, file_name, conn):
    # 기존 temp 테이블 truncate 
    conn.execute(text(f"TRUNCATE TABLE {table_name}"))
    # copy 방식으로 temp 에 새로 쓰기 
    with conn.connection.cursor() as cursor:
        with open(file_name, "r", encoding="utf-8") as f:
            cursor.copy_expert(
                f'COPY {table_name} (arsid, station_name, x, y) FROM STDIN WITH CSV HEADER',
                f
            )    
    conn.execute(text(f"""
            UPDATE {table_name}
            SET geom = ST_SetSRID(ST_MakePoint(x, y), 4326)
        """))
    conn.commit()

def insert_ord(table_name, file_name, conn):
    # 기존 temp 테이블 truncate 
    conn.execute(text(f"TRUNCATE TABLE {table_name}"))
    # copy 방식으로 temp 에 새로 쓰기 
    with conn.connection.cursor() as cursor:
        with open(file_name, "r", encoding="utf-8") as f:
            cursor.copy_expert(
                f'COPY {table_name} (route_id, arsid, sta_ord) FROM STDIN WITH CSV HEADER',
                f
            )    
    conn.commit()

with engine.connect() as conn:
    try:
        insert_ord("seo_ord", SEO_ORD_CSV, conn)
        insert_ord("gg_ord", GG_ORD_CSV, conn)
        insert_bus("seo_bus", SEO_BUS_CSV, conn)
        insert_bus("gg_bus", GG_BUS_CSV, conn)
        print("🎉 배치 스크립트 정상 실행 및 마이그레이션 완료!")
    except Exception as e:
        conn.rollback()
        print(f"에러 발생, 롤백: {e}")
engine.dispose()