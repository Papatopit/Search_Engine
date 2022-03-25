package application.response;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import application.model.dto.statistics.Statistics;

@Data
@RequiredArgsConstructor
public class StatisticResponse {

    private String result = "true";

    private Statistics statistics;

    public StatisticResponse(Statistics statisticsDto) {
        this.statistics = statisticsDto;
    }
}
