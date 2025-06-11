package com.multi.tracklearn.domain;

public enum Tone {
    SOFT, DIRECT;

    public Feedback.ToneType toFeedbackToneType() {
        return this == SOFT ? Feedback.ToneType.soft : Feedback.ToneType.direct;
    }
}
