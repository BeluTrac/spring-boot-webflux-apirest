package com.example.springbootwebfluxapirest.models.dao;

import com.example.springbootwebfluxapirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
    Mono<Categoria> findCategoriaByNombre(String nombre);
}
