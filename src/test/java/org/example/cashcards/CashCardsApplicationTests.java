package org.example.cashcards;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardsApplicationTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnACashCardJsonWhenIdIsPassed(){
		ResponseEntity<String> stringResponseEntity = testRestTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/99", String.class);
		Assertions.assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(stringResponseEntity.getBody());
		Number id = documentContext.read("$.id");
		Assertions.assertThat(id).isEqualTo(99);
		Double amount = documentContext.read("$.amount");
		Assertions.assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardThatHeIsNotTheOwner(){
		ResponseEntity<String> stringResponseEntity = testRestTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/102", String.class);
		Assertions.assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		Assertions.assertThat(stringResponseEntity.getBody()).isBlank();
	}

	@Test
	void shouldReturnACashCardObjectWhenIdIsPassed(){
		ResponseEntity<CashCard> cashCardResponseEntity = testRestTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards/99", CashCard.class);
		Assertions.assertThat(cashCardResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(cashCardResponseEntity.getBody()).isEqualTo(new CashCard(99L, 123.45, "sarah1"));

	}

	@Test
	@DirtiesContext
	void shouldReturnACashCardJsonAfterTheSaveIsSuccess(){
		CashCard cashCard = new CashCard(null, 200.00,null);
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("sarah1","abc123")
				.postForEntity("/cashcards", cashCard, String.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		Double amount = documentContext.read("$.amount");
		Assertions.assertThat(amount).isEqualTo(200.00);
	}

	@Test
	void shouldReturnAllTheListOfCashCardJson(){
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("sarah1","abc123")
				.getForEntity("/cashcards", String.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		int cashCardsSize = documentContext.read("$.length()");
		Assertions.assertThat(cashCardsSize).isEqualTo(4);
		JSONArray cashCardsId = documentContext.read("$..id");
		Assertions.assertThat(cashCardsId).isEqualTo(List.of(99, 100, 101, 102));
	}

	@Test
	void shouldReturnTheListOfCashCardByPageAndSorting(){
		ResponseEntity<String> responseEntity = testRestTemplate
				.withBasicAuth("sarah1","abc123").getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		JSONArray cashCardsSize = documentContext.read("$[*]");
		Assertions.assertThat(cashCardsSize.size()).isEqualTo(1);
		Double amount = documentContext.read("$[0].amount");
		Assertions.assertThat(amount).isEqualTo(200.00);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingProductPUT(){
		CashCard cashCard = new CashCard(null, 1000.00, null);
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99", HttpMethod.PUT, new HttpEntity<>(cashCard), Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Double amount = documentContext.read("$.amount");
		Assertions.assertThat(amount).isEqualTo(1000.00);
	}

	@Test
	void shouldNotUpdateACashCardDoesNotExist(){
		CashCard cashCard = new CashCard(null, 500.00, null);
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/9999", HttpMethod.PUT, new HttpEntity<>(cashCard), Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowedUpdatingACashCardThatDontOwn(){
		CashCard cashCard = new CashCard(null, 500.00, null);
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, new HttpEntity<>(cashCard), Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard(){
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardDoesNotExist(){
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/9999", HttpMethod.DELETE, null, Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shoulNotdAllowedDeletingACashCardThatNotOwn(){
		ResponseEntity<Void> responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		responseEntity = testRestTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/101", HttpMethod.DELETE, null, Void.class);
		Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

	}

}