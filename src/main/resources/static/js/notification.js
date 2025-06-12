document.addEventListener("DOMContentLoaded", async () => {
    const button = document.getElementById("notificationButton");
    const dropdown = document.getElementById("notificationDropdown");
    const list = document.getElementById("notificationList");
    const dot = document.getElementById("notificationDot");

    if (!button || !dropdown || !list) return;

    window.updateNotificationDot = async function () {
        try {
            const res = await fetch("/api/notifications/unread-count", { credentials: "include" });
            const data = await res.json();
            if (data.count > 0 && dot) {
                dot.style.display = "inline-block";
            } else if (dot) {
                dot.style.display = "none";
            }
        } catch (err) {
            console.error("알림 개수 확인 실패", err);
        }
    };

    await window.updateNotificationDot();

    function formatTimeAgo(dateTimeStr) {
        const date = new Date(dateTimeStr);
        const now = new Date();
        const diff = Math.floor((now - date) / 1000); // 초 단위 차이

        if (diff < 60) return `${diff}초 전`;
        if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
        if (diff < 2592000) return `${Math.floor(diff / 86400)}일 전`;
        return date.toLocaleDateString("ko-KR");
    }

    // 클릭 시 드롭다운 + 알림 목록
    button.addEventListener("click", async (e) => {
        e.preventDefault();

        const isVisible = dropdown.style.display === "block";
        dropdown.style.display = isVisible ? "none" : "block";
        if (isVisible) return;

        try {
            const res = await fetch("/api/notifications", { credentials: "include" });
            const data = await res.json();
            list.innerHTML = "";

            if (data.length === 0) {
                const li = document.createElement("li");
                li.textContent = "새로운 알림이 없습니다.";
                list.appendChild(li);
                return;
            }

            data.forEach(n => {
                const li = document.createElement("li");
                li.dataset.id = n.id;

                const contentDiv = document.createElement("div");
                contentDiv.className = "notification-content";
                contentDiv.textContent = n.content;

                const timeDiv = document.createElement("div");
                timeDiv.className = "notification-time";
                timeDiv.textContent = formatTimeAgo(n.createdAt);

                li.appendChild(contentDiv);
                li.appendChild(timeDiv);

                if (n.read === true) {
                    li.classList.add("read");
                } else {
                    li.classList.add("unread");
                    li.style.cursor = "pointer";
                    li.addEventListener("click", async () => {
                        try {
                            await fetch(`/api/notifications/${n.id}/read`, {
                                method: "PATCH",
                                credentials: "include"
                            });
                            li.classList.remove("unread");
                            li.classList.add("read");
                            li.style.fontWeight = "normal";

                            // ✅ dot 상태 다시 갱신
                            await window.updateNotificationDot();
                        } catch (e) {
                            console.error("읽음 처리 실패", e);
                        }
                    });
                }

                list.appendChild(li);
            });

        } catch (err) {
            list.innerHTML = "<li>알림을 불러오지 못했습니다.</li>";
            console.error("알림 불러오기 실패", err);
        }
    });

    // 바깥 클릭 시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!e.target.closest("#notificationButton") && !e.target.closest("#notificationDropdown")) {
            dropdown.style.display = "none";
        }
    });

    setInterval(window.updateNotificationDot, 5000);
});