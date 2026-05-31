package ir.ghiyas.alimaa.presentation.stages.distribution

import ir.ghiyas.alimaa.domain.strategy.DistributionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ۱. مدل‌های اختصاصی برای لایه رابط کاربری (مستقل از Domain)
data class ShareholderInput(
    val name: String = "",
    val ghiyasInput: String = ""
)

// استیتِ تنظیماتِ مربوط به «یک» استخر گردو
data class PoolDistributionState(
    val mode: DistributionMode = DistributionMode.MODE_B_SIMPLE,
    val groupName: String = "",                           // برای حالت فله‌ای (A)
    val peopleCountInput: String = "",                    // برای حالت نفری (B)
    val isBoyGirlSplit: Boolean = false,                  // برای حالت نفری (B)
    val shareholders: List<ShareholderInput> = listOf(ShareholderInput()), // برای حالت قیاس (C) - پیش‌فرض یک ردیف
    val defaultStrategyTitle: String = ""                 // برای حالت محاسبات سازنده
)

// استیت کلِ صفحه ماژول ۴ (شامل هر ۳ استخر احتمالی)
data class DistributionStageState(
    val mainPoolState: PoolDistributionState = PoolDistributionState(),
    val partner1PoolState: PoolDistributionState = PoolDistributionState(),
    val partner2PoolState: PoolDistributionState = PoolDistributionState()
)

// نوع استخر برای اینکه بدانیم کدام تنظیمات در حال آپدیت است
enum class PoolTarget { MAIN, PARTNER_1, PARTNER_2 }

class DistributionStageViewModel {
    private val _state = MutableStateFlow(DistributionStageState())
    val state: StateFlow<DistributionStageState> = _state.asStateFlow()

    fun clearForm() {
        _state.value = DistributionStageState()
    }

    // --- تابع مرکزی برای آپدیت کردن یک استخر خاص ---
    private fun updatePoolState(target: PoolTarget, update: (PoolDistributionState) -> PoolDistributionState) {
        _state.update { currentState ->
            when (target) {
                PoolTarget.MAIN -> currentState.copy(mainPoolState = update(currentState.mainPoolState))
                PoolTarget.PARTNER_1 -> currentState.copy(partner1PoolState = update(currentState.partner1PoolState))
                PoolTarget.PARTNER_2 -> currentState.copy(partner2PoolState = update(currentState.partner2PoolState))
            }
        }
    }

    // --- توابع عمومی تنظیمات استخر ---
    fun updateMode(target: PoolTarget, mode: DistributionMode) {
        updatePoolState(target) { it.copy(mode = mode) }
    }

    fun updateGroupName(target: PoolTarget, name: String) {
        updatePoolState(target) { it.copy(groupName = name) }
    }

    fun updatePeopleCount(target: PoolTarget, countInput: String) {
        updatePoolState(target) { it.copy(peopleCountInput = countInput) }
    }

    fun updateIsBoyGirlSplit(target: PoolTarget, isSplit: Boolean) {
        updatePoolState(target) { it.copy(isBoyGirlSplit = isSplit) }
    }

    fun updateDefaultStrategy(target: PoolTarget, title: String) {
        updatePoolState(target) { it.copy(defaultStrategyTitle = title) }
    }

    // --- توابع اختصاصی برای مدیریت لیست پویا در حالت قیاس (Mode C) ---
    fun addShareholder(target: PoolTarget) {
        updatePoolState(target) { state ->
            state.copy(shareholders = state.shareholders + ShareholderInput())
        }
    }

    fun updateShareholder(target: PoolTarget, index: Int, name: String, ghiyasInput: String) {
        updatePoolState(target) { state ->
            val newList = state.shareholders.toMutableList()
            if (index in newList.indices) {
                newList[index] = ShareholderInput(name, ghiyasInput)
            }
            state.copy(shareholders = newList)
        }
    }

    fun removeShareholder(target: PoolTarget, index: Int) {
        updatePoolState(target) { state ->
            val newList = state.shareholders.toMutableList()
            if (index in newList.indices && newList.size > 1) { // حداقل یک ردیف باید بماند
                newList.removeAt(index)
            }
            state.copy(shareholders = newList)
        }
    }
}
