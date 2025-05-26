package com.multi.tracklearn.controller;

import com.multi.tracklearn.dto.CategoryDTO;
import com.multi.tracklearn.dto.GoalCreateDTO;
import com.multi.tracklearn.dto.GoalListDTO;
import com.multi.tracklearn.dto.GoalUpdateDTO;
import com.multi.tracklearn.service.CategoryService;
import com.multi.tracklearn.service.GoalService;
import com.multi.tracklearn.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalPageController {

    private final GoalService goalService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/create")
    public String showGoalForm(Model model, Authentication authentication) {
        String email = authentication != null ? (String) authentication.getPrincipal() : null;

        String nickname = null;
        if (email != null) {
            nickname = userService.findNicknameByEmail(email);
        }

        List<CategoryDTO> categories = categoryService.findAll();

        // **5개만 보여주는 부분 수정**
        List<GoalListDTO> goals = goalService.getGoals(email)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("categories", categories);
        model.addAttribute("goalCreateDTO", new GoalCreateDTO());
        model.addAttribute("nickname", nickname);
        model.addAttribute("goals", goals);

        return "goal/goal-form";
    }




    @PostMapping("/create")
    public String submitGoalForm(Authentication authentication, @Valid @ModelAttribute GoalCreateDTO goalCreateDTO, BindingResult bindingResult, Model model) {

        String email = authentication != null ? (String) authentication.getPrincipal() : null;


        if (goalCreateDTO.getGoalId() != null) {
            GoalUpdateDTO updateDTO = new GoalUpdateDTO();
            updateDTO.setGoalId(goalCreateDTO.getGoalId());
            updateDTO.setTitle(goalCreateDTO.getTitle());
            updateDTO.setRepeatType(goalCreateDTO.getRepeatType());
            updateDTO.setRepeatValue(goalCreateDTO.getRepeatValue());
            updateDTO.setCategoryId(goalCreateDTO.getCategoryId());

            goalService.updateGoal(email, goalCreateDTO.getGoalId(), updateDTO);
        } else {
            goalService.createGoal(email, goalCreateDTO);
        }



        return "redirect:/goals/create";
    }

    @PostMapping
    public String createGoal(@AuthenticationPrincipal String email, GoalCreateDTO goalCreateDTO) {
        goalService.createGoal(email, goalCreateDTO);
        return "redirect:/goals/create";
    }



    @PostMapping("/update")
    public String updateGoal(@ModelAttribute GoalUpdateDTO goalUpdateDTO, @AuthenticationPrincipal String email) {
        goalService.updateGoal(email, goalUpdateDTO.getGoalId(), goalUpdateDTO);
        return "redirect:/goals/create";
    }




}
