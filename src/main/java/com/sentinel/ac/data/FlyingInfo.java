package com.sentinel.ac.data;

/** Dados de um pacote de movimento (flying) recebido do cliente. */
public record FlyingInfo(
        boolean hasPos,
        boolean hasRot,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        boolean onGround) {
}
