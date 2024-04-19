package microservice.book.multiplication.service;

import jakarta.transaction.Transactional;
import microservice.book.multiplication.domain.Multiplication;
import microservice.book.multiplication.domain.MultiplicationResultAttempt;
import microservice.book.multiplication.domain.User;
import microservice.book.multiplication.event.EventDispatcher;
import microservice.book.multiplication.event.MultiplicationSolvedEvent;
import microservice.book.multiplication.repository.MultiplicationRepository;
import microservice.book.multiplication.repository.MultiplicationResultAttemptRepository;
import microservice.book.multiplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class MultiplicationServiceImpl implements MultiplicationService {

    private final RandomGeneratorService randomGeneratorService;
    private final MultiplicationResultAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final EventDispatcher eventDispatcher;

    @Autowired
    public MultiplicationServiceImpl(final RandomGeneratorService randomGeneratorService,
                                     final MultiplicationResultAttemptRepository attemptRepository,
                                     final UserRepository userRepository,
                                     final EventDispatcher eventDispatcher, MultiplicationResultAttemptRepository multiplicationResultAttemptRepository) {
        this.randomGeneratorService = randomGeneratorService;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public Multiplication createRandomMultiplication() {
        int factorA = randomGeneratorService.generateRandomFactor();
        int factorB = randomGeneratorService.generateRandomFactor();
        return new Multiplication(factorA, factorB);
    }

    @Transactional
    @Override
    public boolean checkAttempt(final MultiplicationResultAttempt attempt) {

        //Check if the user already exists for that alias
        Optional<User> user = userRepository.findByAlias(attempt.getUser().getAlias());

        // Avoids hack attempts
        Assert.isTrue(!attempt.isCorrect(), "You can't send an attempt marked as correct!!");


        // Check if the attempt is correct
        boolean isCorrect = attempt.getResultAttempt() == attempt.getMultiplication().getFactorA() * attempt.getMultiplication().getFactorB();

        // Create a copy, now setting the correct field accordingly
        MultiplicationResultAttempt checkedAttempt = new MultiplicationResultAttempt(user.orElse(attempt.getUser()), attempt.getMultiplication(), attempt.getResultAttempt(), isCorrect);

        //Store attempt
        MultiplicationResultAttempt save = attemptRepository.save(checkedAttempt);

        System.out.println(save);

        eventDispatcher.send(
                new MultiplicationSolvedEvent(checkedAttempt.getId(),
                        checkedAttempt.getUser().getId(),
                        checkedAttempt.isCorrect())
        );

        eventDispatcher.send(
                new MultiplicationSolvedEvent(checkedAttempt.getId(),
                        checkedAttempt.getUser().getId(),
                        checkedAttempt.isCorrect())
        );

        ///Return result
        return isCorrect;
    }

    @Override
    public List<MultiplicationResultAttempt> getStatsForUser(String userAlias) {
        return attemptRepository.findTop5ByUserAliasOrderByIdDesc(userAlias);
    }

    @Override
    public MultiplicationResultAttempt getResultAttempt(Long attemptId) {
        Optional<MultiplicationResultAttempt> resultAttempt = attemptRepository.findById(attemptId);
        return resultAttempt.orElse(null);
    }
}
