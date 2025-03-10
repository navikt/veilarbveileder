package no.nav.veilarbveileder.mock

import no.nav.poao_tilgang.api.dto.response.TilgangsattributterResponse
import no.nav.poao_tilgang.client.*
import no.nav.poao_tilgang.client.api.ApiResult
import java.util.*

class PoaoTilgangClientMock : PoaoTilgangClient {
    override fun erSkjermetPerson(norskeIdenter: List<NorskIdent>): ApiResult<Map<NorskIdent, Boolean>> {
        TODO("Not yet implemented")
    }

    override fun erSkjermetPerson(norskIdent: NorskIdent): ApiResult<Boolean> {
        TODO("Not yet implemented")
    }

    override fun evaluatePolicies(requests: List<PolicyRequest>): ApiResult<List<PolicyResult>> {
        TODO("Not yet implemented")
    }

    override fun evaluatePolicy(input: PolicyInput): ApiResult<Decision> {
        return ApiResult.success(Decision.Permit)
    }

    override fun hentAdGrupper(navAnsattAzureId: UUID): ApiResult<List<AdGruppe>> {
        TODO("Not yet implemented")
    }

    override fun hentTilgangsAttributter(norskIdent: NorskIdent): ApiResult<TilgangsattributterResponse> {
        TODO("Not yet implemented")
    }
}