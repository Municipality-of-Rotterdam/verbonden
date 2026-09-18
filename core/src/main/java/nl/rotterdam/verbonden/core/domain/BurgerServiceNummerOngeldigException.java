package nl.rotterdam.verbonden.core.domain;

public class BurgerServiceNummerOngeldigException extends RuntimeException {

    public BurgerServiceNummerOngeldigException(String message) {
        super(message);
    }
}
