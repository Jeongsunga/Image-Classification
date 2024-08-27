from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil

def pic_period(user):
    if user == 2:
        
        start = input("사진을 정리하고 싶은 시작 날짜를 입력하세요(ex: 2024.03.04) : ")
        start_year = start[0:4]
        start_month = start[5:7]
        start_day = start[8:10]
        
        end = input("사진을 정리하고 싶은 마감 날짜를 입력하세요(ex: 2024.03.04) : ")
        end_year = end[0:4]
        end_month = end[5:7]
        end_day = end[8:10]

        title = input("폴더 이름을 적어주세요(적지 않으면 날짜로 자동 생성됩니다.) : ")
        if len(title) == 0:
            title = start_year + start_month + start_day + " - " + end_year + end_month + end_day

        image_path = "C:/Users/정호정/PycharmProjects/metadata/testfile/"
        img_list = os.listdir(image_path)
        img_list_jpg = [img for img in img_list if img.endswith(".jpg") or img.endswith(".png")]
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
                        if(pic_date_year == start_year == end_year and pic_date_month == start_month == end_month and start_day <= pic_date_day <= end_day): moderate = True
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
                            dir_name = title
                            dst = image_path + dir_name + "/"

                            if os.path.exists(image_path + dir_name):
                                print("이미 파일이 존재합니다.")
                            else:
                                os.mkdir(image_path + dir_name)
                                print(dir_name, " 파일이 생성되었습니다.")

                            img.close()
                            shutil.move(image_path + img_list_jpg[j], dst + img_list_jpg[j])
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


    else:
        print("잘못된 접근입니다.")
        return 0