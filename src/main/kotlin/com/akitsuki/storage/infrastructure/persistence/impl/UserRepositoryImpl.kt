package com.akitsuki.storage.infrastructure.persistence.impl

import com.akitsuki.storage.domain.user.entity.User
import com.akitsuki.storage.domain.user.repository.UserRepository
import com.akitsuki.storage.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    
    override fun save(user: User): User {
        return jpaRepository.save(user)
    }
    
    override fun findById(id: UUID): User? {
        return jpaRepository.findById(id).orElse(null)
    }
    
    override fun findByUsername(username: String): User? {
        return jpaRepository.findByUsername(username)
    }
    
    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)
    }
    
    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByUsername(username)
    }
    
    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }
}