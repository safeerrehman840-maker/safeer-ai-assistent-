package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SafeerApp
import com.example.ai.AssistantAction
import com.example.ai.GeminiAssistantClient
import com.example.ai.ParsedAssistantResponse
import com.example.data.local.*
import com.example.service.AlarmSchedulerHelper
import com.example.service.PhoneActionExecutor
import com.example.service.SpeechManager
import com.example.ui.components.AssistantStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SafeerApp
    private val repository = app.repository
    private val geminiClient = GeminiAssistantClient()
    val speechManager = SpeechManager(application)
    val alarmScheduler = AlarmSchedulerHelper(application)

    // Current Screen Navigation State
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Assistant Status
    private val _assistantStatus = MutableStateFlow(AssistantStatus.IDLE)
    val assistantStatus: StateFlow<AssistantStatus> = _assistantStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("Safeer is ready. Tap orb or mic to speak.")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Pending Action Requiring User Confirmation / Execution
    private val _pendingAction = MutableStateFlow<AssistantAction?>(null)
    val pendingAction: StateFlow<AssistantAction?> = _pendingAction.asStateFlow()

    // Data Flows from Room
    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workSessions: StateFlow<List<WorkSessionEntity>> = repository.workSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emailDrafts: StateFlow<List<EmailDraftEntity>> = repository.emailDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedWebBookmarks: StateFlow<List<SavedBookmarkEntity>> = repository.getBookmarks("WEB")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedVideoBookmarks: StateFlow<List<SavedBookmarkEntity>> = repository.getBookmarks("YOUTUBE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pomodoro / Work Timer State
    private val _timerSecondsLeft = MutableStateFlow(25 * 60)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow("Work") // "Work" (25m), "Short Break" (5m), "Long Break" (15m)
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private var timerJob: Job? = null

    // Preferences & Settings
    val preferredLanguage = MutableStateFlow("Roman Urdu") // "Urdu", "Roman Urdu", "English"
    val ttsEnabled = MutableStateFlow(true)

    init {
        // Pre-populate sample goals if first time launch
        viewModelScope.launch {
            if (allGoals.value.isEmpty()) {
                val existing = repository.allGoals.first()
                if (existing.isEmpty()) {
                    repository.addGoal(
                        GoalEntity(
                            title = "Learn 20 English Words",
                            description = "Vocab practice with Safeer Assistant",
                            category = "English Practice",
                            priority = "High",
                            estimatedMinutes = 20,
                            isRecurring = true
                        )
                    )
                    repository.addGoal(
                        GoalEntity(
                            title = "Review Weekly Plan",
                            description = "Audit tasks and upcoming deadlines",
                            category = "Work",
                            priority = "Medium",
                            estimatedMinutes = 30
                        )
                    )
                    repository.addReminder(
                        ReminderEntity(
                            title = "Drink Water & Hydrate",
                            notes = "Stay focused and energized",
                            triggerTime = System.currentTimeMillis() + 30 * 60 * 1000,
                            category = "Water"
                        )
                    )
                }
            }
        }
    }

    fun navigateTo(screenId: String) {
        _currentScreen.value = screenId
    }

    // Voice & Chat interaction
    fun startListening() {
        _assistantStatus.value = AssistantStatus.LISTENING
        _statusMessage.value = "Listening... (Sun raha hoon...)"

        val langCode = when (preferredLanguage.value) {
            "Urdu" -> "ur-PK"
            "English" -> "en-US"
            else -> "ur-PK"
        }

        speechManager.startListening(
            languageCode = langCode,
            onResult = { spokenText ->
                _assistantStatus.value = AssistantStatus.THINKING
                _statusMessage.value = "Thinking... (Soch raha hoon...)"
                processUserInput(spokenText)
            },
            onError = { errorMsg ->
                _assistantStatus.value = AssistantStatus.IDLE
                _statusMessage.value = errorMsg
            }
        )
    }

    fun stopListening() {
        speechManager.stopListening()
        if (_assistantStatus.value == AssistantStatus.LISTENING) {
            _assistantStatus.value = AssistantStatus.IDLE
            _statusMessage.value = "Listening paused."
        }
    }

    fun processUserInput(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            // Save user message
            repository.addChatMessage(
                ChatMessageEntity(
                    role = "user",
                    text = prompt,
                    language = preferredLanguage.value
                )
            )

            _assistantStatus.value = AssistantStatus.THINKING
            _statusMessage.value = "Safeer is processing your request..."

            val recent = chatMessages.value.takeLast(6).map { it.role to it.text }
            val parsedResponse: ParsedAssistantResponse = geminiClient.getAssistantResponse(prompt, recent)

            // Save assistant reply
            repository.addChatMessage(
                ChatMessageEntity(
                    role = "assistant",
                    text = parsedResponse.replyText,
                    language = preferredLanguage.value,
                    actionType = parsedResponse.identifiedAction?.let { it::class.simpleName }
                )
            )

            if (ttsEnabled.value) {
                speechManager.speak(parsedResponse.replyText)
            }

            if (parsedResponse.identifiedAction != null) {
                _pendingAction.value = parsedResponse.identifiedAction
                _assistantStatus.value = AssistantStatus.WORKING
                _statusMessage.value = "Action prepared for confirmation."
            } else {
                _assistantStatus.value = AssistantStatus.COMPLETED
                _statusMessage.value = "Response complete."
            }
        }
    }

    fun executePendingAction(action: AssistantAction) {
        val context = getApplication<Application>()
        when (action) {
            is AssistantAction.SendWhatsApp -> {
                PhoneActionExecutor.openWhatsApp(context, action.contactName, action.message)
                _statusMessage.value = "WhatsApp opened. Please review and send."
            }
            is AssistantAction.DraftEmail -> {
                PhoneActionExecutor.openEmailComposer(context, action.recipient, action.subject, action.body)
                viewModelScope.launch {
                    repository.saveEmailDraft(
                        EmailDraftEntity(
                            recipient = action.recipient ?: "",
                            subject = action.subject ?: "",
                            body = action.body
                        )
                    )
                }
                _statusMessage.value = "Email draft opened and saved."
            }
            is AssistantAction.SetAlarm -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, action.hour)
                    set(Calendar.MINUTE, action.minute)
                    set(Calendar.SECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                val reminder = ReminderEntity(
                    title = action.label,
                    notes = "Scheduled for ${action.hour}:${String.format("%02d", action.minute)}",
                    triggerTime = cal.timeInMillis,
                    isAlarm = true,
                    category = "Alarm"
                )
                viewModelScope.launch {
                    val id = repository.addReminder(reminder)
                    alarmScheduler.scheduleReminder(reminder.copy(id = id))
                }
                // Also trigger system clock alarm intent
                PhoneActionExecutor.setSystemAlarm(context, action.hour, action.minute, action.label)
                _statusMessage.value = "Alarm set for ${action.hour}:${String.format("%02d", action.minute)}"
            }
            is AssistantAction.SetReminder -> {
                val trigger = System.currentTimeMillis() + action.delayMinutes * 60 * 1000L
                val reminder = ReminderEntity(
                    title = action.title,
                    notes = "Reminder after ${action.delayMinutes} mins",
                    triggerTime = trigger,
                    category = "Reminder"
                )
                viewModelScope.launch {
                    val id = repository.addReminder(reminder)
                    alarmScheduler.scheduleReminder(reminder.copy(id = id))
                }
                _statusMessage.value = "Reminder scheduled in ${action.delayMinutes} minutes."
            }
            is AssistantAction.CreateGoal -> {
                viewModelScope.launch {
                    repository.addGoal(
                        GoalEntity(
                            title = action.title,
                            category = action.category,
                            priority = "High"
                        )
                    )
                }
                _statusMessage.value = "Goal '${action.title}' created."
            }
            is AssistantAction.SearchYouTube -> {
                PhoneActionExecutor.openYouTube(context, action.query)
                _statusMessage.value = "Searching YouTube for: ${action.query}"
            }
            is AssistantAction.SearchWeb -> {
                PhoneActionExecutor.openWebBrowser(context, action.query)
                _statusMessage.value = "Searching Google Chrome: ${action.query}"
            }
            is AssistantAction.OpenApp -> {
                when (action.appTarget.lowercase()) {
                    "whatsapp" -> PhoneActionExecutor.openWhatsApp(context)
                    "youtube" -> PhoneActionExecutor.openYouTube(context, "")
                    "chrome" -> PhoneActionExecutor.openWebBrowser(context, "https://www.google.com")
                    "gmail" -> PhoneActionExecutor.openEmailComposer(context)
                    "settings" -> PhoneActionExecutor.openPhoneSettings(context)
                }
                _statusMessage.value = "Opening ${action.appTarget}..."
            }
        }
        _pendingAction.value = null
        _assistantStatus.value = AssistantStatus.COMPLETED
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
        _assistantStatus.value = AssistantStatus.IDLE
        _statusMessage.value = "Action dismissed."
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _statusMessage.value = "Conversation history cleared."
        }
    }

    // Goal Management Methods
    fun addGoal(title: String, category: String, priority: String, estMinutes: Int, isRecurring: Boolean) {
        viewModelScope.launch {
            repository.addGoal(
                GoalEntity(
                    title = title,
                    category = category,
                    priority = priority,
                    estimatedMinutes = estMinutes,
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun toggleGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.toggleGoalCompletion(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Reminder Management Methods
    fun addReminder(title: String, notes: String, triggerTime: Long, isAlarm: Boolean, category: String) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = title,
                notes = notes,
                triggerTime = triggerTime,
                isAlarm = isAlarm,
                category = category
            )
            val id = repository.addReminder(reminder)
            alarmScheduler.scheduleReminder(reminder.copy(id = id))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            alarmScheduler.cancelReminder(reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    // Work Timer / Pomodoro Controls
    fun setTimerDuration(mode: String) {
        _timerMode.value = mode
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timerSecondsLeft.value = when (mode) {
            "Short Break" -> 5 * 60
            "Long Break" -> 15 * 60
            else -> 25 * 60
        }
    }

    fun toggleWorkTimer() {
        if (_isTimerRunning.value) {
            pauseWorkTimer()
        } else {
            startWorkTimer()
        }
    }

    private fun startWorkTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSecondsLeft.value > 0) {
                delay(1000)
                _timerSecondsLeft.value -= 1
            }
            _isTimerRunning.value = false
            onTimerCompleted()
        }
    }

    private fun pauseWorkTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetWorkTimer() {
        pauseWorkTimer()
        setTimerDuration(_timerMode.value)
    }

    private fun onTimerCompleted() {
        val completedMode = _timerMode.value
        val minutes = when (completedMode) {
            "Short Break" -> 5
            "Long Break" -> 15
            else -> 25
        }
        viewModelScope.launch {
            repository.recordWorkSession(
                WorkSessionEntity(
                    sessionType = completedMode,
                    durationMinutes = minutes,
                    completed = true
                )
            )
        }
        speechManager.speak("Work timer completed. Time for your next break or goal.")
        _statusMessage.value = "$completedMode session ended!"
    }

    // Bookmark helpers
    fun saveBookmark(title: String, url: String, type: String) {
        viewModelScope.launch {
            repository.addBookmark(SavedBookmarkEntity(title = title, url = url, type = type))
        }
    }

    fun deleteBookmark(bookmark: SavedBookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        timerJob?.cancel()
    }
}
