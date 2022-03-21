package org.example.services.parseurl;

import org.example.config.SitesConfig;
import org.example.model.Page;
import org.example.model.Site;
import org.example.model.StatusSite;
import org.example.response.ResultResponse;
import org.example.services.IndexService;
import org.example.services.LemmaService;
import org.example.services.PageService;
import org.example.services.SiteService;
import org.example.utility.JsoupData;
import org.example.utility.Lemmatizator;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Service
public class UrlParserService {

    @Autowired
    private SitesConfig sitesConfig;
    @Autowired
    private IndexService indexService;
    @Autowired
    private LemmaService lemmaService;
    @Autowired
    private PageService pageService;
    @Autowired
    private SiteService siteService;

    public Object startIndexing() {
        ArrayList<Site> sitesConfigSites = sitesConfig.getSites();
        siteService.setIndexingStarted(true);
        siteService.setIndexingStopFlag(false);

        indexService.deleteAllIndexData();
        pageService.deleteAllPageData();
        lemmaService.deleteAllLemmaData();
        siteService.deleteAllSiteData();

        for (Site site : sitesConfigSites) {
            CompletableFuture.runAsync(() -> {
                try {
                    asyncStartIndexing(site);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, ForkJoinPool.commonPool());
        }
        return new ResultResponse();
    }

    @Async
    void asyncStartIndexing(Site siteFromConfig) throws IOException {
        siteFromConfig.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteFromConfig.setStatus(StatusSite.INDEXING);
        Site dbSite = siteService.saveSiteIfNotExist(siteFromConfig);
        dbSite.setStatus(StatusSite.INDEXING);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<String> parsedURLs = Collections.synchronizedSet(new HashSet<>());
        parsedURLs.add(dbSite.getUrl().toLowerCase(Locale.ROOT) + "/");
        UrlParser urlParser = new UrlParser(dbSite.getUrl().toLowerCase(Locale.ROOT) + "/", dbSite, parsedURLs);
        urlParser.setIndexService(indexService);
        urlParser.setLemmaService(lemmaService);
        urlParser.setPageService(pageService);
        urlParser.setSiteService(siteService);
        urlParser.setLemmatizator(new Lemmatizator());
        forkJoinPool.invoke(urlParser);
        if (siteService.isIndexingStopFlag()) {
            siteService.updateStatus(dbSite, StatusSite.FAILED);
            siteService.updateErrorMessage(dbSite, "Indexing Stopped");
        } else {
            siteService.updateStatus(dbSite, StatusSite.INDEXED);
        }
        siteService.setIndexingStarted(false);
    }

    public Object stopIndexing() {
        siteService.setIndexingStarted(false);
        siteService.setIndexingStopFlag(true);
        return new ResultResponse();
    }

    public Object indexOnePage(String url, Site siteFromConfig) throws IOException, SQLException, InterruptedException {

        siteFromConfig.setStatusTime(new Timestamp(System.currentTimeMillis()));
        siteFromConfig.setStatus(StatusSite.INDEXING);
        Site dbSite = siteService.saveSiteIfNotExist(siteFromConfig);

        Optional<Page> pageOptional = pageService.getPageByPath(url.substring(dbSite.getUrl().length()), dbSite);
        if (pageOptional.isPresent()) {
            lemmaService.unCountLemmasOfPage(pageOptional.get().getId());
            pageService.deletePage(pageOptional.get());
        }

        Set<String> parsedURLs = Collections.synchronizedSet(new HashSet<>());
        parsedURLs.add(dbSite.getUrl().toLowerCase(Locale.ROOT));
        UrlParser urlParser = new UrlParser(url.toLowerCase(Locale.ROOT), dbSite, parsedURLs);
        urlParser.setIndexService(indexService);
        urlParser.setLemmaService(lemmaService);
        urlParser.setPageService(pageService);
        urlParser.setSiteService(siteService);
        urlParser.setLemmatizator(new Lemmatizator());
        Connection connection = JsoupData.createConnection(url);
        urlParser.insertData(connection);
        siteService.updateStatus(dbSite, StatusSite.INDEXING);
        return new ResultResponse();
    }
}
