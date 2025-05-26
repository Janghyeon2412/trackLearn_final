document.addEventListener("DOMContentLoaded", () => {
    // ✅ 오늘의 목표 불러오기
    fetch("/api/diary/today-goals")
        .then(res => res.json())
        .then(data => renderGoalCheckboxes(data))
        .catch(err => {
            console.error("오늘의 목표 로딩 실패", err);
            document.getElementById("goal-checkbox-list").innerText = "목표를 불러올 수 없습니다.";
        });

    // ✅ 체크박스 렌더링
    function renderGoalCheckboxes(goals) {
        const container = document.getElementById("goal-checkbox-list");
        container.innerHTML = "";

        if (!goals.length) {
            container.innerText = "오늘의 목표가 없습니다.";
            return;
        }

        goals.forEach(goal => {
            const wrapper = document.createElement("div");
            wrapper.className = "checkbox-wrap";

            wrapper.innerHTML = `
  <label>
    <input type="checkbox" name="goalCheck" value="${goal.goalLogId}">
    <span class="goal-title">${goal.title} (${goal.categoryName}, ${goal.repeatText})</span>
  </label>
  <input type="text" name="retrospectives" class="retrospective-input"
         placeholder="이 목표에 대한 회고를 입력하세요">
`;


            container.appendChild(wrapper);
        });
    }

    // ✅ 저장 버튼 클릭
    document.getElementById("saveDiaryBtn").addEventListener("click", async () => {
        const title = document.getElementById("title").value.trim();
        const content = document.getElementById("content").value.trim();
        const summary = document.getElementById("summary")?.value.trim() ?? ""; // 선택사항이라면 null-safe 처리
        const satisfaction = parseFloat(document.getElementById("satisfaction").value);

        const hour = parseInt(document.getElementById("studyHour").value) || 0;
        const minute = parseInt(document.getElementById("studyMinute").value) || 0;
        const studyTime = hour * 60 + minute;



        const checkedGoalIds = [...document.querySelectorAll("input[name='goalCheck']:checked")]
            .map(cb => parseInt(cb.value));

        const retrospectiveInputs = [...document.querySelectorAll("input[name='retrospectives']")];
        const retrospectives = retrospectiveInputs.map(input => input.value.trim());

        if (!title || !content) {
            alert("제목과 내용을 모두 입력해주세요.");
            return;
        }

        const body = {
            title,
            content,
            summary,
            satisfaction,
            studyTime,
            completedGoalIds: checkedGoalIds,
            retrospectives
        };

        try {
            const res = await fetch('/api/diary/diaries', {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (res.ok) {
                alert("일지가 저장되었습니다.");
                window.location.href = "/main";
            } else {
                const err = await res.json();
                alert("저장 실패: " + JSON.stringify(err));
            }
        } catch (err) {
            alert("서버 오류 발생: " + err.message);
        }
    });


    const contentInput = document.getElementById("content");
    const charCountDisplay = document.getElementById("char-count");

    contentInput.addEventListener("input", () => {
        const length = contentInput.value.length;
        charCountDisplay.textContent = `(${length}자)`;
    });


    const stars = document.querySelectorAll('.star-rating span');
    const satisfactionInput = document.getElementById('satisfaction');

    stars.forEach((star, index) => {
        star.addEventListener('mousemove', (e) => {
            const percent = e.offsetX / star.offsetWidth;
            const value = index + (percent > 0.5 ? 1 : 0.5);
            highlightStars(value);
        });

        star.addEventListener('click', (e) => {
            const percent = e.offsetX / star.offsetWidth;
            const value = index + (percent > 0.5 ? 1 : 0.5);
            satisfactionInput.value = value;
        });

        star.addEventListener('mouseleave', () => {
            highlightStars(parseFloat(satisfactionInput.value));
        });
    });

    function highlightStars(value) {
        stars.forEach((star, index) => {
            star.classList.remove('full', 'half');
            if (value >= index + 1) {
                star.classList.add('full');
            } else if (value >= index + 0.5) {
                star.classList.add('half');
            }
        });
    }


});
