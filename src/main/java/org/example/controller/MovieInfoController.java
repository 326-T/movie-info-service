package org.example.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.MovieInfo;
import org.example.service.MovieInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MovieInfoController {

    @Autowired
    private MovieInfoService movieInfoService;

    @GetMapping("/movie-info")
    public Flux<MovieInfo> getAll() {
        return movieInfoService.getAll();
    }

    @GetMapping("/movie-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> findById(@PathVariable("id") String id) {

        return movieInfoService.findById(id)
                .map(movieInfo1 -> ResponseEntity.ok()
                        .body(movieInfo1))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PostMapping("/movie-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.create(movieInfo);
    }

    @PutMapping("/movie-info/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo movieInfo, @PathVariable String id) {

        Mono<MovieInfo> updatedMovieInfoMono =  movieInfoService.update(movieInfo, id);
        return updatedMovieInfoMono
                .map(movieInfo1 -> ResponseEntity.ok()
                        .body(movieInfo1))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/movie-info/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteById(@PathVariable String id){
        return movieInfoService.deleteById(id);
    }
}
