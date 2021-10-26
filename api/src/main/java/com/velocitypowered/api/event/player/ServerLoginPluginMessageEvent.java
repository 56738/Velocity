/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.event.player;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.player.ServerLoginPluginMessageEvent.ResponseResult;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ServerLoginPluginMessageEvent implements ResultedEvent<ResponseResult> {
  private final ServerConnection connection;
  private final ChannelIdentifier identifier;
  private final byte[] contents;
  private final int sequenceId;
  private ResponseResult result;

  /**
   * Constructs a new {@code ServerLoginPluginMessageEvent}.
   * @param connection the connection on which the plugin message was sent
   * @param identifier the channel identifier for the message sent
   * @param contents the contents of the message
   * @param sequenceId the ID of the message
   */
  public ServerLoginPluginMessageEvent(
      ServerConnection connection, ChannelIdentifier identifier,
      byte[] contents, int sequenceId) {
    this.connection = checkNotNull(connection, "connection");
    this.identifier = checkNotNull(identifier, "identifier");
    this.contents = checkNotNull(contents, "contents");
    this.sequenceId = sequenceId;
    this.result = ResponseResult.UNKNOWN;
  }

  @Override
  public ResponseResult getResult() {
    return this.result;
  }

  @Override
  public void setResult(ResponseResult result) {
    this.result = checkNotNull(result, "result");
  }

  public ChannelIdentifier getIdentifier() {
    return identifier;
  }

  public byte[] getContents() {
    return contents.clone();
  }

  public ByteArrayInputStream contentsAsInputStream() {
    return new ByteArrayInputStream(contents);
  }

  public ByteArrayDataInput contentsAsDataStream() {
    return ByteStreams.newDataInput(contents);
  }

  public int getSequenceId() {
    return sequenceId;
  }

  @Override
  public String toString() {
    return "ServerLoginPluginMessageEvent{"
        + "connection=" + connection
        + ", identifier=" + identifier
        + ", sequenceId=" + sequenceId
        + ", contents=" + BaseEncoding.base16().encode(contents)
        + ", result=" + result
        + '}';
  }

  public static class ResponseResult implements Result {

    private static final ResponseResult UNKNOWN = new ResponseResult(null);

    private final byte@Nullable [] response;

    private ResponseResult(byte @Nullable [] response) {
      this.response = response;
    }

    @Override
    public boolean isAllowed() {
      return response != null;
    }

    /**
     * Returns the response to the message.
     *
     * @return the response to the message
     * @throws IllegalStateException if there is no reply (an unknown message)
     */
    public byte[] getResponse() {
      if (response == null) {
        throw new IllegalStateException("Fetching response of unknown message result");
      }
      return response.clone();
    }

    public static ResponseResult unknown() {
      return UNKNOWN;
    }

    public static ResponseResult reply(byte[] response) {
      checkNotNull(response, "response");
      return new ResponseResult(response);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ResponseResult that = (ResponseResult) o;
      return Arrays.equals(response, that.response);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(response);
    }

    @Override
    public String toString() {
      return "ResponseResult{"
          + "response=" + (response == null ? "none" : BaseEncoding.base16().encode(response))
          + '}';
    }
  }
}
