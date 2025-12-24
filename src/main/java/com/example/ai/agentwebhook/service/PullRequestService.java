package com.example.ai.agentwebhook.service;

import com.example.ai.agentwebhook.agent.CodeReviewParallelWorkflow;
import org.springframework.stereotype.Service;
// GitHub에서 정보를 가져와 워크플로우를 실행하고, 결과를 다시 GitHub에 리뷰등록합니다.
@Service
public class PullRequestService {
    private final GithubService githubService;
    private final CodeReviewParallelWorkflow workflow;

    public PullRequestService(GithubService githubService, CodeReviewParallelWorkflow workflow) {
        this.githubService = githubService;
        this.workflow = workflow;
    }
    /**
     * PR 처리의 전체 흐름을 관장하는 메인 비즈니스 로직입니다.
     * * @param repoOwner   레포지토리 주인 (교수님 ID)
     * @param repoName    레포지토리 이름 (과제명)
     * @param prNumber    PR 번호
     * @param studentName 학생 ID
     * @param solutionCode 정답 코드 (Controller나 DB에서 받아옴)
     */
    public void processPullRequest(String repoOwner, String repoName, int prNumber, String studentName, String solutionCode){
        // 1. GitHub API 호출 (변경된 코드 가져오기)
        String diff=githubService.getPrDiff(repoOwner, repoName, prNumber);
        // 2. 병렬 워크플로우 실행 (AI 에이전트들에게 일 시키기)
        String finalComment=workflow.execute(diff, solutionCode, prNumber, studentName, repoName);
        // 3. GitHub에 댓글 달기 (최종 리뷰 등록)
        githubService.commentOnPr(repoOwner, repoName, prNumber, finalComment);
        System.out.println("✅ [Service] 모든 처리가 완료되었습니다.");
    }
}
