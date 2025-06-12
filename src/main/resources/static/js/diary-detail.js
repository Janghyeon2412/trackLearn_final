document.addEventListener("DOMContentLoaded", function () {

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

});
