package es.upm.miw.betca_tpv_spring.repositories;

import es.upm.miw.betca_tpv_spring.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@TestConfig
class BudgetReactRepositoryIT {

    @Autowired
    private BudgetReactRepository budgetReactRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testFindAllAndDatabaseSeeder() {
        StepVerifier
                .create(this.budgetReactRepository.findAll())
                .expectNextMatches(budget -> {
                    assertNotNull(budget.getId());
                    assertNotNull(budget.getCreationDate());
                    assertNotNull(budget.getShoppingList());
                    assertEquals(0, new BigDecimal("61.7").compareTo(budget.getBudgetTotal()));
                    assertFalse(budget.toString().matches("@"));
                    return true;
                })
                .thenCancel()
                .verify();
    }

}
