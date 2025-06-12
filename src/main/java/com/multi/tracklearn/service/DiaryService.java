package com.multi.tracklearn.service;


import com.multi.tracklearn.domain.*;
import com.multi.tracklearn.dto.DiaryDetailDTO;
import com.multi.tracklearn.dto.DiaryEditDTO;
import com.multi.tracklearn.dto.DiaryListDTO;
import com.multi.tracklearn.dto.DiarySaveDTO;
import com.multi.tracklearn.gpt.GptFeedbackService;
import com.multi.tracklearn.repository.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.multi.tracklearn.dto.GptFeedbackRequestDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final GoalLogRepository goalLogRepository;
    private final GoalRepository goalRepository;
    private final FeedbackRepository feedbackRepository;
    private final GptFeedbackService gptFeedbackService;
    private final NotificationService notificationService;
    private final UserSettingService userSettingService;

    @Transactional
    public void saveDiary(String email, DiarySaveDTO diarySaveDTO) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Diary diary;


        List<GoalLog> todayLogs = goalLogRepository.findByUserIdAndDate(user.getId(), LocalDate.now());
        List<Long> todayGoalLogIds = todayLogs.stream().map(GoalLog::getId).toList();

        if (diarySaveDTO.getDiaryId() != null) {
            // 수정 모드
            diary = diaryRepository.findById(diarySaveDTO.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }

            diary.setUpdatedAt(LocalDateTime.now());
            diary.setModifiedPerson(user.getNickname());

        } else {
            boolean alreadyExists = diaryRepository.existsByUserAndDate(user, LocalDate.now());
            if (alreadyExists) {
                throw new IllegalStateException("이미 오늘 일지를 작성하셨습니다.");
            }

            diary = new Diary();
            diary.setUser(user);
            diary.setCreatedAt(LocalDateTime.now());
            diary.setCreatedPerson(user.getNickname());
        }


        diary.setGoalLogIds(new ArrayList<>(new LinkedHashSet<>(todayGoalLogIds)));

        diary.setTitle(diarySaveDTO.getTitle());
        diary.setContent(diarySaveDTO.getContent());
        diary.setDifficulty(diarySaveDTO.getDifficulty());
        diary.setTomorrowPlan(diarySaveDTO.getTomorrowPlan());

        String summary = generateSummary(diarySaveDTO.getContent(), 30);
        diary.setSummary(summary.length() > 30 ? summary.substring(0, 30) : summary);

        diary.setSatisfaction(diarySaveDTO.getSatisfaction());
        diary.setStudyTime(diarySaveDTO.getStudyTime());
        diary.setDate(LocalDate.now());

        // 회고 보정
        List<String> retrospectives = diarySaveDTO.getRetrospectives() != null
                ? new ArrayList<>(diarySaveDTO.getRetrospectives())
                : new ArrayList<>();

        retrospectives = retrospectives.stream()
                .map(r -> r.length() > 30 ? r.substring(0, 30) : r)
                .collect(Collectors.toList());


        if (diary.getRetrospectives() == null) {
            diary.setRetrospectives(new ArrayList<>());
        } else {
            diary.getRetrospectives().clear();
        }
        diary.getRetrospectives().addAll(retrospectives); // ★ 필수
        diaryRepository.save(diary);


        List<Long> logIds = diarySaveDTO.getCompletedGoalIds();
        if (logIds != null && !logIds.isEmpty()) {
            List<GoalLog> logs = goalLogRepository.findAllById(logIds);
            for (GoalLog log : logs) {
                log.markChecked();
                goalLogRepository.save(log);
            }

            Set<Long> goalIds = logs.stream()
                    .map(log -> log.getGoal().getId())
                    .collect(Collectors.toSet());

            goalIds.forEach(this::updateGoalProgress);
        }
    }


    // 요약 생성 함수
    private String generateSummary(String content, int maxLength) {
        if (content == null) return "";
        String flat = content.replaceAll("[\\n\\r\\t]+", " ");

        flat = flat.replaceAll(" +", " ").trim();
        return flat.length() > maxLength ? flat.substring(0, maxLength) + "..." : flat;
    }


    private void updateGoalProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표"));

        int total = goalLogRepository.countByGoal(goal);
        int completed = goalLogRepository.countByGoalAndIsCheckedIsTrue(goal);

        int rate = total == 0 ? 0 : (int) ((completed / (double) total) * 100);
        goal.updateProgress(rate);
        goalRepository.save(goal);
        goalRepository.flush();
    }


    @Transactional(readOnly = true)
    public List<DiaryListDTO> getDiaryList(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<Diary> diaries = diaryRepository.findByUserIdOrderByDateDesc(user.getId());

        return diaries.stream()
                .map(diary -> new DiaryListDTO(
                        diary.getId(),
                        diary.getTitle(),
                        diary.getSummary(),
                        diary.getDate().toString(),
                        "",
                        diary.getSatisfaction(),
                        diary.isFavorite(),
                        diary.getContent(),
                        diary.getGoalLogIds()
                ))
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public Page<DiaryListDTO> getDiariesSorted(String email, String sort, int page, int size) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Diary> diaryPage;

        switch (sort) {
            case "oldest":
                diaryPage = diaryRepository.findByUserIdOrderByCreatedAtAsc(user.getId(), pageable);
                break;
            case "rating":
                diaryPage = diaryRepository.findByUserIdOrderBySatisfactionDesc(user.getId(), pageable);
                break;
            case "favorite":
                diaryPage = diaryRepository.findByUserIdOrderByIsFavoriteDescCreatedAtDesc(user.getId(), pageable);
                break;
            case "latest":
            default:
                diaryPage = diaryRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
                break;
        }

        return diaryPage.map(diary -> new DiaryListDTO(
                diary.getId(),
                diary.getTitle(),
                diary.getSummary(),
                diary.getDate().toString(),
                "", // tags
                diary.getSatisfaction(),
                diary.isFavorite(),
                diary.getContent(),
                diary.getGoalLogIds()
        ));


    }


    @Transactional
    public void updateFavoriteStatus(String email, Long diaryId, boolean value) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 일지에 접근할 권한이 없습니다.");
        }

        diary.setFavorite(value);
    }



    @Transactional(readOnly = true)
    public DiaryEditDTO prepareEditFormByDiaryId(Long diaryId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        List<Long> checkedGoalLogIds = diary.getGoalLogIds() != null
                ? new ArrayList<>(new LinkedHashSet<>(diary.getGoalLogIds()))
                : new ArrayList<>();

        LocalDate today = diary.getDate();
        List<GoalLog> todayGoalLogs = goalLogRepository.findByUserIdAndDate(user.getId(), today);

        for (GoalLog log : todayGoalLogs) {
            log.setChecked(checkedGoalLogIds.contains(log.getId()));
        }

        for (GoalLog log : todayGoalLogs) {
            boolean isChecked = checkedGoalLogIds.contains(log.getId());
            log.setChecked(isChecked);
        }


        return DiaryEditDTO.fromEntityWithAllGoalLogs(diary, todayGoalLogs);
    }





    @Transactional
    public void updateDiaryByDiaryId(Long diaryId, DiaryEditDTO dto, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        // 오늘 전체 목표 조회
        List<GoalLog> todayLogs = goalLogRepository.findByUserIdAndDate(user.getId(), diary.getDate());
        List<Long> allGoalLogIds = todayLogs.stream().map(GoalLog::getId).toList();

        List<Long> checkedGoalIds = dto.getGoalLogIds() != null
                ? new ArrayList<>(new LinkedHashSet<>(dto.getGoalLogIds()))
                : new ArrayList<>();

        List<String> retrospectives = dto.getRetrospectives() != null
                ? new ArrayList<>(dto.getRetrospectives())
                : new ArrayList<>();

        while (retrospectives.size() < checkedGoalIds.size()) retrospectives.add("");
        if (retrospectives.size() > checkedGoalIds.size()) {
            retrospectives = retrospectives.subList(0, checkedGoalIds.size());
        }

        retrospectives = retrospectives.stream()
                .map(r -> r.length() > 150 ? r.substring(0, 150) : r)
                .collect(Collectors.toList());

        // 회고 리스트
        if (diary.getRetrospectives() == null) {
            diary.setRetrospectives(new ArrayList<>());
        } else {
            diary.getRetrospectives().clear();
        }
        diary.getRetrospectives().addAll(retrospectives);
        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setStudyTime(dto.getStudyTime());
        diary.setSatisfaction(dto.getSatisfaction());
        diary.setUpdatedAt(LocalDateTime.now());
        diary.setModifiedPerson(user.getNickname());
        diary.setDifficulty(dto.getDifficulty());
        diary.setTomorrowPlan(dto.getTomorrowPlan());
        diary.setGoalLogIds(new ArrayList<>(allGoalLogIds));

        diaryRepository.save(diary);

        for (GoalLog log : todayLogs) {
            if (checkedGoalIds.contains(log.getId())) {
                log.markChecked();
            } else {
                log.uncheck();
            }
            goalLogRepository.save(log);
        }

        Set<Long> goalIds = todayLogs.stream()
                .map(log -> log.getGoal().getId())
                .collect(Collectors.toSet());
        goalIds.forEach(this::updateGoalProgress);
    }



    @Transactional(readOnly = true)
    public DiaryEditDTO prepareEditForm(Long goalLogId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        GoalLog goalLog = goalLogRepository.findById(goalLogId)
                .orElseThrow(() -> new IllegalArgumentException("목표 일정을 찾을 수 없습니다."));

        List<GoalLog> goalLogs = goalLogRepository.findByDateAndUserId(goalLog.getDate(), user.getId());

        Optional<Diary> optionalDiary = diaryRepository.findByUserAndGoalLogId(user, goalLogId);

        System.out.println("goalLogId: " + goalLogId);
        System.out.println("userId: " + user.getId());
        System.out.println("goalLogs: " + goalLogs.size());
        System.out.println("optionalDiary.isPresent(): " + optionalDiary.isPresent());

        if (optionalDiary.isPresent()) {
            Diary diary = optionalDiary.get();

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }

            return DiaryEditDTO.fromEntityWithAllGoalLogs(diary, goalLogs);
        }

        if (!goalLog.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        return DiaryEditDTO.fromGoalLogs(goalLogs);
    }

    // 상세보기
    public DiaryDetailDTO getDiaryDetail(Long diaryId, String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지 없음"));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("조회 권한 없음");
        }

        List<GoalLog> goalLogs = goalLogRepository.findAllById(diary.getGoalLogIds());
        List<Feedback> feedbacks = feedbackRepository.findByDiaryId(diaryId);

        return DiaryDetailDTO.fromEntity(diary, goalLogs, feedbacks);
    }



    public boolean existsTodayDiaryByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now();
        return diaryRepository.findByUserAndDate(user, today).isPresent();
    }


    public Optional<Diary> findDiaryByGoalLogId(Long goalLogId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        return diaryRepository.findByUserAndGoalLogId(user, goalLogId);
    }


    public Diary getDiaryByIdAndUserEmail(Long diaryId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        Diary diary = diaryRepository.findWithGoalLogIds(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        return diary;
    }

    @Transactional
    public String generateFeedbackByDiaryId(Long diaryId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        List<GoalLog> goalLogs = goalLogRepository.findByDiaryId(diaryId);

        GptFeedbackRequestDTO requestDto = GptFeedbackRequestDTO.fromDiary(diary, goalLogs);

        String prompt = gptFeedbackService.generatePrompt(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getStudyTime(),
                requestDto.getSatisfaction(),
                requestDto.getGoals(),
                requestDto.getRetrospectives(),
                requestDto.getGoalDetails(),
                requestDto.getGoalReasons(),
                requestDto.getLearningStyles(),
                requestDto.getDifficulty(),
                requestDto.getTomorrowPlan()
        );

        // GPT 응답
        String feedback = gptFeedbackService.getFeedback(
                requestDto.getTone(),
                requestDto.getSubject(),
                prompt
        );

        return feedback;
    }


    @Transactional
    public String generateAndSaveFeedback(GptFeedbackRequestDTO dto, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("사용자 없음");

        Diary diary = null;
        if (dto.getDiaryId() != null) {
            diary = diaryRepository.findById(dto.getDiaryId())
                    .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("접근 권한이 없습니다.");
            }
        }

        String prompt = gptFeedbackService.generatePrompt(
                dto.getTitle(),
                dto.getContent(),
                dto.getStudyTime(),
                dto.getSatisfaction(),
                dto.getGoals(),
                dto.getRetrospectives(),
                dto.getGoalDetails(),
                dto.getGoalReasons(),
                dto.getLearningStyles(),
                dto.getDifficulty(),
                dto.getTomorrowPlan()
        );

        String response = gptFeedbackService.getFeedback(dto.getTone(), dto.getSubject(), prompt);

        if (diary != null) {
            List<Feedback> existing = feedbackRepository.findByDiaryId(diary.getId());
            feedbackRepository.deleteAll(existing);

            List<String> sections = Arrays.stream(response.split("\\n\\n"))
                    .map(String::trim)
                    .toList();

            for (int i = 0; i < sections.size(); i++) {
                Feedback feedback = new Feedback();
                feedback.setDiary(diary);
                feedback.setToneType(Feedback.ToneType.soft);
                feedback.setContent(sections.get(i));
                feedback.setCreatedPerson("GPT");
                feedback.setModifiedPerson("GPT");

                switch (i) {
                    case 0 -> feedback.setResponseType(Feedback.ResponseType.cheer);
                    case 1 -> feedback.setResponseType(Feedback.ResponseType.advice);
                    case 2 -> feedback.setResponseType(Feedback.ResponseType.adjust);
                    default -> feedback.setResponseType(Feedback.ResponseType.advice);
                }

                feedbackRepository.save(feedback);
            }

            UserSetting setting = userSettingService.getSetting(user.getId());
            if (setting.getGptFeedbackNotify()) {
                notificationService.create(
                        user,
                        "GPT 피드백이 도착했습니다. 일지를 확인해보세요.",
                        Notification.NotificationType.feedback
                );
            }
        }

        return response;
    }

}



