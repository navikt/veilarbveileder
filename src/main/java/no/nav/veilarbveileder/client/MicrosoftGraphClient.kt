package no.nav.veilarbveileder.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.ws.rs.core.HttpHeaders
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.json.JsonUtils
import no.nav.common.rest.client.RestClient
import no.nav.common.rest.client.RestUtils
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.CONSISTENCY_LEVEL_HEADER_KEY
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.CONSISTENCY_LEVEL_HEADER_VALUE_EVENTUAL
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.COUNT_PARAM_KEY
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.COUNT_PARAM_VALUE_TRUE
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.SELECT_PARAM_VALUE_DISPLAY_NAME
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.FILTER_PARAM_KEY
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.SELECT_PARAM_VALUE_ID
import no.nav.veilarbveileder.client.MicrosoftGraphClient.Companion.SELECT_PARAM_KEY
import no.nav.veilarbveileder.config.EnvironmentProperties
import no.nav.veilarbveileder.utils.deserializeJsonOrThrow
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.springframework.stereotype.Component
import java.net.URI

interface MicrosoftGraphClient {
    fun hentAdGrupper(filter: AdGruppeFilter = ENHET_FILTER_VALUE): Set<AdGruppe>

    companion object {
        const val SELECT_PARAM_KEY = "\$select"
        const val FILTER_PARAM_KEY = "\$filter"
        const val COUNT_PARAM_KEY = "\$count"
        const val COUNT_PARAM_VALUE_TRUE = "true"
        const val SELECT_PARAM_VALUE_ID = "id"
        const val SELECT_PARAM_VALUE_DISPLAY_NAME = "displayName"
        const val ENHET_FILTER_VALUE = "startswith(displayName,'0000-GA-ENHET')"
        const val CONSISTENCY_LEVEL_HEADER_KEY = "ConsistencyLevel"
        const val CONSISTENCY_LEVEL_HEADER_VALUE_EVENTUAL = "eventual"
    }

}
typealias AdGruppeFilter = String

data class AdGruppe(val id: String, val displayName: String) {
    companion object {
        internal fun fraMicrosoftGraphResponseValue(value: MicrosoftGraphResponse.Value): AdGruppe {
            return AdGruppe(value.id, value.displayName)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
internal data class MicrosoftGraphResponse(
    @JsonProperty("@odata.context")
    val context: URI,
    @JsonProperty("@odata.count")
    val count: Int,
    @JsonProperty("@odata.nextLink")
    val nextLink: URI? = null,
    val value: List<Value>
) {
    internal data class Value(
        @JsonProperty("@odata.type")
        val type: String,
        val id: String,
        val displayName: String,
    )
}

@Component
class MicrosoftGraphClientImpl(
    private val environmentProperties: EnvironmentProperties,
    private val azureAdOnBehalfOfTokenClient: AzureAdOnBehalfOfTokenClient,
    private val authContextHolder: AuthContextHolder
) : MicrosoftGraphClient {
    override fun hentAdGrupper(filter: AdGruppeFilter): Set<AdGruppe> {
        val oboToken = azureAdOnBehalfOfTokenClient.exchangeOnBehalfOfToken(
            environmentProperties.microsoftGraphScope,
            authContextHolder.requireIdTokenString()
        )
        val url = "${environmentProperties.microsoftGraphUri}/v1.0/me/memberOf"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter(COUNT_PARAM_KEY, COUNT_PARAM_VALUE_TRUE)
            .addQueryParameter(SELECT_PARAM_KEY, "$SELECT_PARAM_VALUE_ID,$SELECT_PARAM_VALUE_DISPLAY_NAME")
            .addQueryParameter(FILTER_PARAM_KEY, filter)
            .build()

        val request = Request.Builder()
            .url(url)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $oboToken")
            .header(CONSISTENCY_LEVEL_HEADER_KEY, CONSISTENCY_LEVEL_HEADER_VALUE_EVENTUAL)
            .get()
            .build()

        RestClient.baseClient().newCall(request).execute().use { response ->
            RestUtils.throwIfNotSuccessful(response)

            return response.deserializeJsonOrThrow<MicrosoftGraphResponse>()
                .value
                .map(AdGruppe::fraMicrosoftGraphResponseValue)
                .toSet()
        }
    }
}
