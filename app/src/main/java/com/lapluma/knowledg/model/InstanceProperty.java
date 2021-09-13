package com.lapluma.knowledg.model;

import java.io.Serializable;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InstanceProperty implements Serializable {
    private String label;
    private String object;
}
