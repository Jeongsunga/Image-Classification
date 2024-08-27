# 압축 파일을 푸는 코드를 작성
import zipfile
import os

def unzip_file(zip_file_path, extract_to_folder):

    # 절대 경로로 변환
    #absolute_folder_path = os.path.abspath(extract_to_folder)   

    # 추출할 폴더가 없으면 생성합니다.
    if not os.path.exists(extract_to_folder):
        os.makedirs(extract_to_folder)

    # ZIP 파일을 열고 추출합니다.
    with zipfile.ZipFile(zip_file_path, 'r') as zip_ref:
        zip_ref.extractall(extract_to_folder)
        print(f"'{zip_file_path}'를 '{extract_to_folder}'로 추출했습니다.")
