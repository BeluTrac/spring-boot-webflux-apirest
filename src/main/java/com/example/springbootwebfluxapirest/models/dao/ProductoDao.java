package com.example.springbootwebfluxapirest.models.dao;

import com.example.springbootwebfluxapirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
    Mono<Producto> findByNombre(String nombre);
    @Query("{ 'nombre': ?0}")
    Mono<Producto> obtenerPorNombre(String nombre);
}
