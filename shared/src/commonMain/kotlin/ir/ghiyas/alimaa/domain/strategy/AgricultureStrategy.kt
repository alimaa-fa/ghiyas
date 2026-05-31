package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit

object AgricultureStrategy {

    // کپسوله کردن ورودی‌های استیت (مستقل از UI)
    data class Input(
        val remainingFromStage2: WalnutUnit, // همان RemainingWalnutsForStage3 در پرامپت شما
        val isKeshavarzi: Boolean,
        val keshavarziRatioInput: String,
        val isNimehkari: Boolean
    )

    // خروجی‌های دقیق طبق پرامپت که به لایه UI و ماژول ۴ می‌روند
    data class Output(
        val keshavarziTotal: WalnutUnit,
        val tempRemainingAfterK: WalnutUnit,
        val nimehkariTotal: WalnutUnit,
        val remainingForStage4: WalnutUnit
    )

    fun calculate(input: Input): Output {
        // A. محاسبه سهم کشاورزی
        val keshavarziTotal = if (input.isKeshavarzi) {
            val ratio = input.keshavarziRatioInput.toDoubleOrNull() ?: 0.0
            if (ratio > 0) input.remainingFromStage2 / ratio else WalnutUnit.ZERO
        } else {
            WalnutUnit.ZERO
        }

        // محاسبه باقی‌مانده موقت پس از کسر سهم کشاورز
        val tempRemainingAfterK = input.remainingFromStage2 - keshavarziTotal

        // B. محاسبه سهم نیمه‌کاری (همیشه روی باقی‌مانده پس از کشاورز اعمال می‌شود)
        val nimehkariTotal = if (input.isNimehkari) {
            tempRemainingAfterK / 2.0
        } else {
            WalnutUnit.ZERO
        }

        // C. محاسبه موجودی نهایی استخر برای انتقال به ماژول ۴ (موتور تسهیم)
        val remainingForStage4 = tempRemainingAfterK - nimehkariTotal

        return Output(
            keshavarziTotal = keshavarziTotal,
            tempRemainingAfterK = tempRemainingAfterK,
            nimehkariTotal = nimehkariTotal,
            remainingForStage4 = remainingForStage4
        )
    }
}
