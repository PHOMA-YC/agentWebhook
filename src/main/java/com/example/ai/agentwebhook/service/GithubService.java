package com.example.ai.agentwebhook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GithubService {

    private final RestClient restClient;

    public GithubService(@Value("${github.token}") String token) {
        // GitHub API 전용 RestClient 설정
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }
    //  1. PR의 변경된 코드(Diff) 가져오기
    //  https://docs.github.com/ko/enterprise-server@3.14/rest/pulls/pulls?apiVersion=2022-11-28#get-a-pull-request
    public String getPrDiff(String owner, String repo, int prNumber){
            return restClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                // 나는 API 버전 3의 규칙에 따라, diff(변경 내역) 형식으로 데이터를 받고 싶어!
               .header("Accept", "application/vnd.github.v3.diff")
               .retrieve()
               .body(String.class);
    }
    // 2. PR에 리뷰 댓글 달기
    // https://docs.github.com/ko/rest/issues/comments?apiVersion=2022-11-28#create-an-issue-comment
   public void commentOnPr(String owner, String repo, int prNumber, String comment){
        restClient.post()
                .uri("/repos/{owner}/{repo}/issues/{prNumber}/comments", owner, repo, prNumber)
                .header("Accept", "application/vnd.github+json")
                .body(Map.of("body", comment))
                .retrieve()
                .toBodilessEntity();
   }
}