from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil
import re

# 스마트폰 갤러리의 폴더를 분류할 때
def pic_period(innoDate, folderName, extractFolder):
        
    # 날짜 추출
    start_year = innoDate[0:4]
    start_month = innoDate[6:8]
    start_day = innoDate[10:12]
        
    end_year = innoDate[14:18]
    end_month = innoDate[20:22]
    end_day = innoDate[24:26]

    # 정규 표현식 패턴 정의
    pattern = r'^\d{4}년 \d{2}월 \d{2}일-\d{4}년 \d{2}월 \d{2}일$'
    
    # 문자열이 패턴과 일치하는지 검사
    match = re.match(pattern, folderName)
    if match: # 사용자 지정 폴더 이름 없음
        title = start_year + start_month + start_day + ' - ' + end_year + end_month + end_day
    else: # 사용자 지정 폴더 이름 있음
        title = folderName

    image_path = './' + extractFolder + '/'
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") 
                    or img.endswith(".jpeg") or img.endswith(".JPEG") or img.endswith(".JPG") or img.endswith(".PNG")]
    img_list_np = []

    j = 0

    for i in img_list_jpg:

        img = Image.open(image_path + img_list_jpg[j])
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

                    '''
                                                1. pic_date_year < start_year => out
                                                2. pic_date_month < start_month => out
                                                3. pic_date_day < start_day => out
                                                4. pic_date_year > end_year => out
                                                5. pic_date_month > end_month => out
                                                6. pic_date_day > end_day => out

                                                정리

                                                pic_date_year < start_year or pic_date_year > end_year
                                                pic_date_month < start_month or pic_date_month > end_month
                                                pic_date_day < start_day or pic_date_day > end_day
                                                '''
                    moderate = True
                    # 시작/마감 년/월이 같고 날만 다를 때
                    if(pic_date_year == start_year == end_year and 
                        pic_date_month == start_month == end_month and 
                        start_day <= pic_date_day <= end_day): moderate = True
                    else: moderate = False

                    # 시작/마감 년은 같은데 달이 다를 때, 시작 월인데 날이 시작 날보다 이르거나, 마감 월인데 마감 일보다 늦거나
                    if(pic_date_year == start_year == end_year and start_month <= pic_date_month <= end_month):
                        if(start_month == pic_date_month and pic_date_day < start_day): moderate == False
                        elif(end_month == pic_date_month and pic_date_day > end_day): moderate == False
                        else: moderate = True

                    # 시작/마감 년/월/일 다를 때
                    elif(start_year <= pic_date_year <= end_year):
                        #사진 년도와 시작 년도가 같고, 사진 월이 시작 월보다 이를 때 false
                        if(pic_date_year == start_year and pic_date_month < start_month):moderate = False
                        #사진 년도/월이 시작 년도/월이 같고, 사진 날이 시작날보다 이를 때 false
                        elif(pic_date_year == start_year and pic_date_month == start_month and pic_date_day < start_day): moderate = False
                        #사진 년도와 시작 년도가 같고, 사진 월이 마감 월보다 뒤일 때 false
                        elif(pic_date_year == end_year and pic_date_month > end_month): moderate = False
                        #사진 년도/월과 마감 년도/월이 같고, 사진 날이 마감 날보다 뒤일 때 false
                        elif(pic_date_year == end_year and pic_date_month == end_month and pic_date_day > end_day): moderate = False
                        else: moderate = True
                            
                    else:
                        moderate == False

                    if moderate == True:
                        dst = './ClassifyResult/'
                        final_dst = dst + title 
                            

                        if os.path.exists(final_dst):
                            print("이미 파일이 존재합니다.")
                        else:
                            os.mkdir(final_dst)
                            print(title, " 파일이 생성되었습니다.")

                        img.close()
                        shutil.move(image_path + img_list_jpg[j], (final_dst + "/") + img_list_jpg[j])
                        print("사진을 이동하였습니다.")
                    else:
                        print("해당하지 않습니다.")

                if count == 0:
                    print("사진 정보가 없습니다.")

        except TypeError:
            print("사진 날짜 정보가 없습니다.")
            j = j + 1
            continue

        else:
            j = j + 1


