package backend.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class   Board {
    String player1;
    String player2;
    String squares;
    Turn[] turns;


}
