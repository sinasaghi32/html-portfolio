# 자판기 관리 프로그램 상위/상세 설계 및 최종 완료 보고서

## 1. 과제 개요

- 과목명: JAVA 프로그래밍
- 목표: 자판기 관리 프로그램 개발
- 구현 환경: Java Swing GUI, Java Socket, 파일 기반 로컬 데이터베이스
- 제출 산출물 대응: 상위 설계서, 상세 설계서, 최종 완료 보고서, 주석 포함 Source Code, 설명 동영상 준비용 실행 절차

## 2. 상위 설계서

### 2.1 시스템 구성

```text
Client GUI(VendingMachineApp)
 ├─ 판매 화면: 화폐 투입, 음료 선택, 환불, 품절 표시
 ├─ 관리자 화면: 매출/재고/화폐/수금/상품/비밀번호 관리
 ├─ Model(VendingMachineModel): 동기화된 업무 규칙 처리
 ├─ LocalDatabase: 음료/매출/비밀번호/감사 파일 DB
 └─ SocketSyncClient: Queue 이벤트를 Server로 전송

Server(VendingSyncServer)
 ├─ Client 접속 수락
 ├─ 판매/재고/수금/환불 이벤트 누적
 └─ 저재고 이벤트 알림 응답
```

### 2.2 주요 스레드

1. Swing Event Dispatch Thread: GUI 이벤트 처리
2. SocketSyncClient Thread: 판매/재고/수금 이벤트 전송
3. LowStockMonitor Thread: 10초 단위 재고 스냅샷 전송
4. Server accept Thread 및 client-handler Thread: 다중 클라이언트 수신

### 2.3 데이터 저장 구조

- `beverages.csv`: 음료 ID, 이름, 가격, 재고, 품절 날짜
- `sales.csv`: 날짜, 음료 ID, 음료명, 수량, 금액, 자판기 ID
- `admin.txt`: 관리자 비밀번호
- `audit.log`: 관리자/판매 감사 로그 및 재고 소진 주기 확인용 기록

## 3. 상세 설계서

### 3.1 클래스 설계

| 클래스 | 역할 |
| --- | --- |
| `Beverage` | 음료 슬롯, 가격, 재고, 품절 날짜 관리 |
| `CashBox` | 화폐 단위별 개수, 거스름돈 계산, 수금 처리 |
| `MoneyInput` | 거래별 동적 생성 화폐 입력 객체 |
| `VendingMachineModel` | 판매/환불/관리자 업무 규칙의 동기화 중심 객체 |
| `LocalDatabase` | 파일 기반 DB 초기화/읽기/쓰기 |
| `VendingMachineApp` | Swing GUI |
| `VendingSyncServer` | 지정 서버 Socket 수신/누적/알림 |
| `SocketSyncClient` | Queue 이벤트를 서버로 송신 |
| `CustomLinkedList` | 음료 재고 저장 Linked List |
| `CustomStack` | 최근 감사 메시지 Stack |
| `CustomQueue` | Socket 이벤트 Queue |
| `SalesTree` | 날짜 기반 매출 검색 Tree |
| `SearchSortUtil` | 직접 구현 Selection Sort / Linear Search |

### 3.2 판매 흐름

1. 사용자가 10/50/100/500/1000원 중 하나를 투입한다.
2. `MoneyInput` 객체가 동적으로 생성되고, 지폐 5,000원/총액 7,000원 한도를 검사한다.
3. 현재 금액으로 구매 가능한 음료 버튼만 활성화된다.
4. 음료 선택 시 재고, 금액, 거스름돈 가능 여부를 검사한다.
5. 판매 성공 시 재고 1개 차감, 판매 파일 저장, Socket 이벤트 전송, `MoneyInput` 해제 처리(`null`)가 수행된다.
6. GUI는 다시 화폐 입력 가능한 상태로 돌아간다.

### 3.3 관리자 흐름

1. 관리자 메뉴 선택 시 비밀번호 입력 창이 표시된다.
2. 비밀번호가 맞으면 Modal 관리자 화면이 열리고 판매 화면 버튼은 잠긴다.
3. 관리자는 일별/월별 매출 조회, 재고 보충, 화폐 현황 조회, 수금, 음료 이름/가격 변경, 비밀번호 변경을 수행한다.
4. 모든 변경 사항은 파일 DB와 감사 로그에 저장되고 Socket 서버로 전송된다.

### 3.4 예외 처리 전략

