package application.services;

import application.model.dto.interfaces.IndexPageId;
import application.model.dto.interfaces.ModelId;
import application.model.dto.interfaces.PageRelevanceAndData;
import application.model.dto.search.PageSearch;
import application.response.SearchResponse;
import application.utility.JsoupData;
import application.utility.Lemmatizator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SearchService {

    private final Lemmatizator lemmatizer;
    @Autowired
    LemmaService lemmaService;
    @Autowired
    IndexService indexService;
    @Autowired
    PageService pageService;
    @Autowired
    SiteService siteService;

    public SearchService() throws IOException {
        this.lemmatizer = new Lemmatizator();
    }

    public Object search(String findQuery, String siteUrl, int offset, int limit) {
        Set<String> findQueryLemmas = lemmatizer.getLemmaSet(findQuery);

        Set<ModelId> allLemmasIds = new HashSet<>();
        Set<IndexPageId> allPageIds = new HashSet<>();

        if (siteUrl == null) {
            siteService.findAllSites().forEach(site -> {
                List<ModelId> lemmasIdsOfSite = lemmaService.findLemmasIdBySiteOrderByFrequency(findQueryLemmas, site);
                allLemmasIds.addAll(lemmasIdsOfSite);
                allPageIds.addAll(getPageIdsOfSite(lemmasIdsOfSite));
            });
        } else {
            List<ModelId> lemmasIdsOfSite = lemmaService.findLemmasIdBySiteOrderByFrequency(findQueryLemmas,
                    siteService.findSiteByName(siteUrl));
            allLemmasIds.addAll(lemmasIdsOfSite);
            allPageIds.addAll(getPageIdsOfSite(lemmasIdsOfSite));
        }

        List<PageRelevanceAndData> pageData = indexService.findPageRelevanceAndData(allPageIds, allLemmasIds,
                limit, offset);

        return new SearchResponse(allPageIds.size(), createSearchResult(pageData, findQuery));
    }

    private List<IndexPageId> getPageIdsOfSite(List<ModelId> lemmasIdsOfSite) {
        List<IndexPageId> pageIdsOfSite = new ArrayList<>();
        if (!lemmasIdsOfSite.isEmpty()) {
            pageIdsOfSite = indexService.findPagesIds(lemmasIdsOfSite.get(0).getId());
            if (lemmasIdsOfSite.size() > 2) {
                for (int lemma = 1; lemma < lemmasIdsOfSite.size() - 1; lemma++) {
                    pageIdsOfSite = indexService.getPagesIdOfNextLemmas(lemmasIdsOfSite.get(lemma).getId(),
                            pageIdsOfSite);
                }
                return pageIdsOfSite;
            }
        }
        return pageIdsOfSite;
    }

    private ArrayList<PageSearch> createSearchResult(List<PageRelevanceAndData> pageData, String findQuery) {
        ArrayList<PageSearch> searchResult = new ArrayList<>();

        pageData.forEach(pageRelevanceAndData -> {
            PageSearch searchDto = new PageSearch();
            searchDto.setSite(pageRelevanceAndData.getSite());
            searchDto.setSiteName(pageRelevanceAndData.getSiteName());
            searchDto.setUri(pageRelevanceAndData.getUri());
            searchDto.setTitle(JsoupData.getTitle(pageRelevanceAndData.getContent()));
            searchDto.setSnippet(JsoupData.getSnippetInHtml(pageRelevanceAndData.getContent(), findQuery));
            searchDto.setRelevance(pageRelevanceAndData.getRelevance());

            searchResult.add(searchDto);
        });

        return searchResult;
    }
}
