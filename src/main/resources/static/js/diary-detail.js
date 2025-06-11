document.addEventListener("DOMContentLoaded", function () {
    // ✅ 만족도 별점 표시
    const ratingValue = parseFloat(document.getElementById("satisfaction")?.value || "0");
    const starSpans = document.querySelectorAll(".star-rating span");

    for (let i = 0; i < 5; i++) {
        if (ratingValue >= i + 1) {
            starSpans[i].classList.add("full");
        } else if (ratingValue >= i + 0.5) {
            starSpans[i].classList.add("half");
        } else {
            starSpans[i].classList.remove("full", "half");
        }
    }

    // ✅ GPT 요청 및 저장 버튼 제거됨 (상세보기에서는 표시만)
});
