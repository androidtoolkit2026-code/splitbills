package com.splitmate.domain.usecase.settlement

import com.splitmate.domain.model.Settlement
import com.splitmate.domain.repository.SettlementRepository
import javax.inject.Inject

class SettleUpUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(settlement: Settlement): Result<Unit> {
        return try {
            require(settlement.amount > 0) { "Settlement amount must be positive" }
            require(settlement.payerId != settlement.payeeId) { "Cannot settle with yourself" }

            settlementRepository.createSettlement(settlement)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
