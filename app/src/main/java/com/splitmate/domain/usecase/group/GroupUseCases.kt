package com.splitmate.domain.usecase.group

import com.splitmate.domain.model.Group
import com.splitmate.domain.repository.GroupRepository
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(group: Group): Result<Unit> {
        return try {
            require(group.name.isNotBlank()) { "Group name cannot be empty" }
            groupRepository.createGroup(group)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
