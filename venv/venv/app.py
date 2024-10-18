import math
import os
import shutil
from flask import Flask, request, jsonify, send_file, send_from_directory
from concurrent.futures import ThreadPoolExecutor, as_completed
from werkzeug.utils import secure_filename
from PIL import Image
from PIL.ExifTags import TAGS
import piexif
import sys
sys.path.append('./python/')
import face
import eyes
import pic_date
import period
import location
import meta_location
import extractZip

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


# 앱에서 분류할 압축 파일을 받고 분류까지 하는 라우터
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/post/folderZip', methods=['POST'])
def upload_file():
    global filterNumber, periodNumber, folderName, innoDate

    print('폰에서 데이터를 받았습니다.')

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

        extracted = './Extracted' # 압축 해제 완료한 폴더들을 모아놓는 곳
        os.makedirs(extracted, exist_ok=True)

        extractZip.unzip_file(zip_file_path, extract_to_folder)

        resultFolderPath = './ClassifyResult' # 분류 완료 폴더 저장할 폴더
        os.makedirs(resultFolderPath, exist_ok=True)

        if filterNumber == 1: # 얼굴
            face.detect_face(extract_to_folder)
        elif filterNumber == 2: # 얼굴&눈
            face.detect_face(extract_to_folder)
            eyes.detect_eyes(extract_to_folder)
        elif filterNumber == 3:
            if periodNumber == 1: # 하루
                pic_date.sortDate(innoDate, folderName, extract_to_folder)
            else: # 기간
                period.pic_period(innoDate, folderName, extract_to_folder)
        elif filterNumber == 4: # 위치
            location.sortLocation(extract_to_folder)
        else:
            print("잘못된 접근입니다.")

        # 현재 디렉터리 (app.py가 위치한 경로)
        current_dir = os.path.dirname(os.path.abspath(__file__))

        # A 폴더와 B 폴더 경로 설정
        source_dir = os.path.join(current_dir, extract_to_folder)
        destination_dir = os.path.join(current_dir, extracted, extract_to_folder)

        # A 폴더를 B 폴더 안으로 이동, 압축 푼 폴더들을 한 곳에 모아놓기 위함
        shutil.move(source_dir, destination_dir)

        # zip 파일 삭제
        #remove_file = './uploads/' + folderName + '.zip'
        #os.remove(remove_file)

        return jsonify({'message': 'File uploaded successfully', 'file_path': file_path}), 200


# 사용자가 분류할 폴더를 서버에서 선택한 경우를 처리하는 라우터
@app.route('/pick-server-folder', methods=['POST'])
def classify_server_folder():
    global filterNumber, periodNumber, folderName, innoDate

    print('서버에서 데이터를 받아옵니다.')

    data = request.json
    if data == '':
        return jsonify({"status": "fail", "message": "No selected folder"}), 400
    
    current_dir = os.path.dirname(os.path.abspath(__file__))
    test_folder_path = os.path.join(current_dir, 'ClassifyResult', data)

    if os.path.exists(test_folder_path) and os.path.isdir(test_folder_path):
        if filterNumber == 1: # 얼굴
            face.detect_face2(test_folder_path)
        elif filterNumber == 2: # 얼굴&눈
            face.detect_face2(test_folder_path)
            eyes.detect_eyes2(test_folder_path)
        elif filterNumber == 3:
            if periodNumber == 1: # 하루
                pic_date.sortDate2(innoDate, folderName, test_folder_path)
            else: # 기간
                period.pic_period2(innoDate, folderName, test_folder_path)
        elif filterNumber == 4: # 위치
            location.sortLocation2(test_folder_path)
        else:
            print("잘못된 접근입니다.")

    else:
        print("test 폴더가 존재하지 않습니다.")
        return jsonify({"status": "fail", "message": "No existed folder"}), 400

    return jsonify({'message': 'File uploaded successfully', 'file_path': file_path}), 200


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


BASE_IMAGE_DIR = './ClassifyResult/'

# 사용자가 선택한 폴더의 이미지 url 목록 반환
@app.route('/get_images/<folder_name>', methods=['GET'])
def get_images(folder_name):
    print(folder_name)
    folder_path = BASE_IMAGE_DIR + folder_name #'./ClassifyResult/서울_test1'
    if not os.path.isdir(folder_path):
        return jsonify({'error': 'Folder not found'}), 404
    
    images = [f for f in os.listdir(folder_path) if f.endswith(('.png', '.jpg', '.jpeg'))]
    image_urls = [f'http://192.168.7.10:5000/images/{folder_name}/{image}' for image in images]

    return jsonify(image_urls)

# 이미지를 실제로 클라이언트로 전달하는 라우터
@app.route('/images/<folder_name>/<filename>')
def serve_image(folder_name, filename):
    folder_path = os.path.join(BASE_IMAGE_DIR, folder_name)
    file_path = os.path.join(folder_path, filename)
    if not os.path.isfile(file_path):
        return jsonify({'error': 'File not found'}), 404
    return send_from_directory(folder_path, filename)

