import cv2
import os, glob
eye_cascPath = "../python/Resource/haarcascade_eye_tree_eyeglasses.xml"  #eye detect model
face_cascPath = "../python/Resource/haarcascade_frontalface_default.xml"  #front_face detect model
faceCascade = cv2.CascadeClassifier(face_cascPath)
eyeCascade = cv2.CascadeClassifier(eye_cascPath)

cnt = 10000
base_dir = './faces/No_Face_Detection'
base_dir2 = './faces/Face_Detection'

if not os.path.exists(base_dir2):
    os.mkdir(base_dir2)

dirs = [d for d in glob.glob(base_dir) if os.path.isdir(d)]
for dir in dirs:
    files = glob.glob(dir+'/*.jpg')
    print('\t path:%s, %dfiles'%(dir, len(files)))
    for file in files:
        img = cv2.imread(file)
        frame = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        # Detect faces in the image
        faces = faceCascade.detectMultiScale(
            frame,
            scaleFactor=1.1,
            minNeighbors=5,
            minSize=(30, 30),
            # flags = cv2.CV_HAAR_SCALE_IMAGE
        )
        # print("Found {0} faces!".format(len(faces)))
        if len(faces) > 0:
            file_name_path = os.path.join(base_dir2, str(cnt) + '.jpg')
            cnt += 1
            img = cv2.imread(file)
            cv2.imwrite(file_name_path, img)
            # Draw a rectangle around the faces
            for (x, y, w, h) in faces:
                cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), 2)
            frame_tmp = img[faces[0][1]:faces[0][1] + faces[0][3], faces[0][0]:faces[0][0] + faces[0][2]:1, :]
            frame = frame[faces[0][1]:faces[0][1] + faces[0][3], faces[0][0]:faces[0][0] + faces[0][2]:1]
            eyes = eyeCascade.detectMultiScale(
                frame,
                scaleFactor=1.1,
                minNeighbors=5,
                minSize=(30, 30),
                # flags = cv2.CV_HAAR_SCALE_IMAGE
            )
            if len(eyes) == 0:
                print('no eyes!!!')
            else:
                print(file)
                print('eyes!!!')
            # cv2.imshow('img', img)
            # if cv2.waitKey(0) == ord('q') or cv2.waitKey(0) == ord('Q'):
            #     cv2.destroyAllWindows()