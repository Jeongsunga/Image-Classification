import os
from flask import Flask, request, jsonify, send_file, send_from_directory
import extractZip
from concurrent.futures import ThreadPoolExecutor, as_completed
from werkzeug.utils import secure_filename
import json
import zipfile
import sys
sys.path.append('./python/')
import yunet_face
import Example
import pic_date
import period
import location

app = Flask(__name__)

# POST 요청을 받을 수 있는 간단한 API 엔드포인트, mainActivity의 버튼 누르면 확인 가능
@app.route('/api/data', methods=['POST'])
def receive_data():
    data = request.json  # 클라이언트에서 보낸 JSON 데이터를 받음
    print(f"Received data: {data}")
    # 데이터에 대한 처리(예: 데이터베이스 저장, 비즈니스 로직 등)
    
    # 응답으로 JSON 데이터를 반환
    return jsonify({"status": "success", "message": "Data received successfully"})


global filterNumber
global periodNumber
global folderName
global innoDate

# 사용자가 앱에서 선택한 분류 방식 값을 받는 라우터
@app.route('/filterNumber', methods=['POST'])
def receiveFilterNumber():
    global filterNumber, periodNumber, folderName
    data = request.json
    filterNumber = data['filterNumber']
    if filterNumber != 3: # 얼굴, 눈, 위치이면 
        periodNumber = 0  # 하루/기간 = 0, 생성할 폴더 이름 = 빈 값으로 넘기기
        folderName = ""
    print("filterNumber : ", filterNumber)
    return jsonify({"status": "success", "message": "Data received successfully"})


# 사용자가 앱에서 선택한 날짜를 받는 라우터
@app.route('/filterNumber/date', methods=['POST'])
def receiveFilterNumberDate():
    global periodNumber, folderName, innoDate
    data = (request.json)
    periodNumber = data['periodNumber']
    folderName = data['folderName']
    innoDate = data['innoDate']
    print('분류 방식 : ', periodNumber, '날짜 : ', innoDate, '폴더 이름 : ', folderName)
    return jsonify({"status": "success", "message": "Data received successfully"})


# 앱에서 분류할 압축 파일을 받는 라우터
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/post/folderZip', methods=['POST'])
def upload_file():
    global filterNumber, periodNumber, folderName, innoDate

    if 'uploaded_file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    
    file = request.files['uploaded_file']
    
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    if file:
        # zip 파일이 uploads 폴더(로컬 저장소)에 저장됨, 서버를 꺼도 삭제되지 않음
        file_path = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(file_path)
        
        # 파일을 성공적으로 저장한 경우
        print("File uploaded successfully : ", file.filename)

        zip_file_path = './uploads/' + file.filename  # ZIP 파일의 경로
        extract_to_folder = file.filename.replace('.zip', '') # 압축을 풀고난 결과 파일을 저장할 폴더 이름
        extractZip.unzip_file(zip_file_path, extract_to_folder)

        resultFolderPath = './ClassifyResult' # 분류 완료 폴더 저장할 폴더
        os.makedirs(resultFolderPath, exist_ok=True)

        if filterNumber == 1: # 얼굴
            yunet_face.detect_face(extract_to_folder)
        elif filterNumber == 2: # 얼굴&눈
            Example.detect_eyes(extract_to_folder)
        elif filterNumber == 3:
            if periodNumber == 1: # 하루
                pic_date.sortDate(innoDate, folderName, extract_to_folder)
            else: # 기간
                period.pic_period(innoDate, folderName, extract_to_folder)
        elif filterNumber == 4: # 위치
            location.sortLocation(extract_to_folder)
        else:
            print("잘못된 접근입니다.")

        return jsonify({'message': 'File uploaded successfully', 'file_path': file_path}), 200
    

