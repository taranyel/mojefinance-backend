package cvut.fel.sit.mojefinance.product.domain.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;import com.fasterxml.jackson.databind.annotation.JsonSerialize;import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Amount amount;
    private TransactionDirection direction;
    private TransactionStatus status;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate valueDate;

    private String counterpartyName;
    private RelatedParties relatedParties;
    private String category;
}
