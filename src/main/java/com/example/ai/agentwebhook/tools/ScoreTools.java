package com.example.ai.agentwebhook.tools;

import com.example.ai.agentwebhook.dto.SaveScoreRequest;
import com.example.ai.agentwebhook.entity.AssignmentScore;
import com.example.ai.agentwebhook.repository.ScoreRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ScoreTools {
    private final ScoreRepository scoreRepository;
    public ScoreTools(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }
    // @Tool 애노테이션을 붙이면 AI가 이 메서드를 사용할 수 있습니다.
    @Tool(description = "채점 결과를 DB에 저장하는 도구입니다. 점수(0~100)와 피드백을 반드시 저장해야 합니다.")
    public String saveScore(SaveScoreRequest request){
        System.out.println("[Tool] AI가 DB저장을 요청했습니다." + request.score() + "점");
        AssignmentScore entity=new AssignmentScore(
                request.studentName(),
                request.repoName(),
                request.prNumber(),
                request.score(),
                request.feedback()
        );
        scoreRepository.save(entity);
        return "DB 저장 완료! (학생:" + request.studentName() + ", 점수:" + request.score() + ")";
    }
}
