/*
 * Copyright (C) 2023 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.velocity.fallback

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.protocol.ProtocolUtils
import io.netty.channel.Channel
import io.netty.channel.ChannelPipeline
import net.kyori.adventure.text.Component
import xyz.jonesdev.sonar.api.fallback.Fallback
import xyz.jonesdev.sonar.api.fallback.FallbackConnection
import xyz.jonesdev.sonar.api.fallback.protocol.FallbackPacket
import xyz.jonesdev.sonar.api.fallback.protocol.ProtocolVersion
import xyz.jonesdev.sonar.common.fallback.protocol.packets.Disconnect
import java.net.InetAddress

class FallbackPlayer(
  private val fallback: Fallback,
  private val player: ConnectedPlayer,
  private val connection: MinecraftConnection,
  private val channel: Channel,
  private val pipeline: ChannelPipeline,
  private val inetAddress: InetAddress,
  private val protocolVersion: ProtocolVersion,
) : FallbackConnection<ConnectedPlayer, MinecraftConnection> {

  override fun getFallback(): Fallback {
    return fallback
  }

  override fun getPlayer(): ConnectedPlayer {
    return player
  }

  override fun getConnection(): MinecraftConnection {
    return connection
  }

  override fun getChannel(): Channel {
    return channel
  }

  override fun getPipeline(): ChannelPipeline {
    return pipeline
  }

  override fun getInetAddress(): InetAddress {
    return inetAddress
  }

  override fun getProtocolVersion(): ProtocolVersion {
    return protocolVersion
  }

  override fun disconnect(reason: String) {
    val serialized = ProtocolUtils.getJsonChatSerializer(connection.protocolVersion)
      .serialize(Component.text(reason))
    connection.closeWith(Disconnect.create(serialized))
  }

  override fun sendPacket(packet: FallbackPacket) {
    connection.write(packet)
  }
}
