package com.example;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestPath;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("fruits")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@PermitAll
public class FruitResource {

    @Inject
    FruitDao fruitDao;

    @GET
    public Multi<Fruit> get() {
        return fruitDao.get();
    }

    @GET
    @Path("{id}")
    public Uni<Fruit> getSingle(
            @RestPath
            final Integer id) {
        return fruitDao.getSingle(id);
    }

    @POST
    public Uni<Response> create(final Fruit fruit) {
        if (fruit == null || fruit.getId() != null) {
            throw new IllegalArgumentException("Id was invalidly set on request.");
        }

        return fruitDao.create(fruit) //
                .onItem().transform(created -> Response.ok(created).status(201).build());
    }

    @PUT
    public Uni<Response> update(final Fruit fruit) {
        if (fruit == null || fruit.getName() == null) {
            throw new IllegalArgumentException("Fruit name was invalidly set on request.");
        }

        return fruitDao.update(fruit) //
                .onItem().transform(updated -> Response.ok(updated).status(200).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(
            @RestPath
            final Integer id) {
        return fruitDao.delete(id) //
                .onItem().transform(deleted -> Response.status(204).build());
    }
}