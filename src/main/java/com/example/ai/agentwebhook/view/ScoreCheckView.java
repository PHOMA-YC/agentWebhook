package com.example.ai.agentwebhook.view;

import com.example.ai.agentwebhook.entity.AssignmentScore;
import com.example.ai.agentwebhook.repository.ScoreRepository;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Route;

import java.util.List;

// Vaadin 프레임워크를 사용해 구현한 학생용 대시보드입니다.
@Route("") // http://localhost:8081 접속 시 이 화면 노출
public class ScoreCheckView extends VerticalLayout {

    private final ScoreRepository repository;
    private final Grid<AssignmentScore> grid = new Grid<>(AssignmentScore.class, false);

    public ScoreCheckView(ScoreRepository repository) {
        this.repository = repository;

        // 1. 전체 레이아웃 디자인 (가운데 정렬)
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // 2. 제목
        H1 title = new H1("📊 내 과제 점수 히스토리");
        title.getStyle().set("color", "#2c3e50"); // 진한 남색 스타일

        // 3. 검색 입력창 (GitHub ID만 입력)
        TextField githubIdField = new TextField();
        githubIdField.setPlaceholder("GitHub ID를 입력하세요");
        githubIdField.setPrefixComponent(VaadinIcon.USER.create()); // 아이콘 추가
        githubIdField.setClearButtonVisible(true);
        githubIdField.setWidth("300px");
        githubIdField.focus(); // 페이지 열리면 바로 입력 가능하게 포커스

        // 4. 조회 버튼
        Button searchBtn = new Button("조회", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // 파란색 버튼
        searchBtn.addClickShortcut(Key.ENTER); // 엔터키 누르면 실행

        // 조회 동작 연결
        searchBtn.addClickListener(e -> searchHistory(githubIdField.getValue()));

        HorizontalLayout searchLayout = new HorizontalLayout(githubIdField, searchBtn);
        searchLayout.setAlignItems(Alignment.BASELINE);

        // 5. 결과 그리드(표) 설정
        configureGrid();

        // 6. 화면 조립
        add(title, searchLayout, grid);
    }

    private void configureGrid() {
        grid.setWidth("90%");
        grid.setHeight("600px");
        grid.setVisible(false);

        // [중요] 그리드 자체에 "줄바꿈 허용" 테마 적용
        // 이 설정이 있어야 내용이 많을 때 행 높이가 자동으로 늘어납니다.
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        // [컬럼 1] 과제명
        grid.addColumn(AssignmentScore::getRepoName)
                .setHeader("과제명")
                .setWidth("150px")
                .setFlexGrow(0);

        // [컬럼 2] PR 번호
        grid.addColumn(AssignmentScore::getPrNumber)
                .setHeader("PR #")
                .setWidth("80px")
                .setFlexGrow(0);

        // [컬럼 3] 점수
        grid.addColumn(new ComponentRenderer<>(score -> {
            Span badge = new Span(score.getScore() + "점");
            String theme = "badge pill";
            if (score.getScore() >= 90) theme += " success";
            else if (score.getScore() >= 70) theme += " contrast";
            else theme += " error";
            badge.getElement().getThemeList().add(theme);
            return badge;
        })).setHeader("점수").setWidth("100px").setSortable(true).setFlexGrow(0);

        // [컬럼 4] AI 피드백 (수정됨)
        grid.addColumn(new ComponentRenderer<>(score -> {
                    Span span = new Span(score.getFeedback());

                    // [스타일 설정]
                    // pre-wrap: 줄바꿈(\n) 인식 + 자동 줄바꿈
                    span.getStyle().set("white-space", "pre-wrap");
                    span.getStyle().set("word-break", "break-word"); // 긴 단어 강제 줄바꿈
                    span.getStyle().set("line-height", "1.5"); // 줄 간격
                    span.setWidthFull();

                    return span;
                }))
                .setHeader("AI 피드백")
                // [수정] setMinWidth는 없으므로 setWidth를 사용합니다.
                // setFlexGrow(1)과 함께 쓰면 "기본 350px로 시작해서 남는 공간을 다 차지해라"가 됩니다.
                .setWidth("350px")
                .setFlexGrow(1);

        // [컬럼 5] 날짜
        grid.addColumn(new LocalDateTimeRenderer<>(
                AssignmentScore::getGradedAt,
                "yyyy-MM-dd HH:mm"
        )).setHeader("채점 일시").setWidth("160px").setFlexGrow(0);
    }

    private void searchHistory(String studentName) {
        if (studentName == null || studentName.isBlank()) {
            Notification.show("GitHub ID를 입력해주세요.", 2000, Notification.Position.MIDDLE);
            return;
        }

        // 리스트 조회 호출 (최신순)
        List<AssignmentScore> history = repository.findByStudentNameOrderByGradedAtDesc(studentName);

        if (history.isEmpty()) {
            grid.setVisible(false);
            Notification.show("'" + studentName + "' 님의 채점 기록이 없습니다.", 3000, Notification.Position.MIDDLE);
        } else {
            grid.setVisible(true);
            grid.setItems(history);
            Notification.show(history.size() + "건의 과제 내역을 불러왔습니다.", 2000, Notification.Position.BOTTOM_END);
        }
    }
}