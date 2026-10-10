package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class AssistantRepository(private val db: AppDatabase) {

    // Goals
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val pendingGoals: Flow<List<GoalEntity>> = db.goalDao().getPendingGoals()
    val completedGoals: Flow<List<GoalEntity>> = db.goalDao().getCompletedGoals()

    suspend fun getGoalById(id: Long): GoalEntity? = db.goalDao().getGoalById(id)
    suspend fun addGoal(goal: GoalEntity): Long = db.goalDao().insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = db.goalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = db.goalDao().deleteGoal(goal)
    suspend fun toggleGoalCompletion(goal: GoalEntity) {
        val nextState = !goal.isCompleted
        db.goalDao().updateCompletion(
            id = goal.id,
            completed = nextState,
            completedAt = if (nextState) System.currentTimeMillis() else null
        )
    }

    // Reminders
    val allReminders: Flow<List<ReminderEntity>> = db.reminderDao().getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = db.reminderDao().getActiveReminders()

    suspend fun getReminderById(id: Long): ReminderEntity? = db.reminderDao().getReminderById(id)
    suspend fun addReminder(reminder: ReminderEntity): Long = db.reminderDao().insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = db.reminderDao().updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = db.reminderDao().deleteReminder(reminder)
    suspend fun completeReminder(id: Long) = db.reminderDao().markCompleted(id)

    // Chat
    val chatMessages: Flow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()
    suspend fun addChatMessage(message: ChatMessageEntity): Long = db.chatDao().insertMessage(message)
    suspend fun clearChatHistory() = db.chatDao().clearHistory()

    // Work Sessions
    val workSessions: Flow<List<WorkSessionEntity>> = db.workSessionDao().getAllSessions()
    fun getTodayWorkMinutes(startOfDayTimestamp: Long): Flow<Int?> =
        db.workSessionDao().getTotalWorkMinutesSince(startOfDayTimestamp)
    suspend fun recordWorkSession(session: WorkSessionEntity): Long =
        db.workSessionDao().insertSession(session)

    // Email Drafts
    val emailDrafts: Flow<List<EmailDraftEntity>> = db.emailDraftDao().getAllDrafts()
    suspend fun saveEmailDraft(draft: EmailDraftEntity): Long = db.emailDraftDao().insertDraft(draft)
    suspend fun deleteEmailDraft(draft: EmailDraftEntity) = db.emailDraftDao().deleteDraft(draft)

    // Bookmarks
    fun getBookmarks(type: String): Flow<List<SavedBookmarkEntity>> =
        db.savedBookmarkDao().getBookmarksByType(type)
    suspend fun addBookmark(bookmark: SavedBookmarkEntity): Long =
        db.savedBookmarkDao().insertBookmark(bookmark)
    suspend fun deleteBookmark(bookmark: SavedBookmarkEntity) =
        db.savedBookmarkDao().deleteBookmark(bookmark)
}
