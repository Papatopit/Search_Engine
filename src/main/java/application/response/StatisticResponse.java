package application.response;


import application.model.dto.statistics.Statistics;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StatisticResponse {

    private String result = "true";

    private Statistics statistics;

    public StatisticResponse(Statistics statisticsDto) {
        this.statistics = statisticsDto;
    }
}
