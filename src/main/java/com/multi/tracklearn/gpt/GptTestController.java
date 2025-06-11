package com.multi.tracklearn.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gpt")
@RequiredArgsConstructor
public class GptTestController {

    private final ChatClient chatClient;

    @GetMapping("/test")
    public String testPrompt(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }
}
