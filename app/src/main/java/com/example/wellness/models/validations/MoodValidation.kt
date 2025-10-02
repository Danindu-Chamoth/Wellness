package com.example.wellness.models.validations

class MoodValidation(
    private var emoji: String,
    private var note: String
) {
    fun validateEmoji(): ValidationResult {
        return if (emoji.isEmpty()) {
            ValidationResult.Empty("Please select a mood")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateNote(): ValidationResult {
        return if (note.length > 500) {
            ValidationResult.Invalid("Note should be less than 500 characters")
        } else {
            ValidationResult.Valid
        }
    }
}
