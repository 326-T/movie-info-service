package org.example.it;

import org.example.domain.MovieInfo;
import org.example.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MovieInfoIT {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfoList = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfoList).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getAll() {
        // given
        // when, then
        webTestClient.get()
                .uri("/v1/movie-info")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    List<MovieInfo> movieInfoList = exchangeResult.getResponseBody();
                    assertThat(movieInfoList).hasSize(3);
                    movieInfoList.sort((a, b) -> a.getYear() - b.getYear());
                    assertThat(movieInfoList.get(0))
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                    assertThat(movieInfoList.get(1))
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));
                    assertThat(movieInfoList.get(2))
                            .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                });
    }

    @Test
    void findById() {
        // given
        // when, then
        webTestClient.get()
                .uri("/v1/movie-info/abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    MovieInfo movieInfo = exchangeResult.getResponseBody();
                    assertThat(movieInfo)
                            .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                });
    }

    @Test
    void addMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        // when, then
        webTestClient.post()
                .uri("/v1/movie-info")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    MovieInfo movieInfo1 = exchangeResult.getResponseBody();
                    assertThat(movieInfo1.getMovieInfoId()).isNotBlank();
                    assertThat(movieInfo1)
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                });
    }

    @Test
    void updateMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        // when, then
        webTestClient.put()
                .uri("/v1/movie-info/abc")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    MovieInfo movieInfo1 = exchangeResult.getResponseBody();
                    assertThat(movieInfo1)
                            .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                });
    }

    @Test
    void deleteById() {
        // given
        // when, then
        webTestClient.delete()
                .uri("/v1/movie-info/abc")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);
    }
}
