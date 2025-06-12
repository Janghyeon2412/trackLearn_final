document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("accessToken");

    try {
        const res = await fetch("/api/settings", {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();

        const setting = await res.json();

        // 톤 초기화
        const toneRadio = document.querySelector(`input[name="tone"][value="${setting.tone}"]`);
        if (toneRadio) toneRadio.checked = true;

        // 알림 체크 초기화
        document.getElementById("gptAlarm").checked = setting.gptFeedbackNotify;
        document.getElementById("dailyAlarm").checked = setting.goalArrivalNotify;
        document.getElementById("deadlineAlarm").checked = setting.diaryMissingNotify;

    } catch (e) {
        console.error("설정 초기화 실패", e);
    }


    // 사용자 정보
    try {
        const res = await fetch("/api/settings/profile", {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("불러오기 실패");

        const data = await res.json();
        document.getElementById("nicknameField").innerText = data.nickname;
        document.getElementById("emailField").innerText = data.email;
    } catch (err) {
        alert("사용자 정보를 불러오지 못했습니다.");
    }

    // 닉네임 변경
    document.getElementById("updateNicknameBtn").addEventListener("click", async () => {
        const newNickname = document.getElementById("newNickname").value;
        if (!newNickname) return alert("새 닉네임을 입력하세요.");

        const res = await fetch("/api/settings/nickname", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({ nickname: newNickname })
        });

        if (res.ok) {
            alert("닉네임이 변경되었습니다.");
            location.reload();
        } else {
            alert("닉네임 변경 실패");
        }
    });

    // 비밀번호 변경
    document.getElementById("changePasswordBtn").addEventListener("click", async () => {
        const currentPassword = document.getElementById("currentPassword").value;
        const newPassword = document.getElementById("newPassword").value;

        if (!currentPassword || !newPassword) return alert("모든 비밀번호를 입력하세요.");

        const res = await fetch("/api/settings/password", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({ currentPassword, newPassword })
        });

        if (res.ok) {
            alert("비밀번호가 변경되어 로그아웃됩니다.");

            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");

            // 로그인 페이지로 이동
            location.href = "/login";
        } else {
            const message = await res.text();
            alert(message);
        }
    });


    // GPT 설정
    document.getElementById("saveGptSettingBtn").addEventListener("click", async () => {
        const selectedTone = document.querySelector('input[name="tone"]:checked')?.value;
        if (!selectedTone) return alert("톤을 선택하세요.");

        const res = await fetch("/api/settings/gpt", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({ tone: selectedTone })
        });

        if (res.ok) alert("GPT 설정이 저장되었습니다.");
        else alert("GPT 설정 저장 실패");
    });

    // 알림 설정 저장
    document.getElementById("saveNotificationBtn").addEventListener("click", async () => {
        const gpt = document.getElementById("gptAlarm").checked;
        const daily = document.getElementById("dailyAlarm").checked;
        const deadline = document.getElementById("deadlineAlarm").checked;

        const res = await fetch("/api/settings/notifications", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({
                gptFeedbackNotify: gpt,
                goalArrivalNotify: daily,
                goalArrivalEmailNotify: daily,
                diaryMissingNotify: deadline,
                diaryMissingEmailNotify: deadline
            })
        });

        if (res.ok) alert("알림 설정 저장 완료");
        else alert("알림 설정 저장 실패");
    });

    // 계정 탈퇴
    document.getElementById("deleteAccountLink").addEventListener("click", async (e) => {
        e.preventDefault();
        if (!confirm("정말 탈퇴하시겠습니까?")) return;

        const token = localStorage.getItem("accessToken");
        const res = await fetch("/users/me", {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (res.ok) {
            alert("계정이 삭제되었습니다.");
            localStorage.clear();
            window.location.href = "/login";
        } else {
            alert("계정 삭제 실패");
        }
    });


});
