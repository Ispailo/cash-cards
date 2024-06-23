package org.example.cashcards;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardsRepository cashCardsRepository;

    public CashCardController(CashCardsRepository cashCardsRepository) {
        this.cashCardsRepository = cashCardsRepository;
    }

    /*@GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal){
        Optional<CashCard> optionalCashCard = cashCardsRepository.findByIdAndOwner(requestedId, principal.getName());
        return optionalCashCard.map(cashCard -> ResponseEntity.ok(optionalCashCard.get())).orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal){
        CashCard cashCard = cashCardsRepository.findByIdAndOwner(requestedId, principal.getName());
        return cashCard != null ?  ResponseEntity.ok(cashCard): ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<CashCard> save(@RequestBody CashCard cashCard, Principal principal){
        CashCard newCashCard = new CashCard(null, cashCard.amount(), principal.getName());
        CashCard saved = cashCardsRepository.save(newCashCard);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable){
        Page<CashCard> cashCards = cashCardsRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );
        return ResponseEntity.ok(cashCards.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> updateCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCard, Principal principal){
        CashCard cashCardRetrieved = cashCardsRepository.findByIdAndOwner(requestedId, principal.getName());
        if(cashCardRetrieved != null){
            CashCard updatedCashCard = new CashCard(cashCardRetrieved.id(), cashCard.amount(), principal.getName());
            cashCardsRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal){
        if (cashCardsRepository.existsByIdAndOwner(requestedId, principal.getName())){
            cashCardsRepository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
