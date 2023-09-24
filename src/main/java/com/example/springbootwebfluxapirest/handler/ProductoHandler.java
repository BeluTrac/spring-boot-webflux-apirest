package com.example.springbootwebfluxapirest.handler;

import com.example.springbootwebfluxapirest.models.documents.Categoria;
import com.example.springbootwebfluxapirest.models.documents.Producto;
import com.example.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductoHandler {

    @Value("${config.uploads.path}")
    private String pathname;

    @Autowired
    private ProductoService service;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.multipartData()
                .map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> service.findById(id)
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID() + "-" + replaceInvalidCharacters(file.filename()));
                            return file.transferTo(new File(pathname + p.getFoto()))
                                    .then(service.save(p))
                                    .flatMap(product -> ServerResponse.ok().body(fromValue(product)))
                                    .switchIfEmpty(ServerResponse.notFound().build());
                        }));
    }

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
            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return service.save(p)
                        .flatMap(pdb -> ServerResponse.created(URI.create("/api/v2/productos" + pdb.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(pdb)));
            }
        });
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

    public Mono<ServerResponse> createWithPhoto(ServerRequest request) {
        Mono<Producto> producto = request.multipartData().map(multipart -> {
            FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(),Double.parseDouble(precio.value()), categoria);
        });

        return request.multipartData()
                .map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> producto
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID() + "-" + replaceInvalidCharacters(file.filename()));
                            return file.transferTo(new File(pathname + p.getFoto()))
                                    .then(service.save(p))
                                    .flatMap(product -> ServerResponse.ok().body(fromValue(product)));
                        }));
    }

    private String replaceInvalidCharacters(String string) {
        return string.replaceAll("[:\\\\ ]", "");
    }

}