# 폰에서 이미지의 링크를 받고 해당 이미지의 메타 데이터를 넘겨주는 라우터
@app.route('/image_metadata', methods=['POST'])
def image_metadata():
    image_url = request.data.decode('utf-8')  # 문자열 데이터로 받기

    image_path = image_url.replace("http://192.168.7.10:5000/images", 
                                    "C:/Image-Classification-Application-test/venv/venv/ClassifyResult")
    image_path = image_path.strip('"')
    image_path = os.path.normpath(image_path)

    # 파일이 실제로 존재하는지 확인
    if not os.path.exists(image_path):
        print(f"File does not exist at path: {image_path}")
        return jsonify({"error": "File not found"}), 404

    try:
        # 파일 메타데이터 추출
        file_size = os.path.getsize(image_path)  # 파일 크기 (바이트 단위)
        file_volume = convert_size(file_size)  # 사람이 읽기 쉬운 단위로 변환
        file_name = os.path.basename(image_path)  # 파일명

        # EXIF 데이터(촬영 날짜) 추출
        img = Image.open(image_path)
        exif_data = img._getexif()
        capture_date = "Unknown"
        if exif_data:
            for tag, value in exif_data.items():
                tag_name = TAGS.get(tag, tag)
                if tag_name == 'DateTimeOriginal' or tag_name == 'DateTime':
                    capture_date = value

        # 촬영 위치 추출
        address = meta_location.metaLocation(image_path)

        print(file_name, file_volume, capture_date, address)

        metadata = {
            "file_name": file_name,
            "file_size": file_volume,
            "capture_date": capture_date,
            "address": address
        }

        return jsonify(metadata), 200

    except FileNotFoundError:
        return jsonify({"error": "File not found"}), 404

# 이미지의 크기를 사용자가 보기 편한 단위로 바꿔주는 함수
def convert_size(size_bytes):
    if size_bytes == 0:
        return "0B"
    size_name = ("B", "KB", "MB", "GB", "TB", "PB")
    i = int(math.floor(math.log(size_bytes, 1024)))
    p = math.pow(1024, i)
    s = round(size_bytes / p, 2)
    return f"{s} {size_name[i]}"

# 사용자가 삭제하길 원하는 이미지의 링크(한 장)를 받고 삭제 수행 후
# 폴더 내의 이미지 장수, 업데이트된 이미지 링크를 반환하는 라우터
@app.route("/delete-image", methods=['POST'])
def delete_image():
    
    try:
        image_url = request.data.decode('utf-8')  # 문자열 데이터로 받기

        # 이미지 파일 경로 추출 (로컬 경로로 변환)
        image_path = image_url.replace("http://192.168.7.10:5000/images", "C:/Image-Classification-Application-test/venv/venv/ClassifyResult")

        # 불필요한 따옴표 제거
        image_path = image_path.strip('"')

        # 운영체제에 맞는 경로 구분자로 변환
        image_path = os.path.normpath(image_path)

        # 디버깅을 위한 경로 출력
        print(f"Converted image path: {image_path}")

        # 파일이 실제로 존재하는지 확인
        if not os.path.exists(image_path):
            print(f"File does not exist at path: {image_path}")
            return jsonify({"error": "File not found"}), 404
    
        os.remove(image_path)
        print('삭제되었습니다.')
    
        # 해당 폴더 내의 이미지 개수 확인
        folder_path = os.path.dirname(image_path)
        image_files = [file for file in os.listdir(folder_path) if file.endswith(('jpg', 'jpeg', 'png'))]
        image_count = len(image_files)
        folder_name = os.path.basename(folder_path)

        base_url = "http://192.168.7.10:5000/images"

        image_links = [f"{base_url}/{folder_name}/{file}" for file in image_files]

        # 결과 반환
        return jsonify({
            'success': True,
            'image_count': image_count,
            'image_links': image_links
        })
        #return ({"status": "success", "message": "Data received successfully"})
    
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

# 사용자가 삭제하길 원하는 폴더의 이름을 받고 삭제 수행 &
# 서버에 남은 폴더 목록을 반환하는 라우터
@app.route("/delete-folder", methods=['POST'])
def delete_folder():
    data = request.json
    delete_folder_path = "C:/Image-Classification-Application-test/venv/venv/ClassifyResult/" + data
    print(delete_folder_path)

    if os.path.exists(delete_folder_path):
        shutil.rmtree(delete_folder_path)
        return jsonify({"status": "success", "message": "Data received successfully"})

    else:
        return jsonify({"status": "error", "message": "No Existes Folder"}), 404

# 사용자가 삭제하길 원하는 이미지 리스트 링크(여러 장)를 받고 수행 &
# 폴더에 남은 이미지 링크 리스트와 사진 장 수 반환 라우터
@app.route("/delete-images", methods=['POST'])
def delete_images(): 
    try:
        data = request.json
        print(data)

        for url in data:
            if not url:
                return jsonify({'success': False, 'error': 'No image URLs provided'}), 400

            image_path = url.replace("http://192.168.7.10:5000/images", "C:/Image-Classification-Application-test/venv/venv/ClassifyResult")
            image_path = image_path.strip('"')
            image_path = os.path.normpath(image_path)

            if os.path.exists(image_path):
                os.remove(image_path)
                print(f'Image deleted: {image_path}')
            else:
                print(f'File does not exist at path: {image_path}')
                #return jsonify({"error": "File not found"}), 404
            
        # 해당 폴더 내의 남아있는 이미지 개수 확인 (첫 이미지의 경로 기준)
        folder_path = os.path.dirname(image_path)
        image_files = [file for file in os.listdir(folder_path) if file.endswith(('jpg', 'jpeg', 'png'))]
        image_count = len(image_files)
        folder_name = os.path.basename(folder_path)

        base_url = "http://192.168.7.10:5000/images"
        image_links = [f"{base_url}/{folder_name}/{file}" for file in image_files]

        return jsonify({
            'success': True,
            'image_count': image_count,
            'image_links': image_links
        })
    
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)