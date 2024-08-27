from PIL import Image
from PIL.ExifTags import TAGS
import os
import numpy as np
import shutil

def sortDate(num):
    if num == 1:

        user = input("사진을 정리하고 싶은 날짜를 입력하세요(ex: 2024.03.04) : ")
        user_year = user[0:4]
        user_month = user[5:7]
        user_day = user[8:10]

        title = input("폴더 이름을 적어주세요(적지 않으면 날짜로 자동 생성됩니다.) : ")
        if len(title) == 0:
            title = user_year + user_month + user_day


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

                        # 하루만 정리할 땐 이 코드로 진행
                        if user_year == pic_date_year and user_month == pic_date_month and user_day == pic_date_day:
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
                            print("일치하지 않습니다.")

                if count == 0:
                    print("사진 날짜 정보가 없습니다.")

            except TypeError:
                print("사진 날짜 정보가 없습니다.")
                j = j + 1
                continue

            else:
                j = j + 1


    else:
        print("잘못된 접근입니다.")
        return 0