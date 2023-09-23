package com.example.springbootwebfluxapirest.controllers;

import com.example.springbootwebfluxapirest.models.documents.Producto;
import com.example.springbootwebfluxapirest.models.services.ProductoService;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> getAll() {
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll()));
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Producto>> getProduct(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<ResponseEntity<Producto>> createProduct(@RequestBody Producto producto) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }
        return service.save(producto).map(p -> ResponseEntity
                .created(URI.create("api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p));
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Producto>> updateProduct(@RequestBody Producto product, @PathVariable String id) {
        return service.findById(id).flatMap(p -> {
                    p.setNombre(product.getNombre());
                    p.setPrecio(product.getPrecio());
                    p.setCategoria(product.getCategoria());
                    return service.save(p);
                })
                .map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(id)))
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return service.findById(id).flatMap(p -> service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
