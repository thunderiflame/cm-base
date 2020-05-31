package com.github.pure.cm.auth.client.exception;

/**
 * @author bairitan
 * @since 2019/11/14
 */
public class AuthClientException extends RuntimeException {

  public AuthClientException(String message) {
    super(message);
  }

  public AuthClientException(Throwable cause) {
    super(cause);
  }

  public AuthClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