- 잘못된 화폐, 초과 투입, 재고 부족, 거스름돈 부족, 잘못된 관리자 비밀번호, 잘못된 가격/재고 입력은 Java 예외로 발생시킨다.
- GUI 계층은 공통 `safe` 실행기로 예외를 받아 사용자 오류 메시지를 표시한다.
- Socket 통신 실패는 프로그램을 종료하지 않고 경고 로그를 출력한다.

## 4. 요구사항 구현 완료 표

| 요구사항 | 완료 |
| --- | --- |
| Java 우선 사용 | O |
| GUI 환경 동작 | O |
| 8개 기본 음료 및 지정 가격 | O |
| 기본 재고 10개 및 품절 표시/선택 불가 | O |
| 재고 보충 후 판매 가능 | O |
| 10/50/100/500/1000원만 입력 | O |
| 지폐 5,000원, 총액 7,000원 초과 방지 | O |
| 화폐 입력 객체 동적 생성 및 거래 종료 후 해제 | O |
| 금액에 따른 판매 가능 음료 표시 | O |
| 기본 거스름돈 각 단위 10개 생성자 초기화 | O |
| 거스름돈 반환/판매에 따른 화폐 가감 | O |
| 사용자 요구 환불 | O |
| 거스름돈 없음 표시 | O |
| 판매 후 재투입 상태 복귀 | O |
| 관리자 비밀번호 확인 | O |
| 비밀번호 숫자/특수문자 포함 8자리 이상 및 변경 | O |
| 일별/월별 전체 매출 | O |
| 음료별 일별/월별 매출 | O |
| 사전 저장 매출 파일과 현재 매출 연계 | O |
| 관리자 재고 보충 | O |
| 자판기 화폐 현황 | O |
| 수금 및 최소 반환 화폐 보존 | O |
| 음료 가격/이름 변경 | O |
| 관리자 관련 파일 읽기/쓰기 | O |
| 재고 소진 날짜 기록 | O |
| 관리자 화면과 판매 화면 독립 동작 | O |
| 예외 처리 및 주석 | O |
| 모든 자료구조 직접 구현 | O |
| 재고 Linked List 구현 | O |
| Stack 1회 이상 사용 | O |
| Queue 1회 이상 사용 | O |
| Tree 1회 이상 사용 | O |
| Sort/Search 1회 이상 사용 | O |
| 멀티스레드 3개 이상 | O |
| 관리자 데이터 별도 DB 사용 | O |
| Socket으로 생성/저장 데이터 서버 전송 | O |
| 서버의 매출/재고 누적 및 저재고 알림 기반 | O |
| Client1, Client2, Server 3환경 시연 가능 | O |
| 4학년 클라우드 DB | X - 본 구현은 3학년 필수 범위 중심 |
| 4학년 Server1/Server2/Backup 실시간 동기화 | X - 설계 확장 항목으로 분리 |
| Raw Socket 신규 프로토콜 | X - Java TCP Socket 사용 |
| 서버 웹 관리 페이지 | O - `VendingSyncServer`가 이벤트 로그 웹 현황 페이지 제공 |
| Bluetooth/IrDA 또는 Embedded End_Device | X - 추가 기능 범위 |

## 5. 실행 및 동영상 촬영 순서

1. 서버 실행: `java -cp vending-machine/out vending.VendingSyncServer 5555`
2. 클라이언트 GUI 2개 실행: `java -cp vending-machine/out vending.VendingMachineApp`
3. 각 클라이언트에서 화폐 투입, 음료 구매, 환불, 품절 전환을 시연한다.
4. 관리자 메뉴에 `Admin!123`으로 접속한다.
5. 일별/월별 매출, 재고 보충, 수금, 이름/가격 변경, 비밀번호 변경을 시연한다.
6. 서버 터미널에서 Socket 이벤트 ACK 및 저재고 알림을 확인한다.

## 6. 향후 확장 특장점

- 파일 기반 DB를 SQLite/MySQL/Cloud DB로 교체하면 4학년 클라우드 요구사항으로 확장 가능하다.
- `VendingSyncServer`를 Server1/Server2/Backup_Server 세 프로세스로 분리하고 heartbeat 및 replication packet을 추가하면 실시간 이중화 구조로 확장할 수 있다.
- 현재 서버는 HTTP 웹 관리자 페이지를 제공하며, 향후 인증/그래프/Cloud DB 연동을 추가할 수 있다.
