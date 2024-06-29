package dev.optimistic.decentdiscordbridge.ducks;

public interface CachedAvatarUrlDuck {
    String getAvatarUrl();

    void calculateAvatarUrl();
}
