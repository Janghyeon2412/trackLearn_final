document.addEventListener("DOMContentLoaded", () => {
    const passwordInput = document.getElementById("password");
    const hint = document.getElementById("passwordHint");

    passwordInput.addEventListener("input", () => {
        const value = passwordInput.value;
        const isValid = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/.test(value);

        if (value.length === 0 || isValid) {
            hint.classList.remove("invalid");
        } else {
            hint.classList.add("invalid"); // 빨간색 전환
        }
    });
});
