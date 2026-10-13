package no.nav.veilarbveileder.controller

import no.nav.veilarbveileder.controller.dto.OverblikkVisningRequest
import no.nav.veilarbveileder.controller.dto.OverblikkVisningResponse
import no.nav.veilarbveileder.service.AuthService
import no.nav.veilarbveileder.service.OverblikkVisningService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/overblikkvisning", produces = [MediaType.APPLICATION_JSON_VALUE])
class OverblikkVisningController(
    private val overblikkVisningService: OverblikkVisningService,
    private val authService: AuthService
) {

    @GetMapping
    fun hent(): OverblikkVisningResponse {
        authService.sjekkTilgangTilModia()
        return OverblikkVisningResponse(overblikkVisningService.hent(authService.innloggetVeilederIdent))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun lagre(@RequestBody request: OverblikkVisningRequest) {
        authService.sjekkTilgangTilModia()
        try {
            overblikkVisningService.lagre(authService.innloggetVeilederIdent, request.overblikkVisning)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun slett() {
        authService.sjekkTilgangTilModia()
        overblikkVisningService.slett(authService.innloggetVeilederIdent)
    }
}
