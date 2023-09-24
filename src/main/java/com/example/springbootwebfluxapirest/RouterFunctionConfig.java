package com.example.springbootwebfluxapirest;

import com.example.springbootwebfluxapirest.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

//Sirve para definir las rutas
@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::getAll)
                .andRoute(GET("/api/v2/productos/{id}"), handler::getById)
                .andRoute(POST("/api/v2/productos"), handler::create)
                .andRoute(PUT("/api/v2/productos/{id}"), handler::edit)
                .andRoute(DELETE("/api/v2/productos/{id}"), handler::delete)
                .andRoute(POST("/api/v2/productos/upload/{id}"),handler::upload)
                .andRoute(POST("/api/v2/productosWithPhoto"), handler::createWithPhoto);
    }
}
