<h1 style='background-color: rgba(55, 55, 55, 0.4); text-align: center'>AutoClassify</h1>
컴퓨터 비전과 메타데이터를 활용한 이미지 자동 분류 어플입니다.  
플라스크 서버를 이용하여 이미지 분류를 수행합니다.

---

## 목차
1. [소개](#소개)
2. [기능](#기능)
3. [설치 방법](#설치-방법)
4. [폴더 구조](#폴더-구조)
5. [기술 스택](#기술-스택)
6. [시연 영상](#시연-영상)
7. [팀 구성](#팀-구성)
8. [라이선스](#라이선스)

---

## 소개
- 목적 : 사용자가 다량의 사진을 정리하는 것을 돕기 위한 어플리케이션 서비스를 제공하는 것입니다.
- 목표 : 사용자가 최고의 사진을 고르는 방법은 각자의 주관적인 기준을 따르므로 명확한 기준을 설정할 필요가 있습니다.  
우리는 최적의 사진을 찾아주기보다는 사용자 입장에서 삭제할 것이라 여겨지는 사진을 골라주는 것을 우선순위로 합니다.

---

## 기능
- 얼굴 유무에 따른 분류
- 눈이 떠져있는지 감겨있는지에 따른 분류
- 사진 촬연 날짜에 따른 분류
- 사진 촬영 위치에 따른 분류
- 이미지 찜하기 기능
- 서버 -> 로컬 이미지 삭제/저장 등

---

## **설치 방법**
1. Prerequisites:
    - Android Studio, JDK 17, Python 3.8.10

2. **클론 및 빌드**
    ```bash
    git clone https://github.com/Jeongsunga/Image-Classification
    ```
    - Android Studio에서 프로젝트 열기
    
3. **APK 설치**
    - 생성된 APK 파일을 기기에 설치하거나 에뮬레이터에서 실행합니다.

4. **Flask 가상 환경 실행**
   ```bash
   cd Scrips
   activate.bat
   cd venv
   python app.py
   ```

5. **라이브러리 설치**:
    ```bash
    pip install deepface
    pip install dlib
    pip install PIL
    pip install numpy
    pip install dotenv
   ```

  - dlib 설치 오류시, 아래의 링크로 이동하여 수동 설치
    [https://github.com/z-mahmud22/Dlib_Windows_Python3.x] 
---

## 폴더 구조
### app 주요 폴더 구조
```plaintext
main/
├── java/
│ ├── constants/ # 상수 선언
│ ├── model/ # 객체 선언
│ ├── network/
│ │ ├── interface/ # HTTP 통신 인터페이스
│ │ └── retrofit/ # Retrofit2 클래스
│ ├── ui/
│ │ ├── activity/ # 화면 액티비티
│ │ ├── adapter/ # 어댑터
│ │ └── fragment/ # 프래그먼트
├── res/
│ ├── anim/ # 요소 움직임 파일
│ ├── drawable/ # 이미지 파일
│ ├── function/ # 기능 파일
│ ├── layout/ # 레이아웃 파일
│ ├── menu/ # 팝업 메뉴 파일
│ ├── values/ # 색상 및 문자열 선언
│ └── xml/ # 인터넷 접근 허용 설정
└──
```

### flask 서버 폴더 구조
```plaintext
venv/
├── ClassifyResult/ # 분류 완료 결과 폴더
├── Extracted/ # 분류 완료 후 남은 이미지 폴더
├── Lib/ # 라이브러리 설치 폴더
├── python/ # 분류 코드 폴더
│ ├── dataset/ # 눈 인식 학습 데이터셋
│ ├── Resource/ # 얼굴 인식 학습 데이터셋
│ ├── extractZip.py/ # 압축 파일 풀기 코드
│ ├── eyes.py/ # 눈 인식 코드
│ ├── face.py/ # 얼굴 인식 코드
│ ├── location.py/ # 위치 분류 코드
│ ├── meta_location.py/ # 사진의 메타데이터 추출 코드
│ ├── period.py/ # 날짜 기간 분류 코드
│ └── pic_date.py/ # 날짜 하루 분류 코드
├── Scripts/ #flask 가상 환경 실행을 위한 폴더
├── uploads/ #압축 파일을 보관해놓는 폴더
└── app.py/ #flask 서버 실행 코드
```

---

## 기술 스택
- 언어: Java
- 데이터베이스: Firebase
- 서버 : Flask
- Android Dependency: Retrofit, Glide, Recyclerview, Viewpager2, Material, Okhttp3, Picasso, Exifinterface
- Flask Lib: PIL, Deepface, Matplotlib, cv2, Numpy, Dlib, Dotenv

---

## 시연 영상
AutoClassify 시연 영상입니다. 

### 눈 분류
<img width="100%" alt="영상2" src="resource/eyes.gif">

### 날짜 분류
<img width="100%" alt="영상1" src="resource/date.gif">

### 위치 분류
<img width="100%" alt="영상3" src="resource/location.gif">

### 저장 기능

---

## 팀 구성
| 이름 |  | 역할 |  |
|-----------------|-----------------|-----------------|-----------------|
| 정호정   |  <img src="https://noticon-static.tammolo.com/dgggcrkxq/image/upload/v1638101071/noticon/gpr07ptl1x6evhew7li7.png" alt="정호정" width="100"> | <ul><li>위치/날짜 분류 코드 작성</li><li>앱 개발 및 서버 구축</li><li>데이터베이스 연동</li></ul>     |[GitHub](https://github.com/ristukaJJang) |
| 정승아   |  <img src="https://noticon-static.tammolo.com/dgggcrkxq/image/upload/v1711609978/noticon/goy8tfeuevcdimme8vcl.png" alt="정승아" width="100">| <ul><li>눈/얼굴 인식 분류 코드 작성</li><li>앱 UI 디자인</li></ul> |[GitHub](https://github.com/Jeongsunga) |

---

## 라이선스
    GPL License
    Copyright (c) 2024 [Sorting]

