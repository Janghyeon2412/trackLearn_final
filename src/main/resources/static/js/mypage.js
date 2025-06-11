document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    try {
        const res = await fetch("/api/mypage/info", {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("마이페이지 정보를 불러올 수 없습니다.");

        const data = await res.json();

        document.getElementById("nickname").innerText = data.nickname;
        document.getElementById("email").innerText = data.email;
        document.getElementById("joinDate").innerText = new Date(data.createdAt).toLocaleDateString();

        document.getElementById("diaryCount").innerText = data.diaryCount + "개";
        document.getElementById("totalTime").innerText = (data.totalStudyMinutes / 60).toFixed(1) + "시간";
        document.getElementById("avgSatisfaction").innerText = data.averageSatisfaction.toFixed(2) + "점";

        const list = document.getElementById("recentGoals");
        data.recentGoals.forEach(g => {
            const li = document.createElement("li");
            li.innerText = `${g.date} - ${g.title} (${g.categoryName})`;
            list.appendChild(li);
        });

    } catch (err) {
        alert(err.message);
    }
});


function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    return parts.length === 2 ? parts.pop().split(';').shift() : '';
}
