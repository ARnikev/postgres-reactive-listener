package de.nikeshkin.postgresreactivelistener.controller

import de.nikeshkin.postgresreactivelistener.domain.Flag
import de.nikeshkin.postgresreactivelistener.repository.FlagsRepository
import de.nikeshkin.postgresreactivelistener.service.FlagsService
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
@RequestMapping("/flags")
class FlagsController(
    private val flagsRepository: FlagsRepository,
    private val flagsService: FlagsService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addFlag(@RequestBody flag: Flag) = flagsService.addFlag(flag)

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    fun updateFlag(@RequestBody flag: Flag) = flagsService.updateFlag(flag)

    @GetMapping
    fun getFlags(): MutableIterable<Flag> = flagsRepository.findAll()

    @GetMapping("/{id}")
    fun getFlag(@PathVariable id: String): Flag = flagsRepository.findById(id).get()

}
