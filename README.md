# JAVA 자판기 관리 프로그램

이 저장소는 JAVA 프로그래밍 과제 요구사항에 맞춘 **GUI 기반 자판기 관리 프로그램**입니다. 기존 HTML 포트폴리오에 과제 산출물을 추가했으며, 핵심 구현은 `src/vending/VendingMachineApp.java` 한 파일 안에 Swing GUI, 관리자 기능, 파일 기반 관리 DB, 직접 구현 자료구조, 멀티스레드, Socket 클라이언트/서버를 포함합니다.

## 실행 환경

- OS: Windows, Linux/Unix, macOS 중 Java 실행 가능한 환경
- Language: Java
- Compiler: `javac` 17 이상 권장 (현재 검증: OpenJDK 25)
- GUI: Java Swing
- Socket: TCP Socket (`ServerSocket`, `Socket`)

## 빠른 실행

```bash
# 1) 컴파일
javac -encoding UTF-8 -d out src/vending/VendingMachineApp.java

# 2) 서버 실행(선택, 별도 터미널)
java -cp out vending.VendingMachineApp --server 5050

# 3) 자판기 클라이언트 실행
java -cp out vending.VendingMachineApp

# 4) 자동 점검
java -cp out vending.VendingMachineApp --self-test
```

## 기본 관리자 계정

- 초기 비밀번호: `Admin#123`
- 관리자 메뉴에서 언제든 변경 가능
- 새 비밀번호 규칙: 8자리 이상, 숫자 1개 이상, 특수문자 1개 이상

## 구현 범위 요약

- 판매 음료 8개 및 기본 가격/재고 10개 생성자 초기화
- 10/50/100/500/1000원 입력, 지폐 5,000원 상한, 총 7,000원 상한
- 돈 입력 세션 객체를 동적으로 생성하고 판매/반환 후 해제
- 판매 가능 음료만 활성화, 품절 표시, 거스름돈 부족 처리
- 관리자 전용 화면과 판매 화면을 `CardLayout`으로 분리
- 일별/월별 매출 집계, 재고 보충, 음료명/가격 변경, 수금, 화폐 현황 표시
- 파일 기반 관리 데이터 저장: `data/client-db/*.csv`, `data/server-db/events.log`
- 직접 구현 자료구조: Linked List, Stack, Queue, Binary Tree
- Sort/Search: 삽입 정렬, 선택 정렬, 이진 검색, 선형 검색
- 멀티스레드: Swing EDT, Socket 전송 스레드, 저재고 모니터 스레드, 서버 클라이언트 처리 스레드
- Socket 서버: 복수 클라이언트 이벤트 수신, 매출/재고/저재고 알림 로그 저장

## 산출물

- 상위/상세 설계 및 최종 보고서: `docs/final-report.md`
- 소스 코드: `src/vending/VendingMachineApp.java`
- 포트폴리오 안내 페이지: `index.html`
