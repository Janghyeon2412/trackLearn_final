document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("diary-card-list");
    const pagination = document.getElementById("pagination");
    const sortSelect = document.getElementById("sortOption");
    let currentPage = 1;

    function renderStars(score) {
        let result = "";
        for (let i = 1; i <= 5; i++) {
            if (score >= i) {
                result += "<img src='/images/star-full.png' class='rating-star' />";
            } else if (score >= i - 0.5) {
                result += "<img src='/images/star-half.png' class='rating-star' />";
            } else {
                result += "<img src='/images/star-empty.png' class='rating-star' />";
            }
        }
        return result;
    }


    function renderDiaries(diaries) {
        container.innerHTML = "";

        diaries.forEach(diary => {
            const card = document.createElement("div");
            card.className = "diary-card";
            card.setAttribute("data-id", diary.id);

            const isFav = diary.isFavorite;

            const shortTitle = diary.title.length > 10 ? diary.title.slice(0, 10) + "..." : diary.title;
            const shortContent = (diary.content || '').replace(/\n/g, '<br>');

            const goalLogId = diary.goalLogIds && diary.goalLogIds.length > 0 ? diary.goalLogIds[0] : null;
            console.log("goalLogId:", goalLogId);


            const editButton = `
            <button class="edit-btn btn-edit" data-goal-log-id="${goalLogId}">수정</button>`;

            card.innerHTML = `
            <div class="diary-header">
                <div class="diary-top">
                    <span class="diary-date">${diary.date}</span>
                    <img src="${isFav ? '/images/star-full.png' : '/images/star-empty.png'}"
                         class="favorite-icon"
                         alt="즐겨찾기"
                         data-fav="${isFav}" />
                </div>
                <div class="diary-title-rating">
                    <span class="diary-title">${shortTitle}</span>
                    <div class="diary-stars">${renderStars(diary.satisfaction)}</div>
                </div>
            </div>

            <hr>

            <div class="card-body">
                <div class="diary-content">${shortContent}</div>
            </div>

            <hr>

            <div class="diary-footer">
                ${editButton}
                <button class="btn-detail">상세 보기</button>
            </div>
        `;

            container.appendChild(card);
        });

        const placeholdersToAdd = 9 - diaries.length;
        for (let i = 0; i < placeholdersToAdd; i++) {
            const placeholder = document.createElement("div");
            placeholder.className = "diary-card empty-card";
            container.appendChild(placeholder);
        }

        // 상세보기 버ㅌ,ㄴ
        const detailButtons = container.querySelectorAll(".btn-detail");
        detailButtons.forEach(btn => {
            btn.addEventListener("click", function () {
                const diaryId = this.closest(".diary-card").getAttribute("data-id");
                if (diaryId) {
                    window.location.href = `/diary/detail/${diaryId}`;
                }
            });
        });

    }


    function truncateMultilineText(text, maxLines = 3) {
        const lines = text.split('\n');
        const result = [];

        for (let i = 0; i < lines.length; i++) {
            if (result.length >= maxLines) break;

            const line = lines[i];
            if (result.length === maxLines - 1 && i < lines.length - 1) {
                result.push(line + '...');
                break;
            }
            result.push(line);
        }

        return result.join('<br>');
    }



    function renderPagination(totalPages, current) {
        pagination.innerHTML = "";
        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement("button");
            btn.innerText = i;
            if (i === current) btn.classList.add("active");
            btn.addEventListener("click", () => {
                currentPage = i;
                fetchDiaries(i, sortSelect.value);
            });
            pagination.appendChild(btn);
        }
    }

    async function fetchDiaries(page = 1, sort = "latest") {
        try {
            const res = await fetch(`/api/diary/list?page=${page}&sort=${sort}`);
            const data = await res.json();
            renderDiaries(data.content);
            renderPagination(data.totalPages, page);
        } catch (err) {
            console.error("일지 목록 불러오기 에러:", err);
            container.innerHTML = "일지를 불러오는 데 실패했습니다.";
        }
    }

    sortSelect.addEventListener("change", () => {
        fetchDiaries(1, sortSelect.value);
    });

    container.addEventListener("click", async function (e) {
        if (e.target.classList.contains("favorite-icon")) {
            const icon = e.target;
            const card = icon.closest(".diary-card");
            const diaryId = card.getAttribute("data-id");
            const isFav = icon.getAttribute("data-fav") === "true";

            icon.src = isFav ? "/images/star-empty.png" : "/images/star-full.png";
            icon.setAttribute("data-fav", (!isFav).toString());

            try {
                const response = await fetch(`/api/diary/${diaryId}/favorite?value=${!isFav}`, {
                    method: "PATCH"
                });

                if (!response.ok) {
                    throw new Error("서버 응답 실패");
                }

            } catch (err) {
                console.error("즐겨찾기 토글 실패:", err);
                alert("즐겨찾기 상태 변경에 실패했습니다.");

                icon.src = isFav ? "/images/star-full.png" : "/images/star-empty.png";
                icon.setAttribute("data-fav", isFav.toString());
            }
        }

        // 수정
        if (e.target.classList.contains("edit-btn")) {
            const goalLogId = e.target.getAttribute("data-goal-log-id");
            console.log("🧪 goalLogId:", goalLogId);
            if (!goalLogId) {
                alert("수정할 수 없는 일지입니다.");
                return;
            }
            window.location.href = `/diary/edit/${goalLogId}`;
        }

    });

    fetchDiaries();
});