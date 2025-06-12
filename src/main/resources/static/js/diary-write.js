document.addEventListener("DOMContentLoaded", () => {
    const isEditMode = document.getElementById("editMode")?.value === "true";
    const goalLogId = document.getElementById("goalLogId")?.value;
    const diaryId = document.getElementById("diaryId")?.value;

    const difficultyInput = document.getElementById("difficulty");
    const tomorrowPlanInput = document.getElementById("tomorrowPlan");


    const titleInput = document.getElementById("title");
    const contentInput = document.getElementById("content");
    const summaryInput = document.getElementById("summary"); // optional
    const satisfactionInput = document.getElementById("satisfaction");
    const hourInput = document.getElementById("studyHour");
    const minuteInput = document.getElementById("studyMinute");


    let cachedGoalDetails = [];
    let cachedGoalReasons = [];
    let cachedLearningStyles = [];
    let userTone = "SOFT";

    (async () => {
        try {
            const res = await fetch("/api/settings", { credentials: "include" });
            if (res.ok) {
                const setting = await res.json();
                userTone = setting.tone || "SOFT";
            }
        } catch (e) {
            console.warn("톤 불러오기 실패, 기본값 사용:", e);
        }
    })();


    function onlyAllowNumbers(input) {
        input.addEventListener("input", () => {
            input.value = input.value.replace(/[^0-9]/g, "");
        });
    }

    if (hourInput && minuteInput) {
        onlyAllowNumbers(hourInput);
        onlyAllowNumbers(minuteInput);
    }

    const saveBtn = document.getElementById("saveDiaryBtn");
    const charCountDisplay = document.getElementById("char-count");

    const stars = document.querySelectorAll('.star-rating span');

    // 작성 모드에서만
    if (!isEditMode) {
        const todayDate = document.getElementById('today-date');
        if (todayDate) {
            const now = new Date();
            const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
                .toISOString()
                .split('T')[0];
            todayDate.innerText = localDate;

            fetch("/api/diary/today-written", {
                credentials: "include"
            })
                .then(res => {
                    if (res.status === 409) {
                        alert("오늘은 이미 일지를 작성했습니다. 수정하거나 기존 일지를 확인해주세요.");
                        window.location.href = "/main";
                    }
                })
                .catch(err => {
                    console.error("중복 일지 확인 실패", err);
                });
        }

        // 오늘의 목표 체크박스
        fetch("/api/diary/today-goals", {
            credentials: "include"
        })
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
                wrapper.dataset.goalDetail = goal.goalDetail || "";
                wrapper.dataset.goalReason = goal.goalReason || "";
                wrapper.dataset.learningStyle = goal.learningStyle || "";

                wrapper.innerHTML = `
            <label>
                <input type="checkbox" name="goalCheck" value="${goal.goalLogId}">
                <span class="goal-title">${goal.title} (${goal.categoryName}, ${goal.repeatText})</span>
            </label>
            <textarea name="retrospectives" class="retrospective-input" rows="3" maxlength="150"
              placeholder="이 목표에 대한 회고를 구체적으로 작성해주세요 (예: 어떤 점이 어려웠고, 무엇을 느꼈는지 등)"></textarea>
        `;
                container.appendChild(wrapper);
            });
        }
    }

    // 수정 모드 초기값 채우기
    if (isEditMode && diaryId) {
        fetch(`/api/diary/${diaryId}`, {
            credentials: "include"
        })
            .then(res => res.json())
            .then(data => {

                cachedGoalDetails = data.goalDetails || [];
                cachedGoalReasons = data.goalReasons || [];
                cachedLearningStyles = data.learningStyles || [];


                titleInput.value = data.title || "";
                contentInput.value = data.content || "";
                satisfactionInput.value = data.satisfaction ?? 0;
                hourInput.value = data.studyTime ? Math.floor(data.studyTime / 60) : 0;
                minuteInput.value = data.studyTime ? data.studyTime % 60 : 0;

                difficultyInput.value = data.difficulty || "";
                tomorrowPlanInput.value = data.tomorrowPlan || "";

                highlightStars(data.satisfaction ?? 0);

                const container = document.getElementById("goal-checkbox-list");
                if (!container) return;

                const checkedIds = new Set(data.goalLogIds || []);
                const checkedIdArr = [...checkedIds];
                const retrospectives = data.retrospectives || [];

                const allLogs = data.allGoalLogs || [];
                container.innerHTML = "";

                allLogs.forEach((log) => {
                    const isChecked = log.checked;
                    const retrospectiveText = isChecked
                        ? (retrospectives[checkedIdArr.indexOf(log.id)] || "")
                        : "";

                    const wrapper = document.createElement("div");
                    wrapper.className = "checkbox-wrap";
                    wrapper.innerHTML = `
                    <label>
                        <input type="checkbox" name="goalCheck" value="${log.id}" ${isChecked ? 'checked' : ''}>
                        <span class="goal-title">${log.title}</span>
                    </label>
                    <input type="hidden" name="goalLogIds" value="${log.id}" />
                    <input type="text" name="retrospectives" class="retrospective-input" value="${retrospectiveText}" placeholder="이 목표에 대한 회고를 입력하세요">
                `;
                    container.appendChild(wrapper);
                });

            })
            .catch(err => {
                console.error("수정 데이터 로딩 실패", err);
            });
    }




    // 저장
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

            body.difficulty = difficultyInput?.value.trim() ?? "";
            body.tomorrowPlan = tomorrowPlanInput?.value.trim() ?? "";


            if (isEditMode) {
                const checkedBoxes = [...document.querySelectorAll("input[name='goalCheck']:checked")];

                const goalLogIds = checkedBoxes.map(input => parseInt(input.value));
                const retrospectives = checkedBoxes.map(box =>
                    box.closest(".checkbox-wrap")?.querySelector(".retrospective-input")?.value.trim() ?? ""
                );

                for (let r of retrospectives) {
                    if (r.length > 150) {
                        alert("각 회고는 최대 150자까지 입력 가능합니다.");
                        return;
                    }
                }

                body.goalLogIds = goalLogIds;
                body.retrospectives = retrospectives;
                body.diaryId = parseInt(diaryId);
            } else {
                const checkedBoxes = [...document.querySelectorAll("input[name='goalCheck']:checked")];

                const checkedGoalIds = checkedBoxes.map(cb => parseInt(cb.value));
                const retrospectives = checkedBoxes.map(cb =>
                    cb.closest(".checkbox-wrap")?.querySelector(".retrospective-input")?.value.trim() || ""
                );


                for (let r of retrospectives) {
                    if (r.length > 150) {
                        alert("각 회고는 최대 150자까지 입력 가능합니다.");
                        return;
                    }
                }

                body.completedGoalIds = checkedGoalIds;
                body.retrospectives = retrospectives;
            }

            const url = isEditMode ? `/api/diary/${diaryId}` : '/api/diary/diaries';
            const method = isEditMode ? "PUT" : "POST";

            try {
                const res = await fetch(url, {
                    method,
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body),
                    credentials: "include"
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
    }


    // 글자 수 카운트
    if (contentInput && charCountDisplay) {
        contentInput.addEventListener("input", () => {
            charCountDisplay.textContent = `(${contentInput.value.length}자)`;
        });
    }

    // 별점 표시
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

    // GPT 피드백 요청 버튼
    const gptBtn = document.getElementById("gptFeedbackBtn");
    const gptResultBox = document.getElementById("gptFeedbackResult");

    if (gptBtn && gptResultBox) {
        gptBtn.addEventListener("click", async () => {
            const title = titleInput?.value.trim();
            const content = contentInput?.value.trim();
            const hour = parseInt(hourInput?.value) || 0;
            const minute = parseInt(minuteInput?.value) || 0;
            const studyTime = hour * 60 + minute;
            const satisfaction = parseFloat(satisfactionInput?.value) || 0;
            const difficulty = difficultyInput?.value.trim() || "";
            const tomorrowPlan = tomorrowPlanInput?.value.trim() || "";

            const checkedGoalEls = [...document.querySelectorAll("input[name='goalCheck']:checked")];
            const checkedGoalIds = checkedGoalEls.map(cb => parseInt(cb.value));
            const goals = checkedGoalEls.map(cb => cb.closest(".checkbox-wrap")?.querySelector(".goal-title")?.innerText || "");
            const retrospectives = checkedGoalEls.map(cb => cb.closest(".checkbox-wrap")?.querySelector(".retrospective-input")?.value.trim() || "");

            const goalDetails = isEditMode
                ? checkedGoalIds.map(id => {
                    const index = (window.loadedGoalLogIds || []).indexOf(id);
                    return index !== -1 ? cachedGoalDetails[index] : "";
                })
                : checkedGoalEls.map(cb => cb.closest(".checkbox-wrap")?.dataset.goalDetail || "");

            const goalReasons = isEditMode
                ? checkedGoalIds.map(id => {
                    const index = (window.loadedGoalLogIds || []).indexOf(id);
                    return index !== -1 ? cachedGoalReasons[index] : "";
                })
                : checkedGoalEls.map(cb => cb.closest(".checkbox-wrap")?.dataset.goalReason || "");

            const learningStyles = isEditMode
                ? checkedGoalIds.map(id => {
                    const index = (window.loadedGoalLogIds || []).indexOf(id);
                    return index !== -1 ? cachedLearningStyles[index] : "";
                })
                : checkedGoalEls.map(cb => cb.closest(".checkbox-wrap")?.dataset.learningStyle || "");


            const body = {
                title,
                content,
                studyTime,
                satisfaction,
                goals,
                retrospectives,
                goalDetails,
                goalReasons,
                learningStyles,
                tone: userTone,
                subject: "학습 피드백",
                difficulty,
                tomorrowPlan
            };

            if (isEditMode) {
                const diaryIdValue = parseInt(document.getElementById("diaryId")?.value || "0");
                if (!diaryIdValue) {
                    alert("GPT 요청 실패: diaryId가 비어 있습니다.");
                    return;
                }
                body.diaryId = diaryIdValue;
            }

            try {
                gptBtn.innerText = "GPT 요청 중...";
                gptBtn.disabled = true;

                const res = await fetch("/api/diary/gpt-feedback", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body),
                    credentials: "include"
                });

                if (res.ok) {
                    const feedback = await res.text();
                    gptResultBox.innerText = feedback;

                } else {
                    const errorText = await res.text();
                    console.error("GPT 요청 실패:", errorText);
                    gptResultBox.innerText = "GPT 요청 실패. 다시 시도해주세요.";
                }

            } catch (err) {
                gptResultBox.innerText = "서버 오류 발생: " + err.message;
            } finally {
                gptBtn.innerText = "GPT 피드백 요청";
                gptBtn.disabled = false;
            }
        });
    }


});
