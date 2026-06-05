package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartClearedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long cartId;
    private String userId;
    private Integer itemCount;
    private LocalDateTime clearedAt;
}
