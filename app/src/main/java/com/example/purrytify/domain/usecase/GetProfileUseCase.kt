package com.example.purrytify.domain.usecase

import com.example.purrytify.domain.model.User
import com.example.purrytify.domain.repository.UserRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(): User {
        return userRepository.getUserProfile()
    }
}