import os
# os.environ["TF_ENABLE_ONEDNN_OPTS"] = "0"
import shutil
import cv2
import matplotlib.pyplot as plt
from deepface import DeepFace
from datetime import datetime
# 얼굴

# 스마트폰 갤러리에서 받을 때
def detect_face(extractFolder):
    # Define paths
    source_folder = './' + extractFolder + '/' + extractFolder
    file_name = "Face_Detection_" + extractFolder # Face_Detection_detectFace
    file_name2 = "No_Face_Detection_" + extractFolder
    resultFolderPath = './ClassifyResult/'
    face_folder = resultFolderPath + file_name #./ClassifyResult/Face_Detection
    noface_folder = os.path.join(resultFolderPath, file_name2)

    now = datetime.now()
    print(now)

    # Get list of image files in the source directory
    image_files = [f for f in os.listdir(source_folder) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

    # Define detection model
    detection_model = 'retinaface'  # You can choose any from the detection_models list

    # Process each image
    for image_file in image_files:
        image_path = os.path.join(source_folder, image_file)

        try:
            # Load the image
            img = cv2.imread(image_path)

            # Try to detect faces
            faces = DeepFace.extract_faces(img_path=img, detector_backend=detection_model)

            if faces:  # If at least one face is detected
                # Move the image to the Face folder
                if not os.path.exists(face_folder):
                    os.mkdir(face_folder)
                shutil.move(image_path, os.path.join(face_folder, image_file))
                print(f"Face detected in {image_file}. Moved to Face folder.")
            else:
                # No face detected (although this case is unlikely with DeepFace)
                if not os.path.exists(noface_folder):
                    os.mkdir(noface_folder)
                shutil.move(image_path, os.path.join(noface_folder, image_file))
                print(f"No face detected in {image_file}. Moved to NoFace folder.")

        except Exception as e:
            # If an error occurs (e.g., no face detected, corrupt image), move to NoFace folder
            if not os.path.exists(noface_folder):
                os.mkdir(noface_folder)
            shutil.move(image_path, os.path.join(noface_folder, image_file))
            print(f"Error processing {image_file}: {e}. Moved to NoFace folder.")

# 서버에 있는 폴더 할 때
def detect_face2(extractFolder):
    # Define paths
    source_folder = './ClassifyResult/' + extractFolder     #./ClassifyResult/대전_test1
    file_name = "Face_Detection_" + extractFolder           # Face_Detection_대전_test1
    file_name2 = "No_Face_Detection_" + extractFolder       # No_Face_Detection_대전_test1
    resultFolderPath = './ClassifyResult/'
    face_folder = resultFolderPath + file_name              #./ClassifyResult/Face_Detection_대전_test1
    noface_folder = os.path.join(resultFolderPath, file_name2) #./ClassifyResult/No_Face_Detection_대전_test1

    now = datetime.now()
    print(now)

    # Get list of image files in the source directory
    image_files = [f for f in os.listdir(source_folder) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

    # Define detection model
    detection_model = 'retinaface'  # You can choose any from the detection_models list

    # Process each image
    for image_file in image_files:
        image_path = os.path.join(source_folder, image_file)

        try:
            # Load the image
            img = cv2.imread(image_path)

            # Try to detect faces
            faces = DeepFace.extract_faces(img_path=img, detector_backend=detection_model)

            if faces:  # If at least one face is detected
                # Move the image to the Face folder
                if not os.path.exists(face_folder):
                    os.mkdir(face_folder)
                shutil.move(image_path, os.path.join(face_folder, image_file))
                print(f"Face detected in {image_file}. Moved to Face folder.")
            else:
                # No face detected (although this case is unlikely with DeepFace)
                if not os.path.exists(noface_folder):
                    os.mkdir(noface_folder)
                shutil.move(image_path, os.path.join(noface_folder, image_file))
                print(f"No face detected in {image_file}. Moved to NoFace folder.")

        except Exception as e:
            # If an error occurs (e.g., no face detected, corrupt image), move to NoFace folder
            if not os.path.exists(noface_folder):
                os.mkdir(noface_folder)
            shutil.move(image_path, os.path.join(noface_folder, image_file))
            print(f"Error processing {image_file}: {e}. Moved to NoFace folder.")