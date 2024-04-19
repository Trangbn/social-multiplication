package microservice.book.multiplication.service;

import microservice.book.multiplication.domain.Multiplication;
import microservice.book.multiplication.domain.MultiplicationResultAttempt;

import java.util.List;

public interface MultiplicationService {

    Multiplication createRandomMultiplication();

    boolean checkAttempt(final MultiplicationResultAttempt resultAttempt);
    List<MultiplicationResultAttempt> getStatsForUser(String userAlias);
    MultiplicationResultAttempt getResultAttempt(final Long attemptId);
}
