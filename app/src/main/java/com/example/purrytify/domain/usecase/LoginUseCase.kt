package com.example.purrytify.domain.usecase

import com.example.purrytify.data.model.LoginResponseDTO
import com.example.purrytify.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponseDTO> {
        return authRepository.login(email, password)
    }
}