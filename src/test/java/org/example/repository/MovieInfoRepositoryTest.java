package org.example.repository;

import org.example.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        // given

        // when
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();
        // then
        // 動作を確認するなら
        StepVerifier.create(movieInfoFlux).expectNextCount(3).verifyComplete();
        // 中身まで確認するなら
        StepVerifier.create(movieInfoFlux.sort((a, b) -> a.getYear() - b.getYear()))
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")))
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                .verifyComplete();
    }

    @Test
    void findById() {
        // given

        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo).log();
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 ->
                        assertThat(movieInfo1)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        // given
        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);
        // when
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo).log();
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 ->
                        assertThat(movieInfo1)
                                .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        // given
        // when
        movieInfoRepository.deleteById("abc").block();
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();
        // then
        // 動作を確認するなら
        StepVerifier.create(movieInfoFlux).expectNextCount(2).verifyComplete();
        // 中身まで確認するなら
        StepVerifier.create(movieInfoFlux.sort((a, b) -> a.getYear() - b.getYear()))
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .assertNext(movieInfo ->
                        assertThat(movieInfo)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")))
                .verifyComplete();
    }
}