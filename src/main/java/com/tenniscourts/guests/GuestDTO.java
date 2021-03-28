package com.tenniscourts.guests;

import lombok.*;

import javax.validation.constraints.NotNull;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class GuestDTO {

    private Long Id;

    @NotNull
    private String name;
}
