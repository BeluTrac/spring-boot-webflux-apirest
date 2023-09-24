package com.example.springbootwebfluxapirest.handler;

import com.example.springbootwebfluxapirest.models.documents.Producto;
import com.example.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService service;

    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findById(id).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Producto> monoProducto = request.bodyToMono(Producto.class);
        return monoProducto.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return service.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos" + p.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)));
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        Mono<Producto> monoProducto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return service.findById(id).zipWith(monoProducto, (pDB, pReq) -> {
                    pDB.setNombre(pReq.getNombre());
                    pDB.setPrecio(pReq.getPrecio());
                    pDB.setCategoria(pReq.getCategoria());
                    return pDB;
                }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findById(id)
                .flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
