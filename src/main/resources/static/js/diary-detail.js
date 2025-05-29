document.addEventListener("DOMContentLoaded", function () {
    const ratingValue = parseFloat(document.getElementById("satisfaction").value || "0");
    const starSpans = document.querySelectorAll(".star-rating span");

    for (let i = 0; i < 5; i++) {
        if (ratingValue >= i + 1) {
            starSpans[i].classList.add("full");
        } else if (ratingValue >= i + 0.5) {
            starSpans[i].classList.add("half");
        } else {
            // 기본은 empty 이미지가 적용되어 있어도 명시적으로 설정해줌
            starSpans[i].classList.remove("full", "half");
        }
    }
});
