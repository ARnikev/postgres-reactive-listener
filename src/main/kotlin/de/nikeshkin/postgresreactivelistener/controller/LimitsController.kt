package de.nikeshkin.postgresreactivelistener.controller

import de.nikeshkin.postgresreactivelistener.domain.Limits
import de.nikeshkin.postgresreactivelistener.repository.LimitsRepository
import de.nikeshkin.postgresreactivelistener.service.LimitsService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/limits")
class LimitsController(
    private val limitsRepository: LimitsRepository,
    private val limitsService: LimitsService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addLimit(@RequestBody limit: Limits) = limitsService.addLimit(limit)

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    fun updateLimit(@RequestBody limit: Limits) = limitsService.updateLimit(limit)

    @GetMapping
    fun getLimits(): MutableIterable<Limits> = limitsRepository.findAll()

    @GetMapping("/{id}")
    fun getLimit(@PathVariable id: String): Limits = limitsRepository.findById(id).get()
}
