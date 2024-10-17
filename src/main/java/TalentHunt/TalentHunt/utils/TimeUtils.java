package TalentHunt.TalentHunt.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    public static String formatRelativeTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        if (dateTime == null) {
            return "Unknown time"; // or any default value you prefer
        }

        long days = duration.toDays();
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        long hours = duration.toHours();
        if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long seconds = duration.getSeconds();
        return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
    }

    public String truncateToWords(String text, int wordLimit) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] words = text.split("\\s+");
        if (words.length <= wordLimit) {
            return text;
        }
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < wordLimit; i++) {
            truncated.append(words[i]).append(" ");
        }
        return truncated.toString().trim() + "..."; // Add ellipsis for truncated text
    }
}
