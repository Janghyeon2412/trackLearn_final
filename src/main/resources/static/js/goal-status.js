document.addEventListener("DOMContentLoaded", function () {
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const loadBtn = document.getElementById("loadStatsBtn");
    const tableBody = document.querySelector("#statsTable tbody");

    const today = new Date();
    today.setHours(today.getHours() + 9);
    const sevenDaysLater = new Date(today);
    sevenDaysLater.setDate(today.getDate() + 7);

    startDateInput.value = today.toISOString().split("T")[0];
    endDateInput.value = sevenDaysLater.toISOString().split("T")[0];

    loadBtn.addEventListener("click", async () => {
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;

        if (!startDate || !endDate) {
            alert("시작일과 종료일을 모두 선택하세요.");
            return;
        }

        try {
            const res = await fetch(`/api/goals/statistics?startDate=${startDate}&endDate=${endDate}`);
            const data = await res.json();

            renderTable(data);
        } catch (err) {
            console.error("통계 조회 실패", err);
            alert("통계 데이터를 불러오는 중 오류가 발생했습니다.");

        }
    });

    function renderTable(stats) {
        tableBody.innerHTML = "";

        if (stats.length === 0) {
            const row = tableBody.insertRow();
            row.innerHTML = `<td colspan="7" style="text-align:center;">데이터가 없습니다.</td>`;
            return;
        }

        stats.forEach(stat => {

            console.log("📅 기간 확인:", stat.title, stat.startDate, stat.endDate);

            const row = tableBody.insertRow();

            const statusText = (() => {
                if (stat.totalCount === 0) return "데이터 없음";
                if (stat.checkedCount === 0) return "미진행";
                if (stat.checkedCount === stat.totalCount) return "완료";
                return "진행중";
            })();

            const periodText = stat.startDate && stat.endDate
                ? `${stat.startDate} ~ ${stat.endDate}`
                : "-";

            row.innerHTML = `
            <td>${stat.title}</td>
            <td>${stat.categoryName}</td>
            <td>${stat.repeatText}</td>
            <td>${periodText}</td>
            <td>${stat.totalCount}</td>
            <td>${stat.checkedCount}</td>
            <td>${statusText}</td>
        `;
        });
    }


    // 페이지 진입 시 자동 실행 (기본값 날짜로)
    loadBtn.click();
});
