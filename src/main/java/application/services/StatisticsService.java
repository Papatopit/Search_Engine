package application.services;

import application.model.dto.statistics.DetailedStatistics;
import application.model.dto.statistics.Statistics;
import application.model.dto.statistics.TotalStatistics;
import application.repositories.LemmaRepository;
import application.repositories.PageRepository;
import application.repositories.SiteRepository;
import application.response.StatisticResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private LemmaRepository lemmaRepository;


    public StatisticResponse getStatistics() {
        TotalStatistics totalStatisticsDto = new TotalStatistics(siteRepository.count(), pageRepository.count(),
                lemmaRepository.count(), true);

        List<DetailedStatistics> detailedStatisticsDtoList = new ArrayList<>();
        siteRepository.findAll().forEach(site -> {
            DetailedStatistics detailedStatisticsDto = new DetailedStatistics(site.getUrl(), site.getName(),
                    site.getStatus(), site.getStatusTime(), site.getLastError(),
                    pageRepository.countBySiteBySiteId(site), lemmaRepository.countBySiteBySiteId(site));
            detailedStatisticsDtoList.add(detailedStatisticsDto);
        });

        return new StatisticResponse(new Statistics(totalStatisticsDto, detailedStatisticsDtoList));
    }

}
