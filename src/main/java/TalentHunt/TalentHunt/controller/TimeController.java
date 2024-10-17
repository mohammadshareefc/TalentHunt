package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.utils.TimeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class TimeController {
    @GetMapping("/time-ago")
    public String getTimeAgo(@RequestParam("date") String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
        return TimeUtils.formatRelativeTime(dateTime);
    }
}
