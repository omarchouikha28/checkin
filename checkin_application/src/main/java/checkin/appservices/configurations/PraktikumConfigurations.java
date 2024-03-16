package checkin.appservices.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class PraktikumConfigurations {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int maxHolidays;

    public PraktikumConfigurations(@Value("${config.startDate}") String startDate,
                                   @Value("${config.endDate}") String endDate,
                                   @Value("${config.startTime}") String startTime,
                                   @Value("${config.endTime}") String endTime,
                                   @Value("${config.maxHolidays}") String maxHolidays) {
        this.startTime = LocalTime.parse(startTime);
        this.endTime = LocalTime.parse(endTime);
        this.startDate = LocalDate.parse(startDate);
        this.endDate = LocalDate.parse(endDate);
        this.maxHolidays = Integer.parseInt(maxHolidays);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getMaxHolidays() {
        return maxHolidays;
    }
}
