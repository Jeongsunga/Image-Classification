from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil

def floatmul(a):
    return float(a)

def sortLocation(extractFolder):
    
    image_path = "../" + extractFolder + "/" + extractFolder + "/"
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") or img.endswith(".jpeg")]
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

            if ((34.8799 <= Lat <= 35.3959) and (128.7384 <= Lon <= 129.3728)):
                dir_name = "Busan"
            elif ((37.4132 <= Lat <= 37.7151) and (26.7340 <= Lon <= 127.2693)):
                dir_name = "Seoul"
            elif ((35.3491 <= Lat <= 35.6350) and (128.5773 <= Lon <= 129.0228)):
                dir_name = "Milyang"
            else:
                print("정보가 없습니다. 사진이 이동되지 않습니다.")
                j = j + 1
                continue

                    
            dst = "../ClassifyResult/"
            final_dst = dst + dir_name

            # 분류 완료된 결과를 DB에 저장해야 함
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


#sortLocation('wahaha')