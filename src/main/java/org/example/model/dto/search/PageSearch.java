package org.example.model.dto.search;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSearch implements Comparable<PageSearch> {

    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;


    @Override
    public int compareTo(PageSearch o) {
        return Float.compare(o.relevance, this.relevance);
    }
}
