package org.example.service;

import org.example.domain.MovieInfo;
import org.example.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    public Flux<MovieInfo> getAll() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> findById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> create(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public  Mono<MovieInfo> update(MovieInfo movieInfo, String id) {
        movieInfo.setMovieInfoId(id);
        return movieInfoRepository.save(movieInfo);
    }

    public Mono<Void> deleteById(String id) {
        return movieInfoRepository.deleteById(id);
    }
}
