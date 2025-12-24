package com.example.ai.agentwebhook.agent;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CodeReviewParallelWorkflow {

    private final ReviewAgent reviewAgent;
    private final GradingAgent gradingAgent;

    public CodeReviewParallelWorkflow(ReviewAgent reviewAgent, GradingAgent gradingAgent) {
        this.reviewAgent = reviewAgent;
        this.gradingAgent = gradingAgent;
    }
    public String execute(String diff,  String solutionCode, int prNumber, String studentName, String repoName){
        long startTime = System.currentTimeMillis(); // 성능 측정 시작
        // 1. 리뷰 에이전트 실행 (비동기 - 별도 스레드에서 돎)
        CompletableFuture<String> reviewFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("📝 [Async] 리뷰 에이전트가 분석을 시작했습니다...");
            // 아까 만든 로직대로 정답 코드(solutionCode)도 같이 넘겨줌
            return reviewAgent.generateFeedback(diff, solutionCode);
        });
        // 2. 채점 에이전트 실행 (비동기 - 또 다른 스레드에서 돎)
        CompletableFuture<String> gradingFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("⚖️ [Async] 채점 에이전트가 채점 중입니다...");
            // 정답 코드와 메타 정보를 넘겨줌 (DB 저장까지 내부에서 수행)
            return gradingAgent.gradeAndSave(diff, solutionCode, prNumber, studentName, repoName);
        });
        // 3. 두 작업이 모두 끝날 때까지 여기서 대기 (Join)
        // 둘 중 하나라도 늦게 끝나면 거기 맞춰서 기다립니다.
        CompletableFuture.allOf(reviewFuture, gradingFuture).join();
        // 4. 결과 가져오기
        String reviewResult = reviewFuture.join();   // 리뷰 텍스트
        String gradingLog = gradingFuture.join();    // 채점 로그 (DB 저장 완료 메시지)
        long endTime = System.currentTimeMillis();
        System.out.println("⏱️ [Performance] 전체 처리 시간: " + (endTime - startTime) + "ms");
        System.out.println("🔍 [System Log] " + gradingLog);

        // 5. 최종 리턴: GitHub 댓글에는 오직 '리뷰 내용'만 반환
        return String.format("""
                ## 🤖 AI 코드 리뷰!
                %s
                """, reviewResult);
    }
}