# 사진의 위치 정보만 모아놓은 Json 파일을 받는 라우터
# @app.route('/post/gpsJson', methods=['POST'])
# def getGpsJson():
#     # 'file'이라는 키로 파일을 가져옴
#     if 'file' not in request.files:
#         return jsonify({"status": "failure", "message": "No file part in the request"})
    
#     file = request.files['file']

#     # 파일이 없거나 파일명이 없는 경우 처리
#     if file.filename == '':
#         return jsonify({"status": "failure", "message": "No selected file"})
    
#     # 파일이 JSON 파일인지 확인
#     if file and file.filename.endswith('.json'):
#         try:
#             # 파일을 읽어 JSON으로 변환
#             json_data = json.load(file)
#             print(json_data)

#             # 경로 지정 및 폴더 생성
#             upload_folder = './uploads'
#             file_path = os.path.join(upload_folder, file.filename)

#             # JSON 데이터를 파일에 저장
#             with open(file_path, 'w') as output_file:
#                 json.dump(json_data, output_file, indent=4)

#             # 저장된 파일 확인 로그
#             print(f"File saved to {file_path}")
            
#             return jsonify({"status": "success", "message": "Data received and saved successfully"})
#         except json.JSONDecodeError:
#             return jsonify({"status": "failure", "message": "Invalid JSON format"})
#         except Exception as e:
#             return jsonify({"status": "failure", "message": str(e)})
#     else:
#         return jsonify({"status": "failure", "message": "Invalid file format"})

# 분류 완료된 결과를 사용자 앱으로 JSON 형태로 보내는 라우터
@app.route('/get/folderList', methods=['GET'])
def send_result():
    base_path = './ClassifyResult'
    result = []

    for folder_name in os.listdir(base_path):
        folder_path = os.path.join(base_path, folder_name)
        
        if os.path.isdir(folder_path):  # Check if it is a directory
            photos = [f for f in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, f))]
            photo_count = len(photos)
            first_photo_path = os.path.join(folder_name, photos[0]) if photo_count > 0 else None
            
            result.append({
                'folder_name': folder_name,
                'photo_count': photo_count,
                'first_photo': first_photo_path  # Relative path to the first photo
            })

    return jsonify(result)

# 특정 폴더와 파일의 존재 여부를 확인하고 파일을 반환하는 라우터
@app.route('/get/folderList/<folderName>/<fileName>', methods=['GET'])
def get_file(folderName, fileName):
    base_path = './ClassifyResult'
    file_path = os.path.join(base_path, folderName, fileName)
    
    if os.path.isfile(file_path):
        return send_file(file_path)
    else:
        return jsonify({'error': 'File not found'}), 404
    
BASE_IMAGE_DIR = './ClassifyResult'
# 사용자가 선택한 폴더의 이미지를 JSON 형태로 생성하는 라우터
@app.route('/get_images', methods=['GET'])
def get_images():
    
    folder_name = request.args.get('folder')
    if not folder_name:
        return jsonify({'error': 'Folder name is required'}), 400

    folder_path = os.path.join(BASE_IMAGE_DIR, folder_name)

    if not os.path.isdir(folder_path):
        return jsonify({'error': 'Folder does not exist'}), 404

    images = []
    for file_name in os.listdir(folder_path):
        file_path = os.path.join(folder_path, file_name)
        if os.path.isfile(file_path) and file_name.lower().endswith(('.png', '.jpg', '.jpeg')):
            images.append({
                'file_name': file_name,
                'url': f'/images/{folder_name}/{file_name}'
            })

    return jsonify(images)

# 생성된 이미지 링크 리스트 JSON을 클라이언트로 직접 보내는 라우터
@app.route('/images/<folder>/<filename>', methods=['GET'])
def serve_image(folder, filename):
    file_path = os.path.join(BASE_IMAGE_DIR, folder, filename)
    if not os.path.isfile(file_path):
        return jsonify({'error': 'File not found'}), 404
    return send_from_directory(os.path.join(BASE_IMAGE_DIR, folder), filename)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
