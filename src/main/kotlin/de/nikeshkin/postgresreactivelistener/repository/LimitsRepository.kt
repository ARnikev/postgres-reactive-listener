package de.nikeshkin.postgresreactivelistener.repository

import de.nikeshkin.postgresreactivelistener.domain.Limits
import org.springframework.data.repository.CrudRepository

interface LimitsRepository : CrudRepository<Limits, String>
