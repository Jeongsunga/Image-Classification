import os
import subprocess
from flask import Flask, request, jsonify
import extractZip
from concurrent.futures import ThreadPoolExecutor, as_completed
import asyncio

app = Flask(__name__)
executor = ThreadPoolExecutor(2)

async def run_subprocess(command):
    process = await asyncio.create_subprocess_exec(
        *command,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    stdout, stderr = await process.communicate()

    if process.returncode == 0:
        print("SubProcess finished successfully")
        print(stdout.decode())
    else:
        print("SubProcess failed")
        print(stderr.decode())

def run_subprocess_sync(command):
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(run_subprocess(command))

# POST 요청을 받을 수 있는 간단한 API 엔드포인트
@app.route('/api/data', methods=['POST'])
def receive_data():
    data = request.json  # 클라이언트에서 보낸 JSON 데이터를 받음
    print(f"Received data: {data}")
    # 데이터에 대한 처리(예: 데이터베이스 저장, 비즈니스 로직 등)
    
    # 응답으로 JSON 데이터를 반환
    return jsonify({"status": "success", "message": "Data received successfully"})


filterNumber = 1 # 필터 4가지 중에서 전역변수로 사용하기 위한 초기 선언
periodNumber = 1 # 하루, 기간 중에서 전역변수로 사용하기 위한 초기 선언
folderName = ""  # 폴더 이름, 전역변수 초기 선언

# 사용자가 앱에서 선택한 분류 방식 값을 받는 라우터
@app.route('/filterNumber', methods=['POST'])
def receiveFilterNumber():
    data = request.json # {"filterNumber":1}
    filterNumber = data['filterNumber']
    print("Receive filter number : ", filterNumber)
    return jsonify({"status": "success", "message": "Data received successfully"})


# 사용자가 앱에서 선택한 날짜를 받는 라우터
@app.route('/filterNumber/date', methods=['POST'])
def receiveFilterNumberDate():
    data = (request.json)
    periodNumber = data['periodNumber']
    folderName = data['folderName']

    print(periodNumber, folderName)
    return jsonify({"status": "success", "message": "Data received successfully"})


# 앱에서 분류할 압축 파일을 받는 라우터
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/get/folderZip', methods=['POST'])
def upload_file():
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

        # 앱에서 입력받은 filterNumber에 따라 다른 분류 코드 실행, 비동기처리 필수!
        command = ['python3', './python/main.py', str(filterNumber), str(periodNumber), folderName, extract_to_folder]
        executor.submit(run_subprocess_sync, command)
        #subprocess.run(command, capture_output=True, text=True)
    
        return jsonify({'message': 'File uploaded successfully', 'file_path': file_path}), 200
    

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
