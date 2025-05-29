document.addEventListener("DOMContentLoaded", () => {
    const isEditMode = document.getElementById("editMode")?.value === "true";
    const goalLogId = document.getElementById("goalLogId")?.value;
    const diaryId = document.getElementById("diaryId")?.value;

    const titleInput = document.getElementById("title");
    const contentInput = document.getElementById("content");
    const summaryInput = document.getElementById("summary"); // optional
    const satisfactionInput = document.getElementById("satisfaction");
    const hourInput = document.getElementById("studyHour");
    const minuteInput = document.getElementById("studyMinute");
    const saveBtn = document.getElementById("saveDiaryBtn");
    const charCountDisplay = document.getElementById("char-count");

    const stars = document.querySelectorAll('.star-rating span');

    // ✅ 작성 모드에서만 실행
    if (!isEditMode) {
        const todayDate = document.getElementById('today-date');
        if (todayDate) {
            const now = new Date();
            const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
                .toISOString()
                .split('T')[0];
            todayDate.innerText = localDate;
        }

        // 오늘의 목표 체크박스 렌더링
        fetch("/api/diary/today-goals")
            .then(res => res.json())
            .then(data => renderGoalCheckboxes(data))
            .catch(err => {
                console.error("오늘의 목표 로딩 실패", err);
                const container = document.getElementById("goal-checkbox-list");
                if (container) container.innerText = "목표를 불러올 수 없습니다.";
            });

        function renderGoalCheckboxes(goals) {
            const container = document.getElementById("goal-checkbox-list");
            if (!container) return;
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
                    <input type="text" name="retrospectives" class="retrospective-input" placeholder="이 목표에 대한 회고를 입력하세요">
                `;
                container.appendChild(wrapper);
            });
        }
    }

    // ✅ 수정 모드 초기값 채우기
    if (isEditMode && diaryId) {
        fetch(`/api/diary/${diaryId}`)
            .then(res => res.json())
            .then(data => {
                titleInput.value = data.title || "";
                contentInput.value = data.content || "";
                satisfactionInput.value = data.satisfaction ?? 0;
                hourInput.value = data.studyTime ? Math.floor(data.studyTime / 60) : 0;
                minuteInput.value = data.studyTime ? data.studyTime % 60 : 0;

                highlightStars(data.satisfaction ?? 0);

                // ✅ 회고 여러 개 반영
                const retrospectiveList = data.retrospectives || [];
                retrospectiveList.forEach((text, index) => {
                    const textarea = document.getElementById(`retrospective${index}`);
                    if (textarea) {
                        textarea.value = text;
                    }
                });

                const goalTitles = data.goalTitles || [];
                goalTitles.forEach((title, index) => {
                    const heading = document.querySelector(`h3:nth-of-type(${index + 1})`);
                    if (heading) {
                        heading.innerText = `오늘의 목표 ${index + 1}: ${title}`;
                    }
                });
            })
            .catch(err => {
                console.error("수정 데이터 로딩 실패", err);
            });
    }



    // ✅ 저장 이벤트
    if (saveBtn) {
        saveBtn.addEventListener("click", async () => {
            const title = titleInput?.value.trim();
            const content = contentInput?.value.trim();
            const summary = summaryInput?.value.trim() ?? "";
            const satisfaction = parseFloat(satisfactionInput?.value);
            const hour = parseInt(hourInput?.value) || 0;
            const minute = parseInt(minuteInput?.value) || 0;
            const studyTime = hour * 60 + minute;

            if (!title || !content) {
                alert("제목과 내용을 모두 입력해주세요.");
                return;
            }

            // ✅ 프론트엔드 벨리데이션 추가
            if (title.length > 100) {
                alert("제목은 100자 이내로 입력해주세요.");
                return;
            }

            if (content.length < 10) {
                alert("내용은 최소 10자 이상 입력해주세요.");
                return;
            }

            let body = {
                title,
                content,
                summary,
                satisfaction,
                studyTime
            };

            if (isEditMode) {
                const retrospective = document.getElementById("retrospective")?.value.trim() || "";
                body.retrospective = retrospective;
                body.diaryId = parseInt(diaryId);
                body.goalLogId = parseInt(goalLogId);
            } else {
                const checkedGoalIds = [...document.querySelectorAll("input[name='goalCheck']:checked")].map(cb => parseInt(cb.value));
                const retrospectiveInputs = [...document.querySelectorAll("input[name='retrospectives']")];
                const retrospectives = retrospectiveInputs.map(input => input.value.trim());
                body.completedGoalIds = checkedGoalIds;
                body.retrospectives = retrospectives;
            }

            const url = isEditMode ? `/api/diary/${diaryId}` : '/api/diary/diaries';
            const method = isEditMode ? "PUT" : "POST";

            try {
                const res = await fetch(url, {
                    method,
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
    } // ✅ 이 괄호가 if(saveBtn)의 끝


    // ✅ 글자 수 카운트
    if (contentInput && charCountDisplay) {
        contentInput.addEventListener("input", () => {
            charCountDisplay.textContent = `(${contentInput.value.length}자)`;
        });
    }

    // ✅ 별점 표시
    if (stars.length && satisfactionInput) {
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
    }

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
