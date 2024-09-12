from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil
import requests
from urllib.parse import urlparse
from dotenv import load_dotenv

def floatmul(a):
    return float(a)

def metaLocation(image_path):
    img = Image.open(image_path)
    taglabel = {}
    info = img._getexif()

    try:
        for tag, value in info.items():
            decoded = TAGS.get(tag, tag)
            taglabel[decoded] = value

        try:
            exifGPS = taglabel['GPSInfo']
            #print(exifGPS)
            
        except KeyError:
            print("GPS 정보가 없습니다. 사진이 이동되지 않습니다.")
            data = "no Location"
            return data

        latData = list(map(floatmul, exifGPS[2]))
        lonData = list(map(floatmul, exifGPS[4]))

        latDeg = latData[0]
        latMin = latData[1]
        latSec = latData[2]

        lonDeg = lonData[0]
        lonMin = lonData[1]
        lonSec = lonData[2]

        # 도, 분, 초로 나타내기, 구글 맵에서 사용
        Lat = str(int(latDeg)) + "°" + str(int(latMin)) + "'" + str(int(latSec)) + "\"" + exifGPS[1]
        Lon = str(int(lonDeg)) + "°" + str(int(lonMin)) + "'" + str(lonSec) + "\"" + exifGPS[3]

        #print(Lat, Lon)

        # 도 decimal로 나타내기
        # 위도 계산
        Lat = (latDeg + (latMin + latSec / 60.0) / 60.0)
        # 북위, 남위인지를 판단, 남위일 경우 -로 변경
        if exifGPS[1] == 'S': Lat = Lat * -1

        # 경도 계산
        Lon = (lonDeg + (lonMin + lonSec / 60.0) / 60.0)
        # 동경, 서경인지를 판단, 서경일 경우 -로 변경
        if exifGPS[3] == 'W': Lon = Lon * -1

        print("위도 : ", Lat, ", 경도 : ", Lon)

        load_dotenv()
        url = "https://dapi.kakao.com/v2/local/geo/coord2address.json"
        api_key = os.getenv('API_KEY')
        headers = {
            "Authorization": f"KakaoAK {api_key}"
        }
        params = {
            "input_coord": "WGS84",
            "x": str(Lon),
            "y": str(Lat)
        }

        response = requests.get(url, headers=headers, params=params)
        data = response.json()
        try:
            road_address_name = data['documents'][0]['road_address']['address_name']
        except TypeError:
            road_address_name = data['documents'][0]['address']['address_name']

    except AttributeError:
        print("사진 위치 정보가 없습니다.")
        data = "no Location"
        return data

    img.close()
    return(road_address_name)