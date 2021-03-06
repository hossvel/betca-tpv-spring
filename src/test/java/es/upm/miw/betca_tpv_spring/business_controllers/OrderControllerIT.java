package es.upm.miw.betca_tpv_spring.business_controllers;

import es.upm.miw.betca_tpv_spring.TestConfig;
import es.upm.miw.betca_tpv_spring.documents.Order;
import es.upm.miw.betca_tpv_spring.documents.OrderLine;
import es.upm.miw.betca_tpv_spring.dtos.*;
import es.upm.miw.betca_tpv_spring.repositories.ArticleRepository;
import es.upm.miw.betca_tpv_spring.repositories.OrderRepository;
import es.upm.miw.betca_tpv_spring.repositories.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestConfig
public class OrderControllerIT {

    @Autowired
    private OrderController orderController;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private OrderDto orderDto;

    @BeforeEach
    void seed() {
        OrderLineDto[] orderLines = {
                new OrderLineDto(this.articleRepository.findAll().get(0).getCode(), 10),
                new OrderLineDto(this.articleRepository.findAll().get(1).getCode(), 8),
                new OrderLineDto(this.articleRepository.findAll().get(2).getCode(), 6),
                new OrderLineDto(this.articleRepository.findAll().get(3).getCode(), 4),
        };

        this.orderDto = new OrderDto("order0", this.providerRepository.findAll().get(0).getId(), LocalDateTime.now(), orderLines);
        this.orderRepository.save(new Order("orderA", this.providerRepository.findAll().get(0),
                new OrderLine[]{
                        new OrderLine(this.articleRepository.findAll().get(0), 5),
                        new OrderLine(this.articleRepository.findAll().get(1), 7),
                }));
    }

    @Test
    void testSearchOrderByDescriptionOrProvider() {
        OrderSearchDto orderSearchDto =
                new OrderSearchDto("null", this.providerRepository.findAll().get(1).getId(), "null");
        StepVerifier
                .create(this.orderController.searchOrder(orderSearchDto))
                .expectNextCount(1)
                .thenCancel()
                .verify();
    }

    @Test
    void testCreateOrder() {
        OrderLineCreationDto[] orderLines = {
                new OrderLineCreationDto(this.articleRepository.findAll().get(0).getCode(), 10),
                new OrderLineCreationDto(this.articleRepository.findAll().get(1).getCode(), 8),
                new OrderLineCreationDto(this.articleRepository.findAll().get(2).getCode(), 6),
                new OrderLineCreationDto(this.articleRepository.findAll().get(3).getCode(), 4),
        };
        OrderCreationDto orderCreationDto = new OrderCreationDto("orderPrueba", this.providerRepository.findAll().get(1).getId(), orderLines);

        StepVerifier
                .create(this.orderController.createOrder(orderCreationDto))
                .expectNextMatches(order -> {
                    assertEquals("orderPrueba", order.getDescription());
                    assertEquals(this.providerRepository.findAll().get(1).getId(), order.getProvider());
                    assertNotNull(order.getOpeningDate());
                    assertEquals(4, order.getOrderLines().length);
                    return true;
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testUpdateOrder() {
        String id = this.orderRepository.findAll().get(1).getId();
        OrderLineDto[] orderLines = {
                new OrderLineDto(this.articleRepository.findAll().get(1).getCode(), 8),
                new OrderLineDto(this.articleRepository.findAll().get(2).getCode(), 6),
        };
        StepVerifier
                .create(this.orderController
                        .updateOrder(id, new OrderDto("cambiado", this.providerRepository.findAll().get(1).getId(), LocalDateTime.now(), orderLines)))
                .expectNextMatches(orderDtoData -> {
                    assertEquals(this.orderRepository.findById(id).get().getId(), orderDtoData.getId());
                    assertEquals(this.orderRepository.findById(id).get().getDescription(), orderDtoData.getDescription());
                    assertEquals(this.orderRepository.findById(id).get().getOrderLines().length, orderDtoData.getOrderLines().length);
                    return true;
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testGetOrder() {
        String id = this.orderRepository.findAll().get(0).getId();
        StepVerifier
                .create(this.orderController.getOrder(id))
                .expectNextMatches(orderDto1 -> {
                    assertEquals(id, orderDto1.getId());
                    assertEquals("order1", orderDto1.getDescription());
                    assertEquals(4, orderDto1.getOrderLines().length);
                    assertEquals(this.orderRepository.findById(id).get().getOpeningDate(), orderDto1.getOpeningDate());
                    assertNotNull(orderDto1.getOpeningDate());
                    assertNull(orderDto1.getClosingDate());
                    return true;
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testDeleteOrder() {
        assertEquals(2, this.orderRepository.count());
        String id = this.orderRepository.findAll().get(1).getId();
        StepVerifier
                .create(this.orderController.deleteOrder(id))
                .expectComplete()
                .verify();
        assertEquals(1, this.orderRepository.count());
    }
}
