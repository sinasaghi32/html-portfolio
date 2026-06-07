# 자판기 관리 프로그램 (JAVA Swing + Socket)

이 폴더는 JAVA 프로그래밍 과제 요구사항을 한 번에 시연할 수 있도록 구성한 자판기 관리 프로그램입니다.

## 실행 환경

- OS: Windows / Linux / macOS 중 Java JDK가 설치된 환경
- 언어/컴파일러: Java 17 이상 권장 (`javac`, `java`)
- GUI: Java Swing
- 통신: TCP Socket (`ServerSocket`, `Socket`)
- 로컬 관리 DB: `vending-machine/data/*.csv`, `admin.txt`, `audit.log` 파일 기반 데이터베이스

## 빌드 및 실행

```bash
javac -encoding UTF-8 -d vending-machine/out $(find vending-machine/src -name '*.java')
java -cp vending-machine/out vending.VendingSyncServer 5555
# 웹 관리자: http://127.0.0.1:8080
java -cp vending-machine/out vending.VendingMachineApp
```

서버는 별도 터미널에서 실행하고, 클라이언트 GUI는 2개 이상 띄우면 Client1/Client2/Server 형태의 Socket 통신 시연이 가능합니다.

## 기본 관리자 계정

- 초기 비밀번호: `Admin!123`
- 관리자 메뉴에서 숫자와 특수문자를 포함한 8자리 이상 비밀번호로 변경할 수 있습니다.

## 구현 요약

- 8개 음료 기본 구성: 믹스커피 200원, 고급믹스커피 300원, 물 450원, 캔커피 500원, 이온음료 550원, 고급캔커피 700원, 탄산음료 750원, 특화음료 800원
- 각 음료 기본 재고 10개, 재고 0개 시 품절 및 선택 비활성화
- 10/50/100/500/1000원 투입, 지폐 5,000원 및 총 투입 7,000원 한도
- 구매 가능 금액이 되면 해당 음료 버튼 활성화
- 거스름돈 보유/차감/수금, 거스름돈 부족 예외 표시
- 판매 후 다시 화폐 투입 상태로 복귀
- 관리자 메뉴: 비밀번호 확인, 일별/월별 매출, 재고 보충, 화폐 현황, 수금, 음료 이름/가격 변경, 검색/정렬
- 파일 읽기/쓰기: 음료, 매출, 관리자 비밀번호, 감사 로그
- 직접 구현 자료구조: Linked List, Stack, Queue, Tree
- 직접 구현 알고리즘: Selection Sort, Linear Search, Binary Search Tree date lookup
- 멀티스레드: GUI 이벤트 스레드, Socket 동기화 스레드, 서버 클라이언트 핸들러 스레드, 재고 모니터 스레드
- Socket 서버: 판매/재고/수금 이벤트 수신, ACK/저재고 알림 응답, 웹 관리자 현황 페이지 제공

## 주요 파일

- `src/vending/VendingMachineApp.java`: Swing 판매/관리 GUI
- `src/vending/VendingMachineModel.java`: 판매, 환불, 재고, 매출, 관리자 기능 중심 로직
- `src/vending/VendingSyncServer.java`: 중앙 Socket 서버
- `src/vending/SocketSyncClient.java`: 클라이언트 이벤트 전송 스레드
- `src/vending/CustomLinkedList.java`: 음료 재고 Linked List
- `src/vending/CustomStack.java`: 관리자 감사 기록 Stack
- `src/vending/CustomQueue.java`: Socket 전송 Queue
- `src/vending/SalesTree.java`: 일별/월별 매출 검색 Tree