# 서버의 폴더를 분류할 때
def pic_period2(innoDate, folderName, extractFolder):
        
    # 날짜 추출
    start_year = innoDate[0:4]
    start_month = innoDate[6:8]
    start_day = innoDate[10:12]
        
    end_year = innoDate[14:18]
    end_month = innoDate[20:22]
    end_day = innoDate[24:26]

    # 정규 표현식 패턴 정의
    pattern = r'^\d{4}년 \d{2}월 \d{2}일-\d{4}년 \d{2}월 \d{2}일$'
    
    # # 문자열이 패턴과 일치하는지 검사
    match = re.match(pattern, folderName)
    if match: # 사용자 지정 폴더 이름 없음
        title = start_year + start_month + start_day + ' - ' + end_year + end_month + end_day
    else: # 사용자 지정 폴더 이름 있음
        title = folderName

    image_path = './ClassifyResult/' + extractFolder + '/'
    img_list = os.listdir(image_path)
    img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png") 
                    or img.endswith(".jpeg") or img.endswith(".JPEG") or img.endswith(".JPG") or img.endswith(".PNG")]
    img_list_np = []

    j = 0

    for i in img_list_jpg:

        img = Image.open(image_path + img_list_jpg[j])
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

                    '''
                                                1. pic_date_year < start_year => out
                                                2. pic_date_month < start_month => out
                                                3. pic_date_day < start_day => out
                                                4. pic_date_year > end_year => out
                                                5. pic_date_month > end_month => out
                                                6. pic_date_day > end_day => out

                                                정리

                                                pic_date_year < start_year or pic_date_year > end_year
                                                pic_date_month < start_month or pic_date_month > end_month
                                                pic_date_day < start_day or pic_date_day > end_day
                                                '''
                    moderate = True
                    # 시작/마감 년/월이 같고 날만 다를 때
                    if(pic_date_year == start_year == end_year and 
                        pic_date_month == start_month == end_month and 
                        start_day <= pic_date_day <= end_day): moderate = True
                    else: moderate = False

                    # 시작/마감 년은 같은데 달이 다를 때, 시작 월인데 날이 시작 날보다 이르거나, 마감 월인데 마감 일보다 늦거나
                    if(pic_date_year == start_year == end_year and start_month <= pic_date_month <= end_month):
                        if(start_month == pic_date_month and pic_date_day < start_day): moderate == False
                        elif(end_month == pic_date_month and pic_date_day > end_day): moderate == False
                        else: moderate = True

                    # 시작/마감 년/월/일 다를 때
                    elif(start_year <= pic_date_year <= end_year):
                        #사진 년도와 시작 년도가 같고, 사진 월이 시작 월보다 이를 때 false
                        if(pic_date_year == start_year and pic_date_month < start_month):moderate = False
                        #사진 년도/월이 시작 년도/월이 같고, 사진 날이 시작날보다 이를 때 false
                        elif(pic_date_year == start_year and pic_date_month == start_month and pic_date_day < start_day): moderate = False
                        #사진 년도와 시작 년도가 같고, 사진 월이 마감 월보다 뒤일 때 false
                        elif(pic_date_year == end_year and pic_date_month > end_month): moderate = False
                        #사진 년도/월과 마감 년도/월이 같고, 사진 날이 마감 날보다 뒤일 때 false
                        elif(pic_date_year == end_year and pic_date_month == end_month and pic_date_day > end_day): moderate = False
                        else: moderate = True
                            
                    else:
                        moderate == False

                    if moderate == True:
                        dst = './ClassifyResult/'
                        final_dst = dst + title 
                            

                        if os.path.exists(final_dst):
                            print("이미 파일이 존재합니다.")
                        else:
                            os.mkdir(final_dst)
                            print(title, " 파일이 생성되었습니다.")

                        img.close()
                        shutil.move(image_path + img_list_jpg[j], (final_dst + "/") + img_list_jpg[j])
                        print("사진을 이동하였습니다.")
                    else:
                        print("해당하지 않습니다.")

                if count == 0:
                    print("사진 정보가 없습니다.")

        except TypeError:
            print("사진 날짜 정보가 없습니다.")
            j = j + 1
            continue

        else:
            j = j + 1