package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

object AsadCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "اسد"
    
    override fun calculate(pool: WalnutUnit): List<ResultItem> {
        val baseShare = pool / 4.5
        val halfShare = baseShare / 2.0
        
        return listOf(
            ResultItem("سهم هر پسر", baseShare),
            ResultItem("سهم هر دختر", halfShare)
        )
    }
}
