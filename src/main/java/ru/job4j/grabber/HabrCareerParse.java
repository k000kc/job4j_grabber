package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGE_NUMBER = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGE_NUMBER; i++) {
            String fullLink = "%s%s%d%s".formatted(link, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink).timeout(120000);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            for (Element row : rows) {
                posts.add(createPost(row, link));
            }
        }
        return posts;
    }

    private Post createPost(Element row, String link) throws IOException {
        Element dateElement = row.select(".vacancy-card__date").first();
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String date = dateElement.child(0).attr("datetime");
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
        String vacancyDesc = retrieveDescription(vacancyLink);
        return new Post(vacancyName, vacancyLink, vacancyDesc, dateTimeParser.parse(date));
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element description = document.select(".vacancy-description__text").first();
        return description.text();
    }

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        Parse careerParse = new HabrCareerParse(dateTimeParser);
        List<Post> posts = careerParse.list(SOURCE_LINK);
        for (Post post : posts) {
            System.out.println(post);
        }
    }
}
