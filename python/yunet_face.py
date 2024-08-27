import cv2
import os, glob
import matplotlib.pyplot as plt
from datetime import datetime

detector = cv2.FaceDetectorYN.create("../python/Resource/face_detection_yunet_2023mar.onnx", "", (0, 0))

cnt = 0
img_list_np = []
base_dir = './faces'
file_name = "Face_Detection"
file_name2 = "No_Face_Detection"
detect_dir = os.path.join(base_dir, file_name)
detect_dir2 = os.path.join(base_dir, file_name2)


if not os.path.exists(detect_dir):
    os.mkdir(detect_dir)

if not os.path.exists(detect_dir2):
    os.mkdir(detect_dir2)

now = datetime.now()
print(now)
dirs = [d for d in glob.glob(base_dir) if os.path.isdir(d)]
for dir in dirs:
    files = glob.glob(dir+'/*.jpg')
    print('\t path:%s, %dfiles'%(dir, len(files)))
    for file in files:
        image_cv2_yunet = cv2.imread(file)
        height, width, _ = image_cv2_yunet.shape
        detector.setInputSize((width, height))
        _, faces = detector.detect(image_cv2_yunet)
        # if faces[1] is None, no face found
        print()
        if faces is not None: # 얼굴 탐지 OK
            for face in faces:
                # parameters: x1, y1, w, h, x_re, y_re, x_le, y_le, x_nt, y_nt, x_rcm, y_rcm, x_lcm, y_lcm

                # bouding box
                box = list(map(int, face[:4]))
                color = (0, 0, 255)
                cv2.rectangle(image_cv2_yunet, box, color, 5)

                # confidence
                confidence = face[-1]
                confidence = "{:.2f}".format(confidence)
                position = (box[0], box[1] - 10)
                cv2.putText(image_cv2_yunet, confidence, position, cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 3, cv2.LINE_AA)

            print("ok" + file)
            file_name_path = os.path.join(detect_dir, str(cnt) + '.jpg')
            cnt += 1
            img = cv2.imread(file)
            cv2.imwrite(file_name_path, img)
            # plt.figure()
            # plt.imshow(image_cv2_yunet)
            # plt.show()
            # plt.axis('off')
            # plt.close()

        else: # 얼굴 탐지 NO
            print("no" + file)
            file_name_path = os.path.join(detect_dir2, str(cnt) + '.jpg')
            cnt += 1
            img = cv2.imread(file)
            cv2.imwrite(file_name_path, img)
            # plt.figure()
            # plt.imshow(image_cv2_yunet)
            # plt.show()
            # plt.axis('off')
            # plt.close()

now = datetime.now()
print(now)