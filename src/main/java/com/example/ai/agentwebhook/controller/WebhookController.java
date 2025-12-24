package com.example.ai.agentwebhook.controller;

import com.example.ai.agentwebhook.service.PullRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
// GitHub로부터 Webhook 요청을 받아 파싱합니다. PR 작성자(pull_request.user)와 레포 주인(repository.owner)을 정확히 구분해야 합니다.
@RestController
public class WebhookController {
    // https://sedentary-unvoluntarily-sarai.ngrok-free.dev/webhook
    // -> http://localhost:8081/webhook
    private final PullRequestService pullRequestService;

    // 정답 코드 상수 (나중에 DB 도입 시 삭제 예정)
    private static final String SOLUTION_CODE = """
            public int add(int a, int b) {
                return a + b;
            }
            """;

    public WebhookController(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    @PostMapping("/webhook")
    public void handleGithubEvent(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType,
            @RequestBody Map<String, Object> payload){
           //1. 이벤트 필터링 (헤더 검사) : PR 이벤트가 아니면 바로 종료 (Push, Issue 등 무시)
            if (!"pull_request".equals(eventType)) {
               return;
            }
           // 2. 액션 필터링 (PR 생성, 코드 수정일 때만 처리)
           String action = (String) payload.get("action");
           if (!"opened".equals(action) && !"synchronize".equals(action)) {
               return;
           }
          System.out.println("🚀 [Webhook] PR 이벤트 감지! 데이터 분석 시작...");
          try{
           // 3. JSON 데이터 파싱 (안전하게 추출)
           Map<String, Object> pr = (Map<String, Object>) payload.get("pull_request");
           Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
           // 데이터가 없으면 중단 (NPE 방지)
           if (pr == null || repo == null) return;
           // 필요한 정보 쏙쏙 뽑기
           int prNumber = (Integer) pr.get("number"); // (int) 대신 (Integer) 캐스팅이 더 안전함
           String studentName = (String) ((Map<String, Object>) pr.get("user")).get("login");
           String repoName = (String) repo.get("name");
           String repoOwner = (String) ((Map<String, Object>) repo.get("owner")).get("login");

           System.out.printf("🔔 [Info] 과제: %s / 학생: %s / PR 번호: #%d\n", repoName, studentName, prNumber);

            // 4. 서비스 호출 (핵심 로직 위임)
            pullRequestService.processPullRequest(
                    repoOwner,
                    repoName,
                    prNumber,
                    studentName,
                    SOLUTION_CODE // 정답 코드 전달
            );
          }catch (Exception e){
              System.err.println("❌ [Error] 웹훅 데이터 파싱 중 오류 발생: " + e.getMessage());
              e.printStackTrace(); // 디버깅용 로그
          }
    }
    // TEST
    /*    @PostMapping("/webhook")
    public void handleGithubEvent(@RequestBody Map<String, Object> payload) {
        System.out.println(payload);   // 혹인?
    }*/
}
