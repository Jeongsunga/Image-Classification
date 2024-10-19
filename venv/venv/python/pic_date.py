from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil
import re

# 스마트폰 갤러리의 폴더 분류할 때
def sortDate(innoDate, fileName, extractFolder):

    # 비교 날짜 추출
    user_year = innoDate[0:4]
    user_month = innoDate[6:8]
    user_day = innoDate[10:12]

    # 정규 표현식 패턴 정의
    pattern = r'^\d{4}년 \d{2}월 \d{2}일$'
    match = re.match(pattern, fileName)
    if match: # 사용자 지정 폴더 이름이 없는 경우
        title = user_year + user_month + user_day 
    else: # 사용자 지정 폴더 이름이 있는 경우
        title = fileName

    image_path = './' + extractFolder + '/'
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") or img.endswith(".jpeg")]
    #print(img_list_jpg)
    img_list_np = []

    j = 0

    for i in img_list_jpg:

        img = Image.open(image_path + "/" + img_list_jpg[j])
        print("\n오픈한 사진 : ", img_list_jpg[j])

        img_array = np.array(img)
        img_list_np.append(img_array)

        info = img._getexif()
        count = 0

        pic_date = []
        pic_date_year = []
        pic_date_day = []
        pic_date_month = []

        try:
            for tag_id in info:
                tag = TAGS.get(tag_id, tag_id)
                data = info.get(tag_id)

                if tag == 'DateTime':
                    print(f'{tag} : {data}')
                    count = 1

                    k = 0
                    for i in data:
                        pic_date.append(data[k])
                        k = k + 1
                        if k == 10:
                            break

                    remove_set = {":"}
                    pic_date = [i for i in pic_date if i not in remove_set]

                    l = 0
                    for i in pic_date:
                        if l < 4:
                            pic_date_year.append(pic_date[l])
                            
                        elif l < 6:
                            pic_date_month.append(pic_date[l])
                            
                        elif l < 8:
                            pic_date_day.append(pic_date[l])
                            
                        l = l + 1

                    # 각 리스트의 요소 각각을 모두 연결하여 하나의 문자열로 만든다
                    pic_date_year = ''.join(str(s) for s in pic_date_year)
                    pic_date_month = ''.join(str(s) for s in pic_date_month)
                    pic_date_day = ''.join(str(s) for s in pic_date_day)

                    
                    if user_year == pic_date_year and user_month == pic_date_month and user_day == pic_date_day:
                        dst = './ClassifyResult/'
                        final_dst = dst + title

                        if os.path.exists(final_dst):
                            print("이미 폴더가 존재합니다.")
                        else:
                            os.mkdir(final_dst)
                            print(title, " 폴더가 생성되었습니다.")

                        # os.makedirs(final_dst, exist_ok=True)
                        # print(dir_name, " 파일이 생성되었습니다.")

                        img.close()
                        shutil.move(image_path + img_list_jpg[j], (final_dst + "/") + img_list_jpg[j])
                        print("사진을 이동하였습니다.")

                    else:
                        print("일치하지 않습니다.")

            if count == 0:
                print("사진 날짜 정보가 없습니다.")

        except TypeError:
            print("사진 날짜 정보가 없습니다.")
            j = j + 1
            continue

        else:
            j = j + 1

# 서버의 폴더를 분류할 때
def sortDate2(innoDate, fileName, extractFolder):

    # 비교 날짜 추출
    user_year = innoDate[0:4]
    user_month = innoDate[6:8]
    user_day = innoDate[10:12]

    # 정규 표현식 패턴 정의
    pattern = r'^\d{4}년 \d{2}월 \d{2}일$'
    match = re.match(pattern, fileName)
    if match: # 사용자 지정 폴더 이름이 없는 경우
        title = user_year + user_month + user_day 
    else: # 사용자 지정 폴더 이름이 있는 경우
        title = fileName

    image_path = './ClassifyResult/' + extractFolder + "/"
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") or img.endswith(".jpeg")]
    img_list_np = []

    j = 0

    for i in img_list_jpg:

        img = Image.open(image_path + "/" + img_list_jpg[j])
        print("\n오픈한 사진 : ", img_list_jpg[j])

        img_array = np.array(img)
        img_list_np.append(img_array)

        info = img._getexif()
        count = 0

        pic_date = []
        pic_date_year = []
        pic_date_day = []
        pic_date_month = []

        try:
            for tag_id in info:
                tag = TAGS.get(tag_id, tag_id)
                data = info.get(tag_id)

                if tag == 'DateTime':
                    print(f'{tag} : {data}')
                    count = 1

                    k = 0
                    for i in data:
                        pic_date.append(data[k])
                        k = k + 1
                        if k == 10:
                            break

                    remove_set = {":"}
                    pic_date = [i for i in pic_date if i not in remove_set]

                    l = 0
                    for i in pic_date:
                        if l < 4:
                            pic_date_year.append(pic_date[l])
                            
                        elif l < 6:
                            pic_date_month.append(pic_date[l])
                            
                        elif l < 8:
                            pic_date_day.append(pic_date[l])
                            
                        l = l + 1

                    # 각 리스트의 요소 각각을 모두 연결하여 하나의 문자열로 만든다
                    pic_date_year = ''.join(str(s) for s in pic_date_year)
                    pic_date_month = ''.join(str(s) for s in pic_date_month)
                    pic_date_day = ''.join(str(s) for s in pic_date_day)

                    
                    if user_year == pic_date_year and user_month == pic_date_month and user_day == pic_date_day:
                        dst = './ClassifyResult/'
                        final_dst = dst + title

                        if os.path.exists(final_dst):
                            print("이미 폴더가 존재합니다.")
                        else:
                            os.mkdir(final_dst)
                            print(title, " 폴더가 생성되었습니다.")

                        # os.makedirs(final_dst, exist_ok=True)
                        # print(dir_name, " 파일이 생성되었습니다.")

                        img.close()
                        shutil.move(image_path + img_list_jpg[j], (final_dst + "/") + img_list_jpg[j])
                        print("사진을 이동하였습니다.")

                    else:
                        print("일치하지 않습니다.")

            if count == 0:
                print("사진 날짜 정보가 없습니다.")

        except TypeError:
            print("사진 날짜 정보가 없습니다.")
            j = j + 1
            continue

        else:
            j = j + 1