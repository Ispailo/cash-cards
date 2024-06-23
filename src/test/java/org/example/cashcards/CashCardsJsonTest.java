package org.example.cashcards;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

@JsonTest
public class CashCardsJsonTest {

    @Autowired
    private JacksonTester<CashCard> cashCardJacksonTester;

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(1L, 100.00, "ispailo");
        Assertions.assertThat(cashCardJacksonTester.read("single.json")).isEqualTo(cashCard);
        Assertions.assertThat(cashCardJacksonTester.write(cashCard)).isStrictlyEqualToJson("single.json");
        Assertions.assertThat(cashCardJacksonTester.write(cashCard)).hasJsonPathNumberValue("@.id");
        Assertions.assertThat(cashCardJacksonTester.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);
        Assertions.assertThat(cashCardJacksonTester.write(cashCard)).hasJsonPathNumberValue("@.amount");
        Assertions.assertThat(cashCardJacksonTester.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(100.0);
    }

    @Test
    void cashCardDeserializationTest() throws IOException {
        String object = "{\n" +
                "  \"id\": 1,\n" +
                "  \"amount\": 100.00,\n" +
                "  \"owner\": \"ispailo\"\n" +
                "}";
        Assertions.assertThat(cashCardJacksonTester.parse(object)).isEqualTo(new CashCard(1L, 100.00, "ispailo"));
        Assertions.assertThat(cashCardJacksonTester.parseObject(object).id()).isEqualTo(1L);
        Assertions.assertThat(cashCardJacksonTester.parseObject(object).amount()).isEqualTo(100.00);

    }
}
