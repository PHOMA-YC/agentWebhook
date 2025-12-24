package com.example.ai.agentwebhook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class AssignmentScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String studentName; // 학생의 GitHub ID
    private String repoName; // 과제 레포지토리 이름
    private int prNumber; // PR번호
    private int score; // 점수(0~100) - 50점 + 50점(감정) = ?
    @Column(length = 1000)
    private String feedback; // 피드백
    private LocalDateTime gradedAt; // now()

    public AssignmentScore(String studentName, String repoName, int prNumber, int score, String feedback) {
        this.studentName = studentName;
        this.repoName = repoName;
        this.prNumber = prNumber;
        this.score = score;
        this.feedback = feedback;
        this.gradedAt=LocalDateTime.now();
    }
}
