package org.example.controller;

import org.example.domain.MovieInfo;
import org.example.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(MovieInfoController.class)
@AutoConfigureWebTestClient
class MovieInfoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    @Test
    void getAll() {
        // given
        List<MovieInfo> movieInfoList = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        when(movieInfoService.getAll()).thenReturn(Flux.fromIterable(movieInfoList));
        // when, then
        webTestClient.get()
                .uri("/v1/movie-info")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    List<MovieInfo> actual = exchangeResult.getResponseBody();
                    assertThat(actual).hasSize(3);
                    actual.sort((a, b) -> a.getYear() - b.getYear());
                    assertThat(actual.get(0))
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                    assertThat(actual.get(1))
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18"));
                    assertThat(actual.get(2))
                            .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                });
    }

    @Test
    void findById() {
        // given
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(movieInfoService.findById("abc")).thenReturn(Mono.just(movieInfo));
        // when, then
        webTestClient.get()
                .uri("/v1/movie-info/abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    MovieInfo actual = exchangeResult.getResponseBody();
                    assertThat(actual)
                            .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                });
    }

    @Test
    void addMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        when(movieInfoService.create(any(MovieInfo.class))).thenReturn(Mono.just(movieInfo));
        // when, then
        webTestClient.post()
                .uri("/v1/movie-info")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(exchangeResult -> {
                    MovieInfo movieInfo1 = exchangeResult.getResponseBody();
                    assertThat(movieInfo1)
                            .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                            .containsExactly("Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                });
    }

    @Test
    void updateMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(movieInfoService.update(any(MovieInfo.class), anyString())).thenReturn(Mono.just(movieInfo));
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
        when(movieInfoService.deleteById("abc")).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
                .uri("/v1/movie-info/abc")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);
    }
}
