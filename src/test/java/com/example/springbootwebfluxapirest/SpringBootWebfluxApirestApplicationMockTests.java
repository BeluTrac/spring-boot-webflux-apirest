package com.example.springbootwebfluxapirest;

import com.example.springbootwebfluxapirest.models.documents.Categoria;
import com.example.springbootwebfluxapirest.models.documents.Producto;
import com.example.springbootwebfluxapirest.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationMockTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService service;

	@Value("${config.base.endpoint}")
	public String url;

	@Test
	void listarTest() {
		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
                    assert productos != null;
                    Assertions.assertFalse(productos.isEmpty());
				});
	}

	@Test
	void getByIdTest() {
		String productName = "TV Panasonic Pantalla LCD";
		Producto producto = service.findByNombre(productName).block();
		client.get()
				.uri(url +"/{id}", Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo(productName)
				.jsonPath("$.precio").isNotEmpty();
	}

	@Test
	void createTest() {
		String productName = "TV Panasonic Pantalla LCD";
		Categoria categoria = service.findCategoriaByNombre("Electrónico").block();
		Producto producto = new Producto("Un producto", 12.0, categoria);
		client.post()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class)
				.consumeWith(response -> {
					Producto p =response.getResponseBody();
					Assertions.assertEquals(p.getNombre(), producto.getNombre());
					Assertions.assertEquals(p.getPrecio(), producto.getPrecio());
                    Assertions.assertEquals(p.getCategoria().getNombre(), categoria.getNombre());
				});
	}

	@Test
	void eliminarTest() {

		String productName = "Apple iPod";
		Producto producto = service.findByNombre(productName).block();

		client.delete()
				.uri(url +"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody().isEmpty();

		client.get()
				.uri(url +"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody().isEmpty();
	}
}
