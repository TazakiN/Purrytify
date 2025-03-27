package com.example.purrytify.domain.usecase

import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponseDTO> {
        return userRepository.login(email, password)
    }
}