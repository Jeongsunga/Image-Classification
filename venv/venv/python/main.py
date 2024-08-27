'''
정승아, 정호정 졸업 작품 중, 메타 데이터를 활용한 사진 분류 코드를 나타냄
GPS 정보의 위도, 경도를 통한 지역별 구분과 사진 촬영 날짜에 따른 구분 2가지가 있다.
미리 짜놓은 두 가지 코드를 여기서 합쳐서 통합적으로 관리할 수 있도록 함
'''
import location
import pic_date
import sys
import os
import period
import yunet_face 
import Example
from concurrent.futures import ThreadPoolExecutor
import asyncio

# Executor 설정 (쓰레드 풀)
executor = ThreadPoolExecutor()

# 비동기 얼굴 인식 함수
async def detect_face_async(extractFolder):
    loop = asyncio.get_event_loop()
    # 백그라운드에서 얼굴 인식 함수 실행
    result = await loop.run_in_executor(executor, yunet_face.detect_face, extractFolder)
    return result

# 비동기 촬영 위치 분류 함수
async def location_async(num, extractFolder):
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor(executor, location.sortLocation, num, extractFolder)
    return result

# 비동기 얼굴&눈 인식 함수
async def detect_eyes_async(extractFolder):
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor(executor, Example.detect_eyes, extractFolder)
    return result

# 비동기 촬영 날짜가 하루 분류 함수
async def oneDay_async(periodNumber, folderName, extractFolder):
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor
    (executor, pic_date.sortDate, periodNumber, folderName, extractFolder)
    return result

# 비동기 촬영 날짜 기간 분류 함수
async def Period_async(periodNumber, folderName, extractFolder):
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor
    (executor, period.pic_period, periodNumber, folderName, extractFolder)
    return result


async def main():
    if len(sys.argv) < 5:
        print("Usage: main.py <필터 번호> <하루/기간> <폴더 이름> <압축한 파일 이름>")
        return
    
    '''
    분류 방법
    option == 1: 얼굴만 인식
    option == 2: 얼굴과 눈 인식
    option == 3: 촬영 날짜
    option == 4: 촬영 위치 
    '''
    filterNumber = int(sys.argv[1])  # 첫 번째 인자: 분류 방법
    periodNumber = int(sys.argv[2])  # 두 번째 인자: 날짜 처리 하루(1)/기간(2)
    folderName = sys.argv[3]  # 세 번째 인자: 날짜 처리시, 폴더 이름
    extractFolder = sys.argv[4] # 네 번째 인자 : 압축 해제한 폴더 이름
    resultFolderPath = '././ClassifyResult' # 분류 완료 폴더 저장할 폴더

    os.makedirs(resultFolderPath, exist_ok=True)

    if filterNumber == 1:
        print("얼굴만 인식합니다.")
        #yunet_face.detect_face(extractFolder)
        await detect_face_async(extractFolder)

    elif filterNumber == 2:
        print("얼굴과 눈을 인식합니다.")
        await detect_eyes_async(extractFolder) 
    
    elif filterNumber == 3:
        print("날짜에 따른 분류입니다.")
        if periodNumber == 1: # 하루
            await oneDay_async(periodNumber, folderName, extractFolder)
        else: # 기간
            await Period_async(periodNumber, folderName, extractFolder)

    elif filterNumber == 4:
        print("위치 정보에 따른 분류입니다.")
        await location_async(filterNumber, extractFolder)

    else:
        print("잘못된 접근입니다.")
        return

    return


if __name__ == "__main__":
    asyncio.run(main())