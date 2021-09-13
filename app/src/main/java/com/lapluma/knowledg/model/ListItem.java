package com.lapluma.knowledg.model;

import java.io.Serializable;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListItem implements Serializable {
    private String label;
    private String category;
    private String uri;
    private boolean hasread;

    public boolean getHasread() {
        return hasread;
    }
}
