package com.example.ai.agentwebhook.repository;

import com.example.ai.agentwebhook.entity.AssignmentScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<AssignmentScore, Long> {
    // 특정 학생의 점수 기록을 '채점 일시' 내림차순(최신순)으로 조회
    List<AssignmentScore> findByStudentNameOrderByGradedAtDesc(String studentName);
}
