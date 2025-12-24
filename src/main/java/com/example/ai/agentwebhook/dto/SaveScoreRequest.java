package com.example.ai.agentwebhook.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("학생의 과제 점수와 피드백을 DB에 저장하기 위한 요청 데이터 구조입니다.")
public record SaveScoreRequest(
        @JsonPropertyDescription("과제를 제출한 학생의 GitHub ID")
        String studentName,
        @JsonPropertyDescription("과제가 제출된 GitHub 리포지토리 이름")
        String repoName,
        @JsonPropertyDescription("제출된 Pull Request의 번호")
        int prNumber,
        @JsonPropertyDescription("채점 기준표에 따라 부여된 최종 점수(0~100사이의 정수")
        int score,
        @JsonPropertyDescription("점수 감점 사유와 학생을 위한 격려의 말이 포함된 한 줄 요약 피드백")
        String feedback
) {}
