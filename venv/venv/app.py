import os
from flask import Flask, request, jsonify
import extractZip
from concurrent.futures import ThreadPoolExecutor, as_completed
import sys
sys.path.append('./python/')
import yunet_face
import Example
import pic_date
import period
import location

app = Flask(__name__)

# POST 요청을 받을 수 있는 간단한 API 엔드포인트
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

@app.route('/get/folderZip', methods=['POST'])
def upload_file():
    global filterNumber, periodNumber, folderName, innoDate
    # 'uploaded_file' 은 앱에서 업로드한 폴더 이름을 찾기 쉽게 키로 설정한 값
    if 'uploaded_file' not in request.files:
        print("No file part")
        return jsonify({'error': 'No file part'}), 400
    
    # 앱에서 json 형태로 보내도 flask에서는 딕셔너리 형태로 받아오기 때문에 바로 추출 가능
    file = request.files['uploaded_file']
    
    if file.filename == '':
        print("No selected file")
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

        if filterNumber == 1:
            print("얼굴 인식합니다.")
            yunet_face.detect_face(extract_to_folder)
        elif filterNumber == 2:
            print("얼굴과 눈을 인식합니다.")
            Example.detect_eyes(extract_to_folder)
        elif filterNumber == 3:
            print("날짜에 따른 분류입니다.")
            if periodNumber == 1: #하루
                pic_date.sortDate(innoDate, folderName, extract_to_folder)
            else:
                period.pic_period(innoDate, folderName, extract_to_folder)
        elif filterNumber == 4:
            location.sortLocation(extract_to_folder)
        else:
            print("잘못된 접근입니다.")

        return jsonify({'message': 'File uploaded successfully', 'file_path': file_path}), 200
    

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
