package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.data.mcp.FewShotExample
import ru.yamost.first.agent.featute.chat.data.mcp.InputSchema
import ru.yamost.first.agent.featute.chat.data.mcp.ReturnParameters

class GigaToolDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("parameters")
    val parameters: InputSchema,
    @SerializedName("fewShotExamples")
    val fewShotExamples: List<FewShotExample>? = null,
    @SerializedName("returnParameters")
    val returnParameters: ReturnParameters? = null
)