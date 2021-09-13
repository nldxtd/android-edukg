package com.lapluma.knowledg.model;

import java.io.Serializable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question implements Serializable {
    private String body;
    private String a;
    private String b;
    private String c;
    private String d;
    private String answer;
}
