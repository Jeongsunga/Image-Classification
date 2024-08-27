'''
정승아, 정호정 졸업 작품 중, 메타 데이터를 활용한 사진 분류 코드를 나타냄
GPS 정보의 위도, 경도를 통한 지역별 구분과 사진 촬영 날짜에 따른 구분 2가지가 있다.
미리 짜놓은 두 가지 코드를 여기서 합쳐서 통합적으로 관리할 수 있도록 함
'''

import location
import pic_date
import period

answer = int(input("어떤 방식으로 분류할지 선택해주세요(1 : 위치, 2: 날짜, 3: 얼굴 인식) : "))

if answer == 1:
    print("위치 정보에 따른 분류입니다.")
    location.sortLocation(answer)

elif answer == 2:
    print("촬영 날짜에 따른 분류입니다.")
    user = int(input("하루만 정리 할려면 1, 여러 날을 정리하려면 2를 눌러주세요 : "))
    if user == 1:
        pic_date.sortDate(user)
    elif user == 2:
        period.pic_period(user)
    else:
        print("잘못된 접근입니다.")

elif answer == 3:
    user = int(input("얼굴 인식 중, 눈도 함께 인식하시겠습니까?(네: 1, 아니요: 2) : "))
    if user == 1:
        '''얼굴 인식 파일로 이동, 값을 넘겨주고 return으로 다시 main으로 돌아와야 함'''
    elif user == 2:
        '''얼굴&눈 인식 파일로 이동, 값을 넘겨주고 return으로 다시 main으로 돌아와야 함'''
    else:
        print("잘못된 접근입니다.")

else:
    print("잘못된 접근입니다.")

print("분류 완료되었습니다.")