package ir.ghiyas.alimaa.data

import ir.ghiyas.alimaa.domain.models.ComprehensiveMode
import ir.ghiyas.alimaa.domain.models.SavedDistributionTemplate
import ir.ghiyas.alimaa.domain.models.ShareholderNode
import kotlinx.browser.window
import kotlin.js.json

object DistributionTemplateRepository {
    private const val STORAGE_KEY = "ghiyas_dist_templates_v1"

    fun getAllTemplates(): List<SavedDistributionTemplate> {
        val jsonString = window.localStorage.getItem(STORAGE_KEY)
        if (jsonString.isNullOrBlank()) return emptyList()
        return try {
            val jsArray = js("JSON.parse(jsonString)") as Array<dynamic>
            jsArray.map { jsToTemplate(it) }.sortedByDescending { (it.createdAt as? Number)?.toLong() ?: 0L }
        } catch (e: Exception) {
            console.error("خطا در بازیابی الگوهای تسهیم:", e)
            emptyList()
        }
    }

    // متد ذخیره طوری اصلاح شد که اگر آیدی موجود بود، همان را آپدیت کند
    fun saveTemplate(template: SavedDistributionTemplate) {
        val templates = getAllTemplates().toMutableList()
        val existingIndex = templates.indexOfFirst { it.id == template.id }
        if (existingIndex >= 0) {
            templates[existingIndex] = template 
        } else {
            // اگر آیدی نبود، شاید نام تکراری باشد
            val existingTitleIndex = templates.indexOfFirst { it.title == template.title }
            if (existingTitleIndex >= 0) {
                templates[existingTitleIndex] = template.copy(id = templates[existingTitleIndex].id)
            } else {
                templates.add(0, template)
            }
        }
        saveAll(templates)
    }

    fun deleteTemplate(id: String) {
        val templates = getAllTemplates().filter { it.id != id }
        saveAll(templates)
    }

    private fun saveAll(templates: List<SavedDistributionTemplate>) {
        val jsArray = templates.map { templateToJs(it) }.toTypedArray()
        window.localStorage.setItem(STORAGE_KEY, js("JSON.stringify(jsArray)") as String)
    }

    private fun nodeToJs(node: ShareholderNode): dynamic {
        val childrenJs = node.children.map { nodeToJs(it) }.toTypedArray()
        return json(
            "id" to node.id,
            "name" to node.name,
            "isFemale" to node.isFemale,
            "rawValue" to node.rawValue,
            "transferredToId" to node.transferredToId,
            "hasSubDistribution" to node.hasSubDistribution,
            "subDistributionMode" to node.subDistributionMode.name,
            "children" to childrenJs,
            "canBeExcluded" to node.canBeExcluded,
            "isExcluded" to node.isExcluded,
            "canBeTransferred" to node.canBeTransferred // ذخیره این فیلد جا مانده بود!
        )
    }

    private fun jsToNode(jsObj: dynamic): ShareholderNode {
        val childrenArray = if (jsObj.children != undefined && jsObj.children != null) jsObj.children as Array<dynamic> else emptyArray<dynamic>()
        val childrenList = childrenArray.map { jsToNode(it) }
        
        val modeStr = if (jsObj.subDistributionMode != undefined && jsObj.subDistributionMode != null) jsObj.subDistributionMode as String else ComprehensiveMode.PERSON.name
        val mode = ComprehensiveMode.entries.find { it.name == modeStr } ?: ComprehensiveMode.PERSON

        return ShareholderNode(
            id = if (jsObj.id != undefined && jsObj.id != null) jsObj.id as String else "",
            name = if (jsObj.name != undefined && jsObj.name != null) jsObj.name as String else "",
            isFemale = if (jsObj.isFemale != undefined && jsObj.isFemale != null) jsObj.isFemale as Boolean else false,
            rawValue = if (jsObj.rawValue != undefined && jsObj.rawValue != null) jsObj.rawValue as String else "1",
            transferredToId = if (jsObj.transferredToId != undefined && jsObj.transferredToId != null) jsObj.transferredToId as String else "",
            hasSubDistribution = if (jsObj.hasSubDistribution != undefined && jsObj.hasSubDistribution != null) jsObj.hasSubDistribution as Boolean else false,
            subDistributionMode = mode,
            children = childrenList,
            canBeExcluded = if (jsObj.canBeExcluded != undefined && jsObj.canBeExcluded != null) jsObj.canBeExcluded as Boolean else false,
            isExcluded = if (jsObj.isExcluded != undefined && jsObj.isExcluded != null) jsObj.isExcluded as Boolean else false,
            canBeTransferred = if (jsObj.canBeTransferred != undefined && jsObj.canBeTransferred != null) jsObj.canBeTransferred as Boolean else false // بازیابی این فیلد جا مانده بود!
        )
    }

    private fun templateToJs(template: SavedDistributionTemplate): dynamic {
        val nodesJs = template.nodes.map { nodeToJs(it) }.toTypedArray()
        return json(
            "id" to template.id,
            "title" to template.title,
            "rootMode" to template.rootMode.name,
            "totalCountLimit" to template.totalCountLimit,
            "nodes" to nodesJs,
            "createdAt" to template.createdAt.toDouble()
        )
    }

    private fun jsToTemplate(jsObj: dynamic): SavedDistributionTemplate {
        val nodesArray = if (jsObj.nodes != undefined && jsObj.nodes != null) jsObj.nodes as Array<dynamic> else emptyArray<dynamic>()
        val nodesList = nodesArray.map { jsToNode(it) }
        
        val rootModeStr = if (jsObj.rootMode != undefined && jsObj.rootMode != null) jsObj.rootMode as String else ComprehensiveMode.PERSON.name
        val rootMode = ComprehensiveMode.entries.find { it.name == rootModeStr } ?: ComprehensiveMode.PERSON

        return SavedDistributionTemplate(
            id = if (jsObj.id != undefined && jsObj.id != null) jsObj.id as String else "",
            title = if (jsObj.title != undefined && jsObj.title != null) jsObj.title as String else "",
            rootMode = rootMode,
            totalCountLimit = if (jsObj.totalCountLimit != undefined && jsObj.totalCountLimit != null) jsObj.totalCountLimit as String else "",
            nodes = nodesList,
            createdAt = if (jsObj.createdAt != undefined && jsObj.createdAt != null) (jsObj.createdAt as Number).toLong() else 0L
        )
    }
}
