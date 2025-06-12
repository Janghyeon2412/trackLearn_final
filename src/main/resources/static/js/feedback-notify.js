let userSetting = {};

window.addEventListener("DOMContentLoaded", async () => {
    try {
        const res = await fetch("/api/settings");
        if (res.ok) {
            userSetting = await res.json();
        }
    } catch (e) {
        console.error("설정 로딩 실패", e);
    }
});

async function generateGptFeedback(diaryId) {
    const res = await fetch(`/api/diary/${diaryId}/gpt-feedback`, {
        method: "POST"
    });

    if (res.ok) {
    } else {
        console.error("GPT 피드백 생성 실패");
    }
}

