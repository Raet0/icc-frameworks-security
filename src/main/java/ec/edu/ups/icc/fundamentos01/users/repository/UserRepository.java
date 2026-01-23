package ec.edu.ups.icc.fundamentos01.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<ProductEntity> findByName(String name);
    // ============== MÉTODOS EXISTENTES ==============

    Optional<UserEntity> findById(Long id);

    // Verificar si email ya está registrado (usado en registro)
    boolean existsByEmail(String email);

}