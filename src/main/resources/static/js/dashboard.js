document.addEventListener("DOMContentLoaded", () => {
    fetchTodayGoals();
    fetchWeeklyStats();
    fetchLatestFeedbacks();
    fetchNextSchedule();
    renderCalendar();
});

function renderCalendar() {
    const calendarEl = document.getElementById('calendar');

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'ko',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: ''
        },
        events: async function (info, successCallback, failureCallback) {
            try {
                const res = await fetch(`/api/dashboard/calendar?start=${info.startStr}&end=${info.endStr}`);
                const data = await res.json();

                const events = data.map(log => ({
                    id: log.goalId,
                    title: log.title,
                    start: log.date,
                    url: log.checked
                        ? `/diary/view?goalLogId=${log.goalLogId}`
                        : `/diary/write?goalLogId=${log.goalLogId}`,
                    className: log.checked ? 'completed' : 'unchecked',
                    extendedProps: {
                        startDate: log.startDate,
                        endDate: log.endDate
                    }
                }));

                successCallback(events);
            } catch (err) {
                console.error('캘린더 이벤트 로딩 실패', err);
                failureCallback(err);
            }
        },

        eventDidMount: function(info) {
            const start = info.event.extendedProps.startDate;
            const end = info.event.extendedProps.endDate;
            const tooltip = `${info.event.title} (${start} ~ ${end})`;

            info.el.setAttribute("title", tooltip);
        },

        eventClick: function (info) {
            if (info.event.url) {
                window.location.href = info.event.url;
                info.jsEvent.preventDefault();
            }
        }
    });

    calendar.render();
}



function fetchTodayGoals() {
    fetch("/api/dashboard/today-goals")
        .then((res) => {
            if (!res.ok) throw new Error("인증 오류 또는 서버 오류");
            return res.json();
        })
        .then((data) => renderTodayGoals(data))
        .catch((err) => {
            document.getElementById("today-goals").innerText = "오늘의 목표를 불러올 수 없습니다.";
            console.error(err);
        });
}

function renderTodayGoals(goals) {
    const container = document.getElementById("today-goals");
    container.innerHTML = "";

    if (goals.length === 0) {
        container.innerText = "오늘의 목표가 없습니다.";
        return;
    }

    const repeatTypeMap = {
        DAILY: "매일",
        WEEKLY: "주 3회",
        CUSTOM: "사용자 지정",
    };


    goals.forEach((goal, index) => {
        const card = document.createElement("div");
        card.className = 'goal-card';

        const repeatText = goal.repeatText || repeatTypeMap[goal.repeatType] || goal.repeatType;
        const categoryText = goal.categoryName || "미지정";


        card.innerHTML = `
  <div class="goal-header">
    <span class="goal-title">${goal.title}</span>
    <span class="goal-repeat">반복: ${repeatText}</span>
  </div>
  <div class="goal-category">카테고리: ${categoryText}</div>
  <div class="progress-bar">
    <div class="progress" style="width: ${goal.progress}%"></div>
  </div>
  <div class="goal-percent">${goal.progress}% 완료</div>
`;


        container.appendChild(card);
    });
}






function fetchWeeklyStats() {
    fetch("/api/dashboard/stats")
        .then((res) => {
            if (!res.ok) throw new Error("주간 통계를 불러오지 못했습니다.");
            return res.json();
        })
        .then((data) => renderWeeklyStats(data))
        .catch((err) => {
            document.getElementById("stat-summary").innerText = "통계 정보를 불러올 수 없습니다.";
            console.error(err);
        });
}

function renderWeeklyStats(data) {
    const days = ["월", "화", "수", "목", "금", "토", "일"];

    // 차트
    const ctx = document.getElementById("weekly-chart").getContext("2d");
    new Chart(ctx, {
        type: "bar",
        data: {
            labels: days,
            datasets: [{
                label: "공부 시간 (분)",
                data: data.dailyStudyTimes,
                backgroundColor: "rgba(59, 130, 246, 0.7)"
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // 요약 통계
    const summary = `
    <p>총 일지 수: ${data.entryCount}</p>
    <p>평균 만족도: ${data.averageSatisfaction.toFixed(1)}</p>
    <p>가장 열심히 한 날: ${data.bestDay ?? "-"}</p>
    <p>목표 달성률: ${data.achievementRate}%</p>
  `;
    document.getElementById("stat-summary").innerHTML = summary;
}







function fetchLatestFeedbacks() {
    fetch("/api/dashboard/latest-feedbacks")
        .then((res) => {
            if (!res.ok) throw new Error("GPT 피드백 로딩 실패");
            return res.json();
        })
        .then((data) => renderFeedbackList(data))
        .catch((err) => {
            document.getElementById("feedback-list").innerHTML = "<li>피드백을 불러올 수 없습니다.</li>";
            console.error(err);
        });
}

function renderFeedbackList(feedbacks) {
    const list = document.getElementById("feedback-list");
    list.innerHTML = "";

    if (!feedbacks.length) {
        list.innerHTML = "<li>아직 받은 피드백이 없습니다.</li>";
        return;
    }

    feedbacks.forEach((f) => {
        const li = document.createElement("li");
        li.className = "feedback-card";
        li.innerHTML = `
      <div><strong>${f.diaryTitle}</strong> <small>${f.date}</small></div>
      <p>${f.feedbackContent}</p>
    `;
        list.appendChild(li);
    });
}







function fetchNextSchedule() {
    fetch("/api/dashboard/next-schedule")
        .then((res) => {
            if (!res.ok) throw new Error("일정 불러오기 실패");
            return res.json();
        })
        .then((data) => renderScheduleCards(data))
        .catch((err) => {
            document.getElementById("schedule-cards").innerText = "일정을 불러올 수 없습니다.";
            console.error(err);
        });
}

function renderScheduleCards(schedules) {
    const container = document.getElementById("schedule-cards");
    container.innerHTML = "";

    if (!schedules.length) {
        container.innerHTML = "<p>예정된 일정이 없습니다.</p>";
        return;
    }

    schedules.forEach((s) => {
        const card = document.createElement("div");
        card.className = "schedule-card";

        const goalsHtml = s.goals.map((g) => `
  <li>
    <strong>${g.title || "제목 없음"}</strong><br/>
    <span class="goal-category">카테고리: ${g.category || "미지정"}</span>
  </li>
`).join("");

        card.innerHTML = `
  <h4>${s.dday} (${s.date})</h4>

  <ul>${goalsHtml}</ul>
`;


        container.appendChild(card);
    });




}