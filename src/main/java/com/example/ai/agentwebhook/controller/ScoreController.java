package com.example.ai.agentwebhook.controller;

import com.example.ai.agentwebhook.entity.AssignmentScore;
import com.example.ai.agentwebhook.repository.ScoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scores") // 공통 주소 (/api/scores) 설정
public class ScoreController { // Vaadin

    private final ScoreRepository scoreRepository;

    public ScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }
    // 예시 URL: GET http://localhost:8081/api/scores/parkmaeil
    @GetMapping("/{studentName}")
    // [  {               }, {             }..... ]
    public ResponseEntity<List<AssignmentScore>> getStudentScores(@PathVariable String studentName) {
        System.out.println("🔍 성적 조회 요청: " + studentName);
        // 1. 레포지토리에 만들어둔 메서드 호출 (최신순 정렬됨)
        List<AssignmentScore> scores = scoreRepository.findByStudentNameOrderByGradedAtDesc(studentName);
        // 2. 결과가 없어도 빈 리스트([])가 반환되므로 안전함
        if (scores.isEmpty()) {
            System.out.println("⚠️ 기록 없음: " + studentName);
            return ResponseEntity.noContent().build(); // 204 No Content (또는 그냥 빈 리스트 200 OK 줘도 됨)
        }
        // 3. 200 OK와 함께 데이터 반환
        return ResponseEntity.ok(scores);
    }
}
