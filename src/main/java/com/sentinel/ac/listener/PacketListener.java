package com.sentinel.ac.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.PlayerData;

import java.util.UUID;
import java.util.logging.Level;

/** Lê pacotes do cliente na thread de rede e repassa para os checks de pacote. */
public final class PacketListener extends PacketListenerAbstract {

    private final SentinelAC plugin;

    public PacketListener(SentinelAC plugin) {
        super(PacketListenerPriority.LOW);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getUser() == null) {
            return;
        }
        UUID uuid = event.getUser().getUUID();
        if (uuid == null) {
            return;
        }
        PlayerData data = plugin.players().get(uuid);
        if (data == null || data.bypass) {
            return;
        }

        PacketTypeCommon type = event.getPacketType();
        try {
            if (WrapperPlayClientPlayerFlying.isFlying(type)) {
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
                Location loc = wrapper.getLocation();
                FlyingInfo info = new FlyingInfo(
                        wrapper.hasPositionChanged(),
                        wrapper.hasRotationChanged(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        loc.getYaw(), loc.getPitch(),
                        wrapper.isOnGround());
                if (info.hasPos()) {
                    data.offerFlying(info);
                }
                data.each(c -> c.onFlyingPacket(info));

            } else if (type == PacketType.Play.Client.ANIMATION) {
                long now = System.currentTimeMillis();
                data.each(c -> c.onAnimationPacket(now));

            } else if (type == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    int entityId = wrapper.getEntityId();
                    data.each(c -> c.onAttackPacket(entityId));
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Erro ao processar pacote " + type, t);
        }
    }
}
