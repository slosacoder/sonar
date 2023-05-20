/*
 *  Copyright (c) 2023, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.velocity.fallback;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import jones.sonar.api.fallback.FallbackConnection;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public final class FallbackPacketDecoder extends ChannelInboundHandlerAdapter {
    private final FallbackConnection<ConnectedPlayer, MinecraftConnection> fallbackPlayer;
    private final long startKeepAliveId;

    private static final Component VERIFIED = Component.text("§e§lSonar §7» §aYou were successfully verified. §7Please reconnect to the server.");

    private boolean hasSentClientBrand, hasSentClientSettings;
    private boolean hasSentKeepAlive;

    @Override
    public void channelRead(final ChannelHandlerContext ctx,
                            final Object msg) throws Exception {
        if (msg instanceof MinecraftPacket packet) {
            //System.out.println("[client → server] " + fallbackPlayer.getPlayer().getUsername() + ": " + msg);

            final boolean legalPacket = packet instanceof ClientSettings
                    || packet instanceof PluginMessage
                    || packet instanceof KeepAlive;

            checkFrame(legalPacket, "unexpected packet: " + packet.getClass().getSimpleName());

            if (packet instanceof ClientSettings && !hasSentClientSettings) {
                checkFrame(hasSentKeepAlive, "unexpected timing #1");
                checkFrame(!hasSentClientBrand, "unexpected timing #2");

                hasSentClientSettings = true;
            }

            if (packet instanceof PluginMessage payload) {
                checkFrame(hasSentKeepAlive, "unexpected timing #3");

                if (!payload.getChannel().equals("MC|Brand") && !payload.getChannel().equals("minecraft:brand")) return;

                val valid = fallbackPlayer.getProtocolVersion() >= ProtocolVersion.MINECRAFT_1_13.getProtocol();

                checkFrame(payload.getChannel().equals("MC|Brand") || valid, "invalid client brand");
                checkFrame(!hasSentClientBrand, "duplicate client brand");
                checkFrame(hasSentClientSettings, "unexpected timing #4");

                hasSentClientBrand = true;

                fallbackPlayer.getFallback().getVerified().add(fallbackPlayer.getInetAddress());
                fallbackPlayer.getConnection().closeWith(Disconnect.create(VERIFIED, fallbackPlayer.getPlayer().getProtocolVersion()));
            }

            if (packet instanceof KeepAlive keepAlive && keepAlive.getRandomId() == startKeepAliveId) {
                checkFrame(!hasSentKeepAlive, "duplicate keep alive");

                hasSentKeepAlive = true;

                fallbackPlayer.getConnection().write(getForVersion(fallbackPlayer.getProtocolVersion()));
            }
        }
    }

    private static JoinGame getForVersion(final int protocolVersion) {
        if (protocolVersion >= ProtocolVersion.MINECRAFT_1_19_4.getProtocol()) {
            return FallbackPackets.JOIN_GAME_1_19_4;
        } else if (protocolVersion >= ProtocolVersion.MINECRAFT_1_19_1.getProtocol()) {
            return FallbackPackets.JOIN_GAME_1_19_1;
        } else if (protocolVersion >= ProtocolVersion.MINECRAFT_1_18_2.getProtocol()) {
            return FallbackPackets.JOIN_GAME_1_18_2;
        } else if (protocolVersion >= ProtocolVersion.MINECRAFT_1_16_2.getProtocol()) {
            return FallbackPackets.JOIN_GAME_1_16_2;
        }
        return FallbackPackets.LEGACY_JOIN_GAME;
    }

    private static final CorruptedFrameException CORRUPTED_FRAME = new CorruptedFrameException();

    private void checkFrame(final boolean condition, final String message) {
        if (!condition) {
            fallbackPlayer.fail(message);
            throw CORRUPTED_FRAME;
        }
    }
}
