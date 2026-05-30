package com.example.voting.domain.usecase

import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.PollRepository
import com.example.voting.domain.repository.VoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class UseCaseTest {

    @Test
    fun `тест 1 - RegisterUseCase возвращает ошибку при пароле короче 6 символов`() = runTest {
        val authRepository = mock(AuthRepository::class.java)
        val registerUseCase = RegisterUseCase(authRepository)

        val email = "test@mail.ru"
        val shortPassword = "123"

        val result = registerUseCase(email, shortPassword)

        assertTrue(result.isFailure)
        assertEquals("Пароль должен содержать минимум 6 символов", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 2 - RegisterUseCase возвращает ошибку при пустом email`() = runTest {
        val authRepository = mock(AuthRepository::class.java)
        val registerUseCase = RegisterUseCase(authRepository)

        val emptyEmail = ""
        val password = "123456"

        val result = registerUseCase(emptyEmail, password)

        assertTrue(result.isFailure)
        assertEquals("Email и пароль не могут быть пустыми", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 3 - CreatePollUseCase возвращает ошибку при наличии только одного варианта ответа`() = runTest {
        val pollRepository = mock(PollRepository::class.java)
        val createPollUseCase = CreatePollUseCase(pollRepository)
        val title = "Любимый язык"
        val description = "Какой язык вы предпочитаете?"
        val options = listOf("Kotlin")  // только 1 вариант
        val result = createPollUseCase(title, description, options)

        assertTrue(result.isFailure)
        assertEquals("Должно быть минимум 2 варианта ответа", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 4 - CreatePollUseCase возвращает ошибку при пустом названии голосования`() = runTest {
        val pollRepository = mock(PollRepository::class.java)
        val createPollUseCase = CreatePollUseCase(pollRepository)
        val emptyTitle = ""
        val description = "Описание"
        val options = listOf("Вариант 1", "Вариант 2")

        val result = createPollUseCase(emptyTitle, description, options)

        assertTrue(result.isFailure)
        assertEquals("Название голосования не может быть пустым", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 5 - CastVoteUseCase возвращает ошибку при неверном ID голосования`() = runTest {
        val voteRepository = mock(VoteRepository::class.java)
        val authRepository = mock(AuthRepository::class.java)
        val castVoteUseCase = CastVoteUseCase(voteRepository, authRepository)

        val invalidPollId = 0

        val result = castVoteUseCase(invalidPollId, 1)

        assertTrue(result.isFailure)
        assertEquals("Неверный ID голосования", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 6 - CastVoteUseCase возвращает ошибку при неверном ID варианта ответа`() = runTest {
        val voteRepository = mock(VoteRepository::class.java)
        val authRepository = mock(AuthRepository::class.java)
        val castVoteUseCase = CastVoteUseCase(voteRepository, authRepository)

        val pollId = 1
        val invalidOptionId = 0

        val result = castVoteUseCase(pollId, invalidOptionId)

        assertTrue(result.isFailure)
        assertEquals("Неверный ID варианта ответа", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 7 - GetResultsUseCase возвращает ошибку при неверном ID голосования`() = runTest {
        val voteRepository = mock(VoteRepository::class.java)
        val getResultsUseCase = GetResultsUseCase(voteRepository)

        val invalidPollId = -1

        val result = getResultsUseCase(invalidPollId)

        assertTrue(result.isFailure)
        assertEquals("Неверный ID голосования", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 8 - GetResultsUseCase успешно возвращает результаты при корректном ID`() = runTest {
        val voteRepository = mock(VoteRepository::class.java)
        val getResultsUseCase = GetResultsUseCase(voteRepository)
        val expectedResult = Result.success(
            com.example.voting.domain.model.PollResult(
                pollId = 1,
                title = "Тест",
                totalVotes = 10,
                options = emptyList()
            )
        )
        `when`(voteRepository.getResults(1)).thenReturn(expectedResult)

        val result = getResultsUseCase(1)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `тест 9 - DeletePollUseCase возвращает ошибку при неверном ID голосования`() = runTest {
        val pollRepository = mock(PollRepository::class.java)
        val deletePollUseCase = DeletePollUseCase(pollRepository)

        val invalidPollId = 0

        val result = deletePollUseCase(invalidPollId)

        assertTrue(result.isFailure)
        assertEquals("Неверный ID голосования", result.exceptionOrNull()?.message)
    }

    @Test
    fun `тест 10 - LoginUseCase возвращает ошибку при пустом email`() = runTest {
        val authRepository = mock(AuthRepository::class.java)
        val loginUseCase = LoginUseCase(authRepository)
        val emptyEmail = ""
        val password = "123456"
        val result = loginUseCase(emptyEmail, password)
        assertTrue(result.isFailure)
        assertEquals("Email и пароль не могут быть пустыми", result.exceptionOrNull()?.message)
    }
}