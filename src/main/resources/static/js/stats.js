document.addEventListener("DOMContentLoaded", async () => {
    console.log("📊 stats.js loaded");

    const today = new Date();
    let currentYear = today.getFullYear();
    let currentMonth = today.getMonth() + 1;
    let monthlyChartInstance = null;


    try {
        const res = await fetch("/api/dashboard/stats");
        if (!res.ok) throw new Error("API 요청 실패");

        const data = await res.json();
        console.log("📦 통계 데이터:", data);

        document.getElementById("weekRange").innerText = `${data.startDate} ~ ${data.endDate}`;


        // ✅ 숫자 출력
        const total = data.dailyStudyTimes.reduce((a, b) => a + b, 0);
        document.getElementById("totalTime").innerText = `${Math.floor(total / 60)}h ${total % 60}m`;
        document.getElementById("entryCount").innerText = `${data.entryCount}개`;
        document.getElementById("avgTime").innerText = `${Math.floor(total / 7 / 60)}h ${Math.floor((total / 7) % 60)}m`;
        document.getElementById("bestDay").innerText = data.bestDay || "-";
        document.getElementById("achievementRate").innerText = `${data.achievementRate}%`;

        // ✅ 차트 그리기 (최대값 자동계산 + 라벨 변환)
        const maxValue = Math.max(...data.dailyStudyTimes);
        const suggestedMax = Math.ceil(maxValue / 60) * 60 + 60;

        const ctx = document.getElementById("weeklyChart").getContext("2d");
        new Chart(ctx, {
            type: "bar",
            data: {
                labels: ["월", "화", "수", "목", "금", "토", "일"],
                datasets: [{
                    label: "학습 시간 (분)",
                    data: data.dailyStudyTimes,
                    backgroundColor: "#ff6b35"
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 600, // ← 여기! 고정 최대값 600분 (10시간)
                        ticks: {
                            stepSize: 60,
                            callback: function (value) {
                                const h = Math.floor(value / 60);
                                const m = value % 60;
                                return h > 0 ? `${h}h ${m}m` : `${m}m`;
                            }
                        }
                    }
                }
            }

        });

        // ✅ 탭 기능 연결
        document.querySelectorAll(".tab-button").forEach(button => {
            button.addEventListener("click", () => {
                document.querySelectorAll(".tab-button").forEach(btn => btn.classList.remove("active"));
                document.querySelectorAll(".tab-section").forEach(sec => sec.classList.remove("active"));

                button.classList.add("active");
                const tabId = button.dataset.tab;
                document.getElementById(tabId + "Tab").classList.add("active");

                if (tabId === "monthly") {
                    loadMonthlyStats(currentYear, currentMonth);
                }
            });
        });

    } catch (e) {
        console.error("❌ 통계 로드 실패:", e);
    }

    // ✅ GPT 유형 통계 fetch
    fetch("/api/dashboard/gpt-summary")
        .then(res => res.json())
        .then(gptData => {
            console.log("📡 gpt-summary 응답:", gptData);
            const ctxGpt = document.getElementById("gptTypeChart").getContext("2d");

            const typeLabels = {
                cheer: "응원",
                advice: "조언",
                adjust: "조절"
            };

            const counts = gptData.typeCounts || {};
            const labels = Object.keys(counts).map(k => typeLabels[k] || k);
            const data = Object.values(counts);

            new Chart(ctxGpt, {
                type: "doughnut",
                data: {
                    labels: labels,
                    datasets: [{
                        data: data,
                        backgroundColor: ["#ff6b35", "#4e79a7", "#f28e2b"]
                    }]
                },
                options: {
                    responsive: true,
                    cutout: "60%",
                    radius: "80%",
                    plugins: {
                        legend: {
                            position: "bottom"
                        }
                    }
                }
            });

            const list = document.getElementById("gptMentList");

            // ✅ GPT 요약 멘트 표시
            gptData.recentFeedbacks.forEach((line, i) => {
                const li = document.createElement("li");

                // ✅ 1. "→" 기준으로 요약만 추출
                const summary = line.includes("→") ? line.split("→")[1].trim() : line;

                // ✅ 2. 길이 제한
                const clipped = summary.length > 70 ? summary.substring(0, 70) + "..." : summary;

                li.textContent = `${i + 1}. ${clipped}`;
                li.classList.add("gpt-summary-item"); // 스타일링용 클래스
                list.appendChild(li);
            });


        })
        .catch(e => {
            console.error("GPT 요약 통계 로드 실패", e);
        });

    // ✅ 탭 기능 연결
    document.querySelectorAll(".tab-button").forEach(button => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".tab-button").forEach(btn => btn.classList.remove("active"));
            document.querySelectorAll(".tab-section").forEach(sec => sec.classList.remove("active"));

            button.classList.add("active");
            const tabId = button.dataset.tab;
            document.getElementById(tabId + "Tab").classList.add("active");

            // ✅ 월간 탭이면 fetch
            if (tabId === "monthly") {
                fetch("/api/dashboard/monthly-stats")
                    .then(res => res.json())
                    .then(drawMonthlyChart)
                    .catch(e => console.error("❌ 월간 통계 로드 실패", e));
            }
        });
    });


    // ✅ 이전/다음 달 버튼 이벤트
    document.getElementById("prevMonthBtn").addEventListener("click", () => {
        if (currentMonth === 1) {
            currentMonth = 12;
            currentYear--;
        } else {
            currentMonth--;
        }
        loadMonthlyStats(currentYear, currentMonth);
    });

    document.getElementById("nextMonthBtn").addEventListener("click", () => {
        if (currentMonth === 12) {
            currentMonth = 1;
            currentYear++;
        } else {
            currentMonth++;
        }
        loadMonthlyStats(currentYear, currentMonth);
    });


    // ✅ 월간 통계 fetch
    function loadMonthlyStats(year, month) {
        fetch(`/api/dashboard/monthly-stats?year=${year}&month=${month}`)
            .then(res => res.json())
            .then(monthlyData => {
                document.getElementById("monthlyDateLabel").innerText = monthlyData.month || "-";

                const total = monthlyData.dailyStudyTimes.reduce((a, b) => a + b, 0);
                const days = monthlyData.dailyStudyTimes.length;
                document.getElementById("monthlyTotalTime").innerText = `${Math.floor(total / 60)}h ${total % 60}m`;
                document.getElementById("monthlyEntryCount").innerText = `${monthlyData.entryCount}개`;
                document.getElementById("monthlyAvgTime").innerText = `${Math.floor(total / days / 60)}h ${Math.floor((total / days) % 60)}m`;
                document.getElementById("monthlyBestDay").innerText = monthlyData.bestDay || "-";
                document.getElementById("monthlyAchievementRate").innerText = `${monthlyData.achievementRate}%`;

                drawMonthlyChart(monthlyData);
            })
            .catch(e => {
                console.error("❌ 월간 통계 로드 실패:", e);
            });
    }



    function drawMonthlyChart(data) {
        const ctx = document.getElementById("monthlyChart").getContext("2d");

        // ✅ 기존 차트 제거
        if (monthlyChartInstance) {
            monthlyChartInstance.destroy();
        }

        const labels = Array.from({ length: data.dailyStudyTimes.length }, (_, i) => `${i + 1}일`);
        const maxValue = Math.max(...data.dailyStudyTimes);
        const suggestedMax = Math.ceil(maxValue / 60) * 60 + 60;

        // ✅ 새 차트 인스턴스 저장
        monthlyChartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: "학습 시간 (분)",
                    data: data.dailyStudyTimes,
                    backgroundColor: "#ff6b35"
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: suggestedMax > 600 ? suggestedMax : 600,
                        ticks: {
                            stepSize: 60,
                            callback: value => {
                                const h = Math.floor(value / 60);
                                const m = value % 60;
                                return h > 0 ? `${h}h ${m}m` : `${m}m`;
                            }
                        }
                    }
                }
            }
        });
    }
    loadMonthlyStats(currentYear, currentMonth);

});
