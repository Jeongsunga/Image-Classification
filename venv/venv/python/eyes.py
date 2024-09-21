import cv2
import dlib
import numpy as np
import os
import shutil
#눈
def detect_eyes(extractFolder):
    # EAR 계산 함수 (눈 감김 상태를 판별하기 위한 함수)
    def calculate_EAR(eye_points):
        A = np.linalg.norm(eye_points[1] - eye_points[5])
        B = np.linalg.norm(eye_points[2] - eye_points[4])
        C = np.linalg.norm(eye_points[0] - eye_points[3])
        EAR = (A + B) / (2.0 * C)
        return EAR

    # EAR 임계값 (이 값보다 낮으면 눈이 감겨있다고 판단)
    EAR_THRESHOLD = 0.23

    # dlib 얼굴 탐지기와 랜드마크 예측기 불러오기
    detector = dlib.get_frontal_face_detector()
    predictor = dlib.shape_predictor('./python/Resource/shape_predictor_81_face_landmarks.dat')

    # 경로 설정
    source_folder = './ClassifyResult/Face_Detection_' + extractFolder
    file_name = "Eyes_Detection_" + extractFolder # Eye_Detection_detectFace
    file_name2 = "No_Eyes_Detection_" + extractFolder
    resultFolderPath = './ClassifyResult/'
    result_eyes_folder = resultFolderPath + file_name
    result_no_eyes_folder = os.path.join(resultFolderPath, file_name2)

    # 눈 랜드마크 좌표 (68포인트 모델 기준)
    LEFT_EYE_POINTS = list(range(36, 42))
    RIGHT_EYE_POINTS = list(range(42, 48))

    # 이미지 파일들을 순회하며 눈 상태 확인 및 이동
    for filename in os.listdir(source_folder):
        if filename.endswith(('.jpg', '.jpeg', '.png')):
            file_path = os.path.join(source_folder, filename)
            img = cv2.imread(file_path)

            # 이미지가 로드되지 않으면 스킵
            if img is None:
                print(f"이미지를 불러올 수 없습니다: {file_path}")
                continue

            # 얼굴 탐지
            dets = detector(img, 1)

            # 얼굴이 감지되지 않으면 스킵
            if len(dets) == 0:
                os.makedirs(result_no_eyes_folder, exist_ok=True)
                shutil.move(file_path, os.path.join(result_no_eyes_folder, filename))
                print(f"얼굴이 감지되지 않았습니다: {file_path}")
                continue

            # 각 얼굴에 대해 랜드마크를 찾고 눈 상태 판단
            eyes_open = False
            for k, d in enumerate(dets):
                shape = predictor(img, d)

                # 왼쪽 눈과 오른쪽 눈 랜드마크 좌표 추출
                left_eye_points = np.array([[shape.part(i).x, shape.part(i).y] for i in LEFT_EYE_POINTS])
                right_eye_points = np.array([[shape.part(i).x, shape.part(i).y] for i in RIGHT_EYE_POINTS])

                # 왼쪽 눈과 오른쪽 눈 EAR 계산
                left_EAR = calculate_EAR(left_eye_points)
                right_EAR = calculate_EAR(right_eye_points)

                # 평균 EAR 계산
                avg_EAR = (left_EAR + right_EAR) / 2.0

                # 눈이 떠 있으면 eyes_open을 True로 설정
                if avg_EAR >= EAR_THRESHOLD:
                    eyes_open = True

            # 눈 상태에 따라 이미지를 이동
            if eyes_open:
                os.makedirs(result_eyes_folder, exist_ok=True)
                shutil.move(file_path, os.path.join(result_eyes_folder, filename))
                print(f"눈이 떠 있음: {filename} -> {result_eyes_folder}")
            else:
                os.makedirs(result_no_eyes_folder, exist_ok=True)
                shutil.move(file_path, os.path.join(result_no_eyes_folder, filename))
                print(f"눈이 감겨 있음: {filename} -> {result_no_eyes_folder}")

    os.rmdir(source_folder)
    print("눈 상태 검사 완료!")
