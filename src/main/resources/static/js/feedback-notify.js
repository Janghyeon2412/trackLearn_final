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

// ✅ 예: GPT 피드백 생성 API 호출 후 토스트 띄우기
async function generateGptFeedback(diaryId) {
    const res = await fetch(`/api/diary/${diaryId}/gpt-feedback`, {
        method: "POST"
    });

    if (res.ok) {
        // 성공 후 아무 것도 하지 않음
    } else {
        console.error("GPT 피드백 생성 실패");
    }
}

