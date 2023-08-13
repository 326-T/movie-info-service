package org.example.service;

import org.example.domain.MovieInfo;
import org.example.repository.MovieInfoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class MovieInfoServiceTest {

    @InjectMocks
    private MovieInfoService movieInfoService;

    @Mock
    private MovieInfoRepository movieInfoRepository;

    @Test
    void getAll() {
        // given
        List<MovieInfo> movieInfoList = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        when(movieInfoRepository.findAll()).thenReturn(Flux.fromIterable(movieInfoList));
        // when
        Flux<MovieInfo> movieInfoFlux = movieInfoService.getAll().log();
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
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(movieInfoRepository.findById("abc")).thenReturn(Mono.just(movieInfo));
        // when
        Mono<MovieInfo> movieInfoMono = movieInfoService.findById("abc").log();
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 ->
                        assertThat(movieInfo1)
                                .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                .verifyComplete();
    }

    @Test
    void create() {
        // given
        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        when(movieInfoRepository.save(any(MovieInfo.class))).thenReturn(Mono.just(movieInfo));
        // when
        Mono<MovieInfo> movieInfoMono = movieInfoService.create(movieInfo);
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 ->
                        assertThat(movieInfo1)
                                .extracting(MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("Batman Begins1", 2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .verifyComplete();
    }

    @Test
    void update() {
        // given
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        when(movieInfoRepository.save(any(MovieInfo.class))).thenReturn(Mono.just(movieInfo));
        // when
        Mono<MovieInfo> movieInfoMono = movieInfoService.update(movieInfo, "abc");
        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 ->
                        assertThat(movieInfo1)
                                .extracting(MovieInfo::getMovieInfoId, MovieInfo::getName, MovieInfo::getYear, MovieInfo::getCast, MovieInfo::getRelease_date)
                                .containsExactly("abc", "Dark Knight Rises", 2021, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        // given
        when(movieInfoRepository.deleteById("abc")).thenReturn(Mono.empty());
        // when
        movieInfoService.deleteById("abc");
        // then
        verify(movieInfoRepository).deleteById("abc");
    }
}
