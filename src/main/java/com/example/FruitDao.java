package com.example;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.reactive.mutiny.Mutiny;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class FruitDao {

    @Inject
    Mutiny.Session session;

    public Multi<Fruit> get() {
        return session.createNamedQuery("Fruits.findAll", Fruit.class).getResults();
    }

    public Uni<Fruit> getSingle(final Integer id) {
        return session.find(Fruit.class, id);
    }

    public Uni<Fruit> create(final Fruit fruit) {
        if (fruit == null || fruit.getId() != null) {
            return Uni.createFrom().failure(new IllegalArgumentException("Fruit name was invalidly set on request."));
        }

        return session.persist(fruit).chain(session::flush) //
                .onItem().transform(ignore -> fruit);
    }

    public Uni<Fruit> update(final Fruit fruit) {
        if (fruit == null || fruit.getName() == null) {
            throw new IllegalArgumentException("Fruit name was invalidly set on request.");
        }

        final Function<Fruit, Uni<? extends Fruit>> update = entity -> {
            entity.setName(fruit.getName());
            return session.flush().onItem().transform(ignore -> entity);
        };

        return session.find(Fruit.class, fruit.getId())
                // If entity doesn't exist then
                .onItem().ifNull().fail() //
                .onItem().invoke(() -> System.out.println("Random message"))
                .onItem().ifNotNull().transformToUni(update);
    }

    public Uni<Void> delete(final Integer id) {
        return session.find(Fruit.class, id)
                // If entity doesn't exist then
                .onItem().ifNull().fail() //
                .onItem().ifNotNull().transformToUni(entity -> session.remove(entity).chain(session::flush));
    }
}
