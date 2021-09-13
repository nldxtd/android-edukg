package com.lapluma.knowledg.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class RelatedInstance implements Serializable {
    private int id;
    private String label;
    private String rel;  // subject or object
    private String des;  // description
}
