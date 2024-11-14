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

# 스마트폰 갤러리의 폴더를 분류할 때
def sortLocation(extractFolder):
    
    image_path = "./" + extractFolder + "/"
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") 
                    or img.endswith(".jpeg") or img.endswith(".JPEG") or img.endswith(".JPG") or img.endswith(".PNG")]
    img_list_np = []

    j = 0
    
    for i in img_list_jpg:
        
        open_img = img_list_jpg[j]
        img = Image.open(image_path + open_img)
        print("\n오픈한 사진 : ", open_img)

        img_array = np.array(img)
        img_list_np.append(img_array)
        info = img._getexif()

        # 새로운 딕셔너리 생성
        taglabel = {}

        try:
            for tag, value in info.items():
                decoded = TAGS.get(tag, tag)
                taglabel[decoded] = value

            try:
                exifGPS = taglabel['GPSInfo']
                print(exifGPS)
            except KeyError:
                print("GPS 정보가 없습니다. 사진이 이동되지 않습니다.")
                j = j + 1
                continue

                
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

            print(Lat, Lon)

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

            region_1depth_name = data["documents"][0]["address"]["region_1depth_name"]
            region_2depth_name = data["documents"][0]["address"]["region_2depth_name"]

            bigCities = ["서울", "부산", "인천", "대구", "대전", "광주", "울산", "세종"]
            if region_1depth_name in bigCities:
                dir_name = region_1depth_name
            else:
                dir_name = region_2depth_name
            print(dir_name)
                    
            dst = "./ClassifyResult/"
            final_dst = dst + dir_name + "_" + extractFolder

            if os.path.exists(final_dst):
                print("이미 파일이 존재합니다.")
            else:
                os.mkdir(final_dst)
                print(dir_name, " 파일이 생성되었습니다.")

            img.close()
            shutil.move(image_path + img_list_jpg[j], final_dst + "/" + img_list_jpg[j])
            print("사진을 이동하였습니다.")
        except AttributeError:
            print("사진 위치 정보가 없습니다.")

        j = j + 1


#서버의 폴더를 분류할 때
def sortLocation2(extractFolder):
    
    image_path = "./ClassifyResult/" + extractFolder + "/"
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") 
                    or img.endswith(".jpeg") or img.endswith(".JPEG") or img.endswith(".JPG") or img.endswith(".PNG")]
    img_list_np = []

    j = 0
    
    for i in img_list_jpg:
        
        open_img = img_list_jpg[j]
        img = Image.open(image_path + open_img)
        print("\n오픈한 사진 : ", open_img)

        img_array = np.array(img)
        img_list_np.append(img_array)
        info = img._getexif()

        # 새로운 딕셔너리 생성
        taglabel = {}

        try:
            for tag, value in info.items():
                decoded = TAGS.get(tag, tag)
                taglabel[decoded] = value

            try:
                exifGPS = taglabel['GPSInfo']
                print(exifGPS)
            except KeyError:
                print("GPS 정보가 없습니다. 사진이 이동되지 않습니다.")
                j = j + 1
                continue

                
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

            print(Lat, Lon)

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

            region_1depth_name = data["documents"][0]["address"]["region_1depth_name"]
            region_2depth_name = data["documents"][0]["address"]["region_2depth_name"]

            bigCities = ["서울", "부산", "인천", "대구", "대전", "광주", "울산", "세종"]
            if region_1depth_name in bigCities:
                dir_name = region_1depth_name
            else:
                dir_name = region_2depth_name
            print(dir_name)
                    
            dst = "./ClassifyResult/"
            final_dst = dst + dir_name + "_" + extractFolder

            if os.path.exists(final_dst):
                print("이미 파일이 존재합니다.")
            else:
                os.mkdir(final_dst)
                print(dir_name, " 파일이 생성되었습니다.")

            img.close()
            shutil.move(image_path + img_list_jpg[j], final_dst + "/" + img_list_jpg[j])
            print("사진을 이동하였습니다.")
        except AttributeError:
            print("사진 위치 정보가 없습니다.")

        j = j + 1