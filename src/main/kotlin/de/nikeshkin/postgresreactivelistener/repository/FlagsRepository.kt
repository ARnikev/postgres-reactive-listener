package de.nikeshkin.postgresreactivelistener.repository

import de.nikeshkin.postgresreactivelistener.domain.Flag
import org.springframework.data.repository.CrudRepository

interface FlagsRepository : CrudRepository<Flag,  String>